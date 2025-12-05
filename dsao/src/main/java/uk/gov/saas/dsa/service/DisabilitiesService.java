package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.DSAApplicationStudDisabilities;
import uk.gov.saas.dsa.domain.DisabilityType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationStudDisabilitiesRepository;
import uk.gov.saas.dsa.persistence.DisabilityTypeRepository;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.vo.DisabilityTypeVO.DISABILITY_NOT_LISTED;

/**
 * DSA Disabilities service
 */
@Service
public class DisabilitiesService {

	public static final String YES = "yes";

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DisabilityTypeRepository disabilityTypeRepository;
	private final DSAApplicationStudDisabilitiesRepository applicationStudDisabilitiesRepository;
	private final ApplicationService applicationService;

	/**
	 * Application service constructor.
	 */
	@Autowired
	public DisabilitiesService(DisabilityTypeRepository disabilityTypeRepository,
							   DSAApplicationStudDisabilitiesRepository applicationStudDisabilitiesRepository,
							   ApplicationService applicationService) {
		this.disabilityTypeRepository = disabilityTypeRepository;
		this.applicationStudDisabilitiesRepository = applicationStudDisabilitiesRepository;
		this.applicationService = applicationService;
	}

	/**
	 * To get the Stud disabilities
	 *
	 * @param dsaApplicationNumber
	 * @return List of saved student disabilities data
	 */
	public List<DisabilityTypeVO> populateApplicationDisabilities(long dsaApplicationNumber) {

		List<DSAApplicationStudDisabilities> existingApplicationDisabilities = getExistingApplicationDisabilities(
				dsaApplicationNumber);

		return getAllApplicationDisabilities(existingApplicationDisabilities);

	}

	private List<DisabilityTypeVO> getAllApplicationDisabilities(
			List<DSAApplicationStudDisabilities> existingApplicationDisabilities) {
		List<DisabilityTypeVO> userSavedDisabilityTypes = new ArrayList<>();
		if (existingApplicationDisabilities != null && !existingApplicationDisabilities.isEmpty()) {
			List<DisabilityType> disabilityTypes = findDisabilityTypes(existingApplicationDisabilities);

			for (DisabilityType disabilityInDB : disabilityTypes) {
				DisabilityTypeVO disabilityTypeVO = new DisabilityTypeVO();
				disabilityTypeVO.setSelected(true);
				String disabilityTypeCode = disabilityInDB.getDisabilityTypeCode();
				if (disabilityTypeCode.equalsIgnoreCase(DISABILITY_NOT_LISTED)) {
					DSAApplicationStudDisabilities notListedDisabilityEntry = existingApplicationDisabilities.stream()
							.filter(studDisability -> studDisability.getDisabilityTypeCode().equals(disabilityTypeCode))
							.findFirst().get();
					disabilityTypeVO.setDisabilityNotlistedText(notListedDisabilityEntry.getDisabilityNotlistedText());
				}
				disabilityTypeVO.setDisabilityTypeId(disabilityInDB.getDisabilityTypeId());
				disabilityTypeVO.setDisabilityCode(disabilityInDB.getDisabilityTypeCode());
				disabilityTypeVO.setDisabilityTypeDesc(disabilityInDB.getDisabilityTypeDesc());
				disabilityTypeVO.setDisabilityTypeHintText(disabilityInDB.getDisabilityTypeHintText());
				disabilityTypeVO.setIsActive(disabilityInDB.getIsActive());
				userSavedDisabilityTypes.add(disabilityTypeVO);
			}
		}
		sortDisabilityTypes(userSavedDisabilityTypes);
		return userSavedDisabilityTypes;
	}

	/**
	 * To save the user selected disabilities
	 *
	 * @param dsaApplicationNumber
	 * @param studRefNo
	 * @param selectedDisabilities
	 * @param notListedText
	 * @throws IllegalAccessException
	 */
	public void saveDisabilities(long dsaApplicationNumber, long studRefNo, List<String> selectedDisabilities,
								 String notListedText) throws IllegalAccessException {
		logger.info("user entered disabilities {}", selectedDisabilities);
		saveStudApplicationDisabilities(dsaApplicationNumber, studRefNo, selectedDisabilities, notListedText);

	}

