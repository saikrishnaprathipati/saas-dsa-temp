package uk.gov.saas.dsa.service;

import static uk.gov.saas.dsa.model.SectionStatus.CANNOT_START_YET;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatAccountNumber;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatSortcode;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.populateCost;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.experimental.UtilityClass;
import uk.gov.saas.dsa.domain.DSAApplicationAssessmentFee;
import uk.gov.saas.dsa.domain.DSAApplicationBankAccount;
import uk.gov.saas.dsa.domain.DSAApplicationSectionStatus;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.model.SectionStatusResponse;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

/**
 * Service Utility class
 */
@UtilityClass
public class ServiceUtil {
	private static final Logger logger = LogManager.getLogger(ServiceUtil.class);

	public static void updateSectionStatus(ApplicationService applicationService, long dsaAppNo, Section section,
			SectionStatus status) throws IllegalAccessException {
		logger.info("updateSectionStatus for section {}, status {}", section, status);
		applicationService.updateSectionStatus(dsaAppNo, section, status);
	}

	public static void setSectionStatus(ApplicationService applicationService, long dsaAppNo, Section section,
			SectionStatus status) throws IllegalAccessException {
		logger.info("setSectionStatus for section {}, status {}", section, status);
		applicationService.setSectionStatus(dsaAppNo, section, status);
	}

	public static void updateOverallApplicationStatus(ApplicationService applicationService, long dsaAppNo) {
		logger.info("updateOverallApplciationStatus {}", dsaAppNo);

		applicationService.updateOverallApplicationSummaryStatus(dsaAppNo);
	}

	public static BankAccountVO populateBankDetailsVO(DSAApplicationBankAccount bankAccount) {
		BankAccountVO bankAccountVO = new BankAccountVO();
		logger.info("bank details in DB {}", bankAccount);
		if (bankAccount != null) {
			bankAccountVO.setDsaApplicationNumber(bankAccount.getDsaApplicationNumber());
			bankAccountVO.setStudentReferenceNumber(bankAccount.getStudentReferenceNumber());
			bankAccountVO.setAccountName(AllowancesHelper.toCapitaliseWord(bankAccount.getAccountName()));
			String accountNumber = bankAccount.getAccountNumber();
			bankAccountVO.setPaymentFor(bankAccount.getPaymentFor());
			bankAccountVO.setSortCode(bankAccount.getSortCode());
			bankAccountVO.setSortCodeForUI(formatSortcode(bankAccount.getSortCode()));
			bankAccountVO.setAccountNumber(accountNumber);
			bankAccountVO.setAccountNumberForUI(formatAccountNumber(accountNumber));

		}
		logger.info("bank details VO {}", bankAccountVO);
		return bankAccountVO;
	}

	public static SectionStatusResponse getApplicationSectionResponse(Section applicationSection,
			List<DSAApplicationSectionStatus> statusDBList) {
		SectionStatusResponse sectionStatusResponse = new SectionStatusResponse();
		final Optional<DSAApplicationSectionStatus> sectionStatusInBD = statusDBList.stream()
				.filter(appStatus -> appStatus.getSectionCode().equals(applicationSection)).findFirst();
		if (sectionStatusInBD.isPresent()) {
			DSAApplicationSectionStatus appSectionStatus = sectionStatusInBD.get();
			logger.info("appSectionStatus in DB {}", appSectionStatus);

			sectionStatusResponse.setSection(applicationSection);

			sectionStatusResponse.setSectionStatus(appSectionStatus.getSectionStatus());
			sectionStatusResponse.setEnabeldToView(!appSectionStatus.getSectionStatus().equals(CANNOT_START_YET));
			sectionStatusResponse.setLastUpdatedDate(appSectionStatus.getLastUpdatedDate());
			sectionStatusResponse.setLastUpdatedBy(appSectionStatus.getLastUpdatedBy());

		}
		logger.info("sectionStatusResponse {}", sectionStatusResponse);
		return sectionStatusResponse;
	}

	/**
	 * Get the section data
	 * 
	 * @param applicationSection
	 * @param sectionPartStatusList
	 * @return
	 */
	public static SectionStatusResponse getSectionStatusData(Section applicationSection,
			List<SectionStatusResponse> sectionPartStatusList) {

		logger.info("getSectionStatusData for {}", applicationSection);
		SectionStatusResponse sectionStatusResponse = new SectionStatusResponse();
		final Optional<SectionStatusResponse> sectionStatus = sectionPartStatusList.stream()
				.filter(appStatus -> appStatus.getSection().equals(applicationSection)).findFirst();
		if (sectionStatus.isPresent()) {
			sectionStatusResponse = sectionStatus.get();
		}
		logger.info("getSectionStatusData response {}", sectionStatusResponse);
		return sectionStatusResponse;
	}

	public static String getCharactersFromString(String cause, int numberOfCharacters) {
		String message = cause.substring(0, Math.min(cause.length(), numberOfCharacters));
		return message;
	}

	public static String capitalizeFully(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		return Arrays.stream(str.split("\\s+")).map(t -> t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static AssessmentFeeVO setAssessmentFeeVO(DSAApplicationAssessmentFee feeItem) {
		AssessmentFeeVO item = AssessmentFeeVO.builder().id(feeItem.getId())
				.dsaApplicationNumber(feeItem.getDsaApplicationNumber())
				.studentReferenceNumber(feeItem.getStudentReferenceNumber())
				.assessmentFeeCentreName(feeItem.getAssessmentCentreName()).assessorName(feeItem.getAssessorName())
				.totalHours(feeItem.getTotalHours()).costStr(currencyLocalisation(feeItem.getCost().doubleValue()))
				.cost(populateCost(feeItem.getCost())).build();
		logger.info("AssessmentFeeVO:{}", item);
		return item;
	}

	public static List<AssessmentFeeVO> setAssessmentFeeItems(List<DSAApplicationAssessmentFee> assessmentFeeList) {
		List<AssessmentFeeVO> assessmentFeeVOs = new ArrayList<AssessmentFeeVO>();
		if (assessmentFeeList != null) {
			assessmentFeeVOs = assessmentFeeList.stream()
					.sorted(Collections
							.reverseOrder(Comparator.comparing(DSAApplicationAssessmentFee::getLastUpdatedDate)))
					.map(ServiceUtil::setAssessmentFeeVO).collect(Collectors.toList());

		}
		logger.info("AssessmentFeeItems:{}", assessmentFeeVOs);
		return assessmentFeeVOs;

	}
	
	public static String formatDate(Date date) {
		return (date == null) ? "" : new SimpleDateFormat("dd MMMM yyyy").format(date);
	}
}