	/**
	 * To get the active disability types
	 *
	 * @return all active disability types list
	 */
	public List<DisabilityTypeVO> getActiveDisabilityTypes() {
		logger.info("Getting active disabilities");
		List<DisabilityTypeVO> list = new ArrayList<>();
		List<DisabilityType> activeDisabilities = disabilityTypeRepository.findByIsActiveIgnoreCase(YES);
		activeDisabilities.forEach(disabilityInDB -> {
			DisabilityTypeVO disabilityVO = new DisabilityTypeVO();
			disabilityVO.setDisabilityTypeId(disabilityInDB.getDisabilityTypeId());
			disabilityVO.setDisabilityCode(disabilityInDB.getDisabilityTypeCode());
			disabilityVO.setDisabilityTypeDesc(disabilityInDB.getDisabilityTypeDesc());
			disabilityVO.setDisabilityTypeHintText(disabilityInDB.getDisabilityTypeHintText());
			disabilityVO.setIsActive(disabilityInDB.getIsActive());
			list.add(disabilityVO);
		});
		logger.info("All active disabilities {}", list);
		sortDisabilityTypes(list);
		return list;
	}

	/**
	 * Copy over disabilities when in REVIEW.
	 *
	 * @param dsaApplicationNumberOrigin      Number of application to copy from
	 * @param dsaApplicationNumberDestination Number of application to copy to
	 * @param studentReferenceNumber          Student Reference Number
	 * @throws IllegalAccessException Illegal Access Exception
	 */
	public void copyOverDisabilities(long dsaApplicationNumberOrigin, long dsaApplicationNumberDestination, long studentReferenceNumber)
			throws IllegalAccessException {

		// Get disabilities from previous session
		List<DSAApplicationStudDisabilities> dsaApplicationStudDisabilities =
				applicationStudDisabilitiesRepository.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumberOrigin, studentReferenceNumber);
		if (!dsaApplicationStudDisabilities.isEmpty()) {
			List<String> selectedDisabilities = new ArrayList<>();
			String notListedText = "";
			for (DSAApplicationStudDisabilities disability : dsaApplicationStudDisabilities) {
				if (disability.getDisabilityTypeCode().equalsIgnoreCase(DISABILITY_NOT_LISTED)) {
					notListedText = disability.getDisabilityNotlistedText();
				}
				selectedDisabilities.add(disability.getDisabilityTypeCode());
			}
			saveStudApplicationDisabilities(dsaApplicationNumberDestination, studentReferenceNumber,
					selectedDisabilities, notListedText, SectionStatus.REVIEW);
		}
	}

	private void sortDisabilityTypes(List<DisabilityTypeVO> disabilityTypes) {
		disabilityTypes.sort(Comparator.comparingLong(DisabilityTypeVO::getDisabilityTypeId));
	}

	private List<DisabilityType> findDisabilityTypes(
			List<DSAApplicationStudDisabilities> existingApplicationDisabilities) {
		List<String> disabilityCodes = existingApplicationDisabilities.stream()
				.map(DSAApplicationStudDisabilities::getDisabilityTypeCode).collect(Collectors.toList());

		return getDisabilityTypes(disabilityCodes);
	}

	private List<DisabilityType> getDisabilityTypes(List<String> disabilityCodes) {
		return disabilityTypeRepository.findAllByDisabilityTypeCodeIn(disabilityCodes);
	}

	private List<DSAApplicationStudDisabilities> getExistingApplicationDisabilities(long dsaApplicationNumber) {
		return applicationStudDisabilitiesRepository
				.findByDsaApplicationNumber(dsaApplicationNumber);
	}

	private void saveStudApplicationDisabilities(long dsaApplicationNumber, long studRefNo,
												 List<String> selectedDisabilityCodes, String notListedText, SectionStatus... sectionStatus) throws IllegalAccessException {

		final List<DSAApplicationStudDisabilities> disabilitiesToSave = populateSelectedDisabilities(
				dsaApplicationNumber, studRefNo, selectedDisabilityCodes, notListedText);
		List<DSAApplicationStudDisabilities> existingDisabilities = getExistingApplicationDisabilities(
				dsaApplicationNumber);

		// do not update the disabilities not changed
		boolean isSame = isUIDisabilityDataAndDBDisabilityDataSame(selectedDisabilityCodes, notListedText,
				existingDisabilities);
		if (!isSame) {
			// When user selected disabilities first time
			if (existingDisabilities.isEmpty()) {
				logger.info("No Disabilities in the database saving the user selected disabilities.");
				applicationStudDisabilitiesRepository.saveAll(disabilitiesToSave);

			} else {
				// When user updating the disabilities
				applicationStudDisabilitiesRepository.deleteAll(existingDisabilities);
				logger.info("existingDisabilities deleted {}", existingDisabilities);
				applicationStudDisabilitiesRepository.saveAll(disabilitiesToSave);
				logger.info("new disabilities saved {}", disabilitiesToSave);
			}
		}

		// When section status is REVIEW do not start ALLOWANCES
		if (sectionStatus.length == 0) {
			ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.ALLOWANCES,
					SectionStatus.NOT_STARTED);
		}

		// Set to completed when saving
		if (sectionStatus.length == 0) {
			ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.DISABILITIES,
					SectionStatus.COMPLETED);
		} else {
			ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.DISABILITIES,
					SectionStatus.REVIEW);
		}
	}

	private boolean isUIDisabilityDataAndDBDisabilityDataSame(List<String> selectedDisabilityCodes,
															  String notListedText, List<DSAApplicationStudDisabilities> existingDisabilities) {

		List<String> dbList = existingDisabilities.stream()
				.map(DSAApplicationStudDisabilities::getDisabilityTypeCode).sorted().collect(Collectors.toList());

		String dbTypeCodes = String.join(",", dbList);
		String selectedCodes = selectedDisabilityCodes.stream().sorted().collect(Collectors.joining(","));

		boolean dbCodesAndUISelectedCodesAreSame = selectedCodes.equalsIgnoreCase(dbTypeCodes);

		boolean dbNotListedTextAndUINotListedTextSame = false;
		if (selectedDisabilityCodes.contains(DISABILITY_NOT_LISTED) && dbList.contains(DISABILITY_NOT_LISTED)) {
			DSAApplicationStudDisabilities notListedDisability = existingDisabilities.stream()
					.filter(studDisability -> studDisability.getDisabilityTypeCode().equals(DISABILITY_NOT_LISTED))
					.findFirst().get();
			dbNotListedTextAndUINotListedTextSame = notListedText
					.equalsIgnoreCase(notListedDisability.getDisabilityNotlistedText());

		}
		logger.info("dbCodesAndUISelectedCodesAreSame: {}", dbCodesAndUISelectedCodesAreSame);
		logger.info("dbNotListedTextAndUINotListedTextSame: {}", dbNotListedTextAndUINotListedTextSame);
		return dbCodesAndUISelectedCodesAreSame && dbNotListedTextAndUINotListedTextSame;
	}

	private List<DSAApplicationStudDisabilities> populateSelectedDisabilities(long dsaApplicationNumber, long studRefNo,
																			  List<String> selectedDisabilityCodes, String notListedText) {
		logger.info("selectedDisabilityCodes saved {}", selectedDisabilityCodes);
		final List<DSAApplicationStudDisabilities> disabilitiesToSave = new ArrayList<>();
		selectedDisabilityCodes.forEach(disabilityCode -> {
			DSAApplicationStudDisabilities disability = new DSAApplicationStudDisabilities();
			if (disabilityCode.equalsIgnoreCase(DISABILITY_NOT_LISTED)) {
				disability.setDisabilityNotlistedText(notListedText);
			}
			disability.setDisabilityTypeCode(disabilityCode);
			disability.setDsaApplicationNumber(dsaApplicationNumber);
			disability.setStudentReferenceNumber(studRefNo);
			disability.setCreatedBy(LoggedinUserUtil.getUserId());
			disability.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
			disability.setLastUpdatedBy(disability.getCreatedBy());
			disability.setLastUpdatedDate(disability.getCreatedDate());
			disabilitiesToSave.add(disability);
		});
		return disabilitiesToSave;
	}
}
