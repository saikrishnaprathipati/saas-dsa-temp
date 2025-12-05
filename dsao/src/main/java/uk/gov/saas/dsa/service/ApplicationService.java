package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.*;
import uk.gov.saas.dsa.model.*;
import uk.gov.saas.dsa.persistence.DSAApplicationSectionStatusRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationsMadeRepository;
import uk.gov.saas.dsa.service.notification.NotificationVO;
import uk.gov.saas.dsa.vo.ApplicationSectiponStatusVO;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserType;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static uk.gov.saas.dsa.model.ApplicationSectionPart.*;
import static uk.gov.saas.dsa.model.ApplicationSummaryStatus.APPLICATION_INCOMPLETE;
import static uk.gov.saas.dsa.model.Section.*;
import static uk.gov.saas.dsa.model.SectionStatus.*;
import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.*;

/**
 * DSA Application service
 */
@Service
public class ApplicationService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DSAApplicationsMadeRepository applicationsMadeRepository;
	private final DSAApplicationSectionStatusRepository applicationSectionStatusRepository;
	private final ConfigDataService configDataService;
	private final StudentDetailsService studentDetailsService;
	private final DSAApplicationDeletionServcie deleteService;

	/**
	 * Application service constructor
	 *
	 * @param applicationsMadeRepository
	 * @param applicationSectionStatusRepository
	 */
	@Autowired
	public ApplicationService(DSAApplicationsMadeRepository applicationsMadeRepository,
			DSAApplicationSectionStatusRepository applicationSectionStatusRepository,
			ConfigDataService configDataService, StudentDetailsService studentDetailsService,
			DSAApplicationDeletionServcie deleteService) {
		this.applicationsMadeRepository = applicationsMadeRepository;
		this.applicationSectionStatusRepository = applicationSectionStatusRepository;
		this.studentDetailsService = studentDetailsService;
		this.configDataService = configDataService;
		this.deleteService = deleteService;
	}

	/**
	 * To create the DSA Application
	 *
	 * @param studentReferenceNumber
	 * @param fundingEligibilityStatus TODO
	 * @return Application response
	 */
	public ApplicationResponse createApplication(long studentReferenceNumber,
			FundingEligibilityStatus fundingEligibilityStatus, int sessionCode) throws IllegalAccessException {
		logger.info("createApplication for studentReferenceNumber:{}", studentReferenceNumber);
		DSAApplicationsMade applicationsMade = new DSAApplicationsMade();
		applicationsMade.setStudentReferenceNumber(studentReferenceNumber);
		// Setting the initial application status
		applicationsMade.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);
		applicationsMade.setOverallApplicationStatus(OverallApplicationStatus.STARTED);
		int currentActiveSession = configDataService.getCurrentActiveSession();
		int yearDifference = currentActiveSession - sessionCode; // 2025- (2025, 2024, 0)
		if (yearDifference > 1) {
			throw new IllegalAccessException(String.format(
					"Unable to create the application details for sessioncode %s with studentReferenceNumber %s",
					sessionCode, studentReferenceNumber));
		}
		applicationsMade.setSessionCode(sessionCode);

		/*
		 * TODO: After manage account work completion, we will load the userId
		 * dynamically and remove the hardcoded val
		 */

		applicationsMade.setCreatedBy(LoggedinUserUtil.getUserId());
		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

		applicationsMade.setCreatedDate(timestamp);
		applicationsMade.setLastUpdatedBy(applicationsMade.getCreatedBy());
		applicationsMade.setLastUpdatedDate(applicationsMade.getCreatedDate());
		DSAApplicationsMade savedApplication = applicationsMadeRepository.save(applicationsMade);
		logger.info("Saved application {}", savedApplication);
		long dsaApplicationNumber = savedApplication.getDsaApplicationNumber();

		SectionStatus studentSectionStatus = deriveStudentSectionStatus(fundingEligibilityStatus);
		DSAApplicationSectionStatus aboutStudentSection = buildStatusFor(ABOUT_STUDENT, studentSectionStatus,
				dsaApplicationNumber, studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);

		DSAApplicationSectionStatus aboutCourseSection = buildStatusFor(ABOUT_COURSE, COMPLETED, dsaApplicationNumber,
				studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);

		DSAApplicationSectionStatus aboutDisabilities = buildStatusFor(DISABILITIES, NOT_STARTED, dsaApplicationNumber,
				studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);

		DSAApplicationSectionStatus aboutAllowances = buildStatusFor(ALLOWANCES, CANNOT_START_YET, dsaApplicationNumber,
				studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);
		DSAApplicationSectionStatus needsAssessmentFeeSection = buildStatusFor(NEEDS_ASSESSMENT_FEE, CANNOT_START_YET,
				dsaApplicationNumber, studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);
		DSAApplicationSectionStatus advisorDeclarationSection = buildStatusFor(ADVISOR_DECLARATION, CANNOT_START_YET,
				dsaApplicationNumber, studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);
		DSAApplicationSectionStatus studDeclarationSection = buildStatusFor(STUDENT_DECLARATION, CANNOT_START_YET,
				dsaApplicationNumber, studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);

		DSAApplicationSectionStatus additionalInfo = buildStatusFor(ADDITIONAL_INFO, NOT_STARTED,
				dsaApplicationNumber, studentReferenceNumber, LoggedinUserUtil.getUserId(), timestamp);
		
		List<DSAApplicationSectionStatus> dsaAppSectionStatusList = asList(aboutStudentSection, aboutCourseSection,
				aboutDisabilities, aboutAllowances, needsAssessmentFeeSection, advisorDeclarationSection,
				studDeclarationSection, additionalInfo);
		logger.info("saving dsaAppSectionStatusList {}", dsaAppSectionStatusList);
		applicationSectionStatusRepository.saveAll(dsaAppSectionStatusList);

		List<DSAApplicationSectionStatus> statusList = applicationSectionStatusRepository
				.findByDsaApplicationNumber(dsaApplicationNumber);

		ApplicationResponse applicationResponse = new ApplicationResponse();

		applicationResponse.setDsaApplicationNumber(savedApplication.getDsaApplicationNumber());
		applicationResponse.setStudentReferenceNumber(savedApplication.getStudentReferenceNumber());
		applicationResponse.setNewApplication(true);
		applicationResponse.setApplicationStatus(applicationsMade.getApplicationSummaryStatus());
		applicationResponse.setOverallApplicationStatus(applicationsMade.getOverallApplicationStatus());
		applicationResponse.setSessionCode(applicationsMade.getSessionCode());

		ApplicationSectionResponse status = populateSectionStatus(statusList);
		applicationResponse.setSectionStatusData(status);
		applicationResponse.setSectionPartStatusList(populateSectionStatusList(statusList));
		populateSectionsCompletionText(statusList, applicationResponse);
		setApplicationFullySubmitted(applicationResponse, applicationsMade);
		return applicationResponse;
	}

	public void updateOverallApplciationStatus(long dsaAppNo, long studentRefNo, OverallApplicationStatus status) {
		DSAApplicationsMade dsaApplicationsMade = findByDsaApplicationNumberAndStudentReferenceNumber(dsaAppNo,
				studentRefNo);
		dsaApplicationsMade.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		dsaApplicationsMade.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaApplicationsMade.setOverallApplicationStatus(status);
		applicationsMadeRepository.save(dsaApplicationsMade);
	}

	public void updateDSAAplicationStatus(NotificationVO notificationVO, DSAApplicationsMade dsaApplicationsMade,
			Date dateAndTime) throws IllegalAccessException {
		logger.info("DSA applciation status updation Start");
		OverallApplicationStatus status = deriveDSAApplicationStatus(notificationVO);
		if (status != null) {
			logger.info("Processing the steps notification record {} for DSA applciation status change",
					notificationVO);

			boolean hasSameStatus = dsaApplicationsMade.getOverallApplicationStatus().equals(status);

			if (!hasSameStatus) {
				dsaApplicationsMade.setOverallApplicationStatus(status);
				dsaApplicationsMade.setLastUpdatedBy(notificationVO.getUpdatedBy());
				dsaApplicationsMade.setLastUpdatedDate(new Timestamp(dateAndTime.getTime()));
				applicationsMadeRepository.save(dsaApplicationsMade);
				logger.info("DSA application status updated successfully with steps status {}", status);

			} else {
				logger.info("DSA application status already updated with steps status {}", status);
			}
		}
		logger.info("DSA applciation status updation End");
	}

	public DSAApplicationsMade getLatestDSAApplciationByStudRefNo(long studentReferenceNumber) {
		return applicationsMadeRepository.findLatestDSAApplciationByStudentRefNumber(studentReferenceNumber);
	}
	
	public DSAApplicationsMade getDSAApplciationByStudRefNoAndSessionCode(long studentReferenceNumber, int sessionCode) {
		DSAApplicationsMade applicationsMade = applicationsMadeRepository.findByStudentReferenceNumberAndSessionCode(studentReferenceNumber, sessionCode);
		if (applicationsMade == null) {
			String message = String.format("DSA applciation not found for studentReferenceNumber:%s, sessionCode: %s", studentReferenceNumber, sessionCode);
			throw new IllegalArgumentException(message);
		}
		return applicationsMade;
	}

	public List<DSAApplicationsMade> findAllAdvisorDeclarationCompletedAndNotSubmittedByStudent(int currentSession) {
		List<DSAApplicationsMade> notSubmittedByStudentList = applicationsMadeRepository
				.findAllAdvisorDeclarationCompletedAndNotSubmittedByStudent(currentSession);

		logger.info("Before filtering non submitted Applications count {} and Stud ref no: {}",
				getAllStudentRefNumbers(notSubmittedByStudentList).size(),
				getAllStudentRefNumbers(notSubmittedByStudentList));

		List<DSAApplicationsMade> filteredList = new ArrayList<>();
		if (notSubmittedByStudentList != null) {
			filteredList = notSubmittedByStudentList.stream()
					.filter(t -> Arrays.asList(NOT_STARTED, STARTED, PENDING).contains(
							populateStatus(STUDENT_DECLARATION, t.getDsaApplicationSectionStatus()).getSectionStatus()))
					.collect(Collectors.toList());

			logger.info("After filtering non submitted Applications count {} and Stud ref no: {}",
					getAllStudentRefNumbers(filteredList).size(), getAllStudentRefNumbers(filteredList));
		}

		return filteredList;
	}
	
	public List<DSAApplicationsMade> findAllByStatusAndSessionCode(OverallApplicationStatus status,
			int currentSession) {

		List<DSAApplicationsMade> list = applicationsMadeRepository.findByOverallApplicationStatusAndSessionCode(status,
				currentSession);
		if (list == null) {
			list = new ArrayList<DSAApplicationsMade>();
		}
		logger.info("Applciations with status {} for session {} : {}", status, currentSession, list);
		return list;
	}
	private List<Long> getAllStudentRefNumbers(List<DSAApplicationsMade> filteredList) {
		return filteredList.stream().map(DSAApplicationsMade::getStudentReferenceNumber).collect(Collectors.toList());
	}

	private OverallApplicationStatus deriveDSAApplicationStatus(NotificationVO notificationVO) {
		OverallApplicationStatus overAllApplicationStatus = null;
		EmailNotificationType notificationType = notificationVO.getNotificationType();

		switch (notificationType) {
		case DSA_90_DAY_WITHDRAW:
			overAllApplicationStatus = OverallApplicationStatus.WITHDRAWN;
			break;
		case STEPS_REJECTED:
			overAllApplicationStatus = OverallApplicationStatus.NOT_AWARDED;
			break;
		case STEPS_AWARDED:
			overAllApplicationStatus = OverallApplicationStatus.AWARDED;
			break;
		case STEPS_PENDED_WITH_SAAS:
			overAllApplicationStatus = OverallApplicationStatus.IN_PROGRESS;
			break;
		default:
			logger.warn("Not processing the steps record {}", notificationVO);
			break;
		}

		return overAllApplicationStatus;
	}

	private SectionStatus deriveStudentSectionStatus(FundingEligibilityStatus fundingEligibilityStatus) {
		SectionStatus status = NOT_STARTED;

		switch (fundingEligibilityStatus) {

		case CONFIRMED:
			status = SectionStatus.COMPLETED;
			break;
		case REJECTED:
			status = SectionStatus.REJECTED;
			break;
		case PENDING:
			status = PENDING;
			break;
		default:
			status = SectionStatus.REJECTED;
			break;
		}
		logger.info("Funding Eligibility Status {} to Student section status {}", fundingEligibilityStatus, status);
		return status;
	}

	/**
	 * Find application
	 *
	 * @param dsaApplicationNumber   the DSA application number
	 * @param studentReferenceNumber the stud reference number
	 * @return Application response
	 * @throws IllegalAccessException
	 */
	public ApplicationResponse findApplication(long dsaApplicationNumber, long studentReferenceNumber)
			throws IllegalAccessException {
		logger.info("findApplication for dsaApplicationNumber: {}, studentReferenceNumber:{}", dsaApplicationNumber,
				studentReferenceNumber);
		ApplicationResponse applicationResponse = new ApplicationResponse();
		DSAApplicationsMade applicationsMade = findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber,
				studentReferenceNumber);
		logger.info("applicationsMade data in DB {} ", applicationsMade);
		if (applicationsMade == null) {
			throw new IllegalAccessException(String.format(
					"Unable to find the application details for dsaApplicationNumber %s studentReferenceNumber %s",
					dsaApplicationNumber, studentReferenceNumber));

		}
		List<DSAApplicationSectionStatus> statusList = applicationsMade.getDsaApplicationSectionStatus();
		ApplicationSectionResponse status = populateSectionStatus(statusList);
		applicationResponse.setSectionStatusData(status);
		applicationResponse.setSectionPartStatusList(populateSectionStatusList(statusList));
		applicationResponse.setDsaApplicationNumber(applicationsMade.getDsaApplicationNumber());
		applicationResponse.setStudentReferenceNumber(applicationsMade.getStudentReferenceNumber());
		applicationResponse.setApplicationStatus(applicationsMade.getApplicationSummaryStatus());
		applicationResponse.setOverallApplicationStatus(applicationsMade.getOverallApplicationStatus());
		applicationResponse.setSessionCode(applicationsMade.getSessionCode());

		String nextSessionCode = Integer.valueOf(applicationsMade.getSessionCode() + 1).toString();
		applicationResponse.setAcademicYear(applicationsMade.getSessionCode() + " to " + nextSessionCode);

		applicationResponse.setAllAllowancesCompleted(allowancesCompleted(applicationsMade));

		applicationResponse.setAdvisorDeclarationCompleted(
				isCompleted(ServiceUtil.getApplicationSectionResponse(ADVISOR_DECLARATION, statusList)));

		applicationResponse.setStudentDeclarationCompleted(
				isCompleted(ServiceUtil.getApplicationSectionResponse(STUDENT_DECLARATION, statusList)));

		populateAssessmentFeeInfo(applicationResponse, applicationsMade);
		populateAdditionalInfo(applicationResponse, applicationsMade);
		populateSectionsCompletionText(statusList, applicationResponse);

		setBankAccountDetails(applicationResponse, applicationsMade);

		applicationResponse.setConsumables(getConsumables(applicationsMade.getApplicationStudConsumables()));
		applicationResponse.setNmphAllowances(getNMPHAllowances(applicationsMade.getApplicationStudNMPHAllowances()));
		applicationResponse.setEquipments(getEquipments(applicationsMade.getApplicationStudequipments()));

		applicationResponse.setTravelExpeses(getTravelExpenses(applicationsMade.getTravelExpenses()));
		applicationResponse.setAccommodations(getAccommodations(applicationsMade.getAccommodations()));
		setApplicationFullySubmitted(applicationResponse, applicationsMade);
		logger.info("Find application - ApplicationResponse {} ", applicationResponse);
		return applicationResponse;
	}

	private void populateAdditionalInfo(ApplicationResponse applicationResponse, DSAApplicationsMade applicationsMade) {
		DSAAppAdditionalInformation additionalInfo = applicationsMade.getAdditionalInfo();
		if (additionalInfo != null) {
			applicationResponse.setAdditionalInfoText(additionalInfo.getInfoText());
		}
	}

	private void populateAssessmentFeeInfo(ApplicationResponse applicationResponse,
			DSAApplicationsMade applicationsMade) {

		applicationResponse.setNeedsAssessmentCompleted(isCompleted(ServiceUtil.getApplicationSectionResponse(
				Section.NEEDS_ASSESSMENT_FEE, applicationsMade.getDsaApplicationSectionStatus())));

		List<DSAApplicationAssessmentFee> assessmentFeeList = applicationsMade.getAssessmentFeeList();
		List<AssessmentFeeVO> assessmentFeeVOs = ServiceUtil.setAssessmentFeeItems(assessmentFeeList);

		applicationResponse.setAssessmentFeeList(assessmentFeeVOs);
	}

	public DSAApplicationsMade findByDsaApplicationNumberAndStudentReferenceNumber(long dsaApplicationNumber,
			long studentReferenceNumber) {
		return applicationsMadeRepository.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber,
				studentReferenceNumber);
	}

	public DSAApplicationsMade findByDsaApplicationNumber(long dsaApplicationNumber) {

		DSAApplicationsMade made = new DSAApplicationsMade();
		Optional<DSAApplicationsMade> applciation = applicationsMadeRepository.findById(dsaApplicationNumber);
		if (applciation.isPresent()) {
			made = applciation.get();
		}
		return made;
	}

	private boolean isCompleted(SectionStatusResponse applicationSectionResponse) {
		boolean isCompleted = applicationSectionResponse != null
				&& applicationSectionResponse.getSectionStatus() != null
				&& applicationSectionResponse.getSectionStatus().equals(COMPLETED);
		logger.info("{} section is completed {} ", applicationSectionResponse.getSection(), isCompleted);
		return isCompleted;
	}

	private boolean allowancesCompleted(DSAApplicationsMade applicationsMade) {

		List<DSAApplicationStudConsumables> consumables = applicationsMade.getApplicationStudConsumables();
		List<DSAApplicationStudNMPH> allowances = applicationsMade.getApplicationStudNMPHAllowances();
		List<DSAApplicationTravelExp> travelExpenses = applicationsMade.getTravelExpenses();
		List<DSAApplicationStudEquipment> applicationStudequipments = applicationsMade.getApplicationStudequipments();
		List<DSAApplicationStudAccommodation> accommodations = applicationsMade.getAccommodations();
		List<DSAQuotePDF> quotes = applicationsMade.getApplicationQuotes();

		boolean hasNMPHAllowances = allowances != null && allowances.size() > 0;
		boolean hastravelExpenses = travelExpenses != null && travelExpenses.size() > 0;
		boolean hasConsumables = consumables != null && consumables.size() > 0;
		boolean hasEquipments = applicationStudequipments != null && applicationStudequipments.size() > 0;
		boolean hasQuotes = quotes != null && quotes.size() > 0;
		boolean hasAccommodationAllowances = accommodations != null && accommodations.size() > 0;

		return hasConsumables || hasNMPHAllowances || hastravelExpenses || hasEquipments || hasAccommodationAllowances
				|| hasQuotes;
	}

	private ApplicationResponse populateSectionsCompletionText(
			List<DSAApplicationSectionStatus> dsaAppSectionStatusDBList, ApplicationResponse appResponse) {
		appResponse.setPart1CompletionStatusText(partCompletiontext(PART1, dsaAppSectionStatusDBList));
		appResponse.setPart2CompletionStatusText(partCompletiontext(PART2, dsaAppSectionStatusDBList));
		appResponse.setPart3CompletionStatusText(partCompletiontext(PART3, dsaAppSectionStatusDBList));
		return appResponse;
	}

	private String partCompletiontext(ApplicationSectionPart part,
			List<DSAApplicationSectionStatus> dsaAppSectionStatusDBList) {
		String text = "%s of %s sections completed";
		int totalSections = getPartSectionList(part, dsaAppSectionStatusDBList).size();
		long completedSections = dsaAppSectionStatusDBList.stream()
				.filter(t -> (t.getSectionPart().equals(part) && t.getSectionStatus().equals(COMPLETED)))
				.map(DSAApplicationSectionStatus::getSectionStatus).count();
		LoggedinUserType userType = LoggedinUserUtil.loggedinUserType();
		if (part.equals(PART2) && userType.equals(LoggedinUserType.STUDENT)) {
			// To ignore the student needs assessment fee section from the description
			completedSections = completedSections - 1;
			totalSections = totalSections - 1;
		}
		return String.format(text, completedSections, totalSections);
	}

	private List<DSAApplicationSectionStatus> getPartSectionList(ApplicationSectionPart sectionPart,
			List<DSAApplicationSectionStatus> dsaAppSectionStatusDBList) {

		return dsaAppSectionStatusDBList.stream().filter(dsaApplicationStatus -> {
			return dsaApplicationStatus.getSectionPart().equals(sectionPart);
		}).collect(Collectors.toList());
	}

	private ApplicationSectionResponse populateSectionStatus(List<DSAApplicationSectionStatus> appSectionStatusList) {
		ApplicationSectionResponse applicationSectionStatus = new ApplicationSectionResponse();

		applicationSectionStatus.setAboutStudentSectionData(populateStatus(ABOUT_STUDENT, appSectionStatusList));
		applicationSectionStatus.setAboutCourseSectionData(populateStatus(ABOUT_COURSE, appSectionStatusList));
		applicationSectionStatus.setDisabilitySectionData(populateStatus(DISABILITIES, appSectionStatusList));
		applicationSectionStatus.setAllowanceSectionData(populateStatus(ALLOWANCES, appSectionStatusList));
		applicationSectionStatus
				.setNeedsAssessmentFeeSectionData(populateStatus(NEEDS_ASSESSMENT_FEE, appSectionStatusList));
		applicationSectionStatus
		.setAdditionalInfoData(populateStatus(ADDITIONAL_INFO, appSectionStatusList));
		
		applicationSectionStatus
				.setAdvisorDeclarationSectionData(populateStatus(ADVISOR_DECLARATION, appSectionStatusList));
		applicationSectionStatus
				.setStudentDeclarationSectionData(populateStatus(STUDENT_DECLARATION, appSectionStatusList));

		return applicationSectionStatus;
	}

	private List<SectionStatusResponse> populateSectionStatusList(
			List<DSAApplicationSectionStatus> appSectionStatusDbList) {

		SectionStatusResponse aboutStudent = populateStatus(ABOUT_STUDENT, appSectionStatusDbList);
		SectionStatusResponse aboutCourse = populateStatus(ABOUT_COURSE, appSectionStatusDbList);
		SectionStatusResponse aboutDisabilities = populateStatus(DISABILITIES, appSectionStatusDbList);
		SectionStatusResponse allowances = populateStatus(ALLOWANCES, appSectionStatusDbList);
		SectionStatusResponse needsAssessmentFee = populateStatus(NEEDS_ASSESSMENT_FEE, appSectionStatusDbList);
		SectionStatusResponse additionalInfo = populateStatus(ADDITIONAL_INFO, appSectionStatusDbList);
		
		SectionStatusResponse advisorDecleraion = populateStatus(ADVISOR_DECLARATION, appSectionStatusDbList);
		SectionStatusResponse studentDeclaration = populateStatus(STUDENT_DECLARATION, appSectionStatusDbList);

		return Arrays.asList(aboutStudent, aboutCourse, aboutDisabilities, allowances, needsAssessmentFee, additionalInfo, 
				advisorDecleraion, studentDeclaration);
	}

	private SectionStatusResponse populateStatus(Section applicationSection,
			List<DSAApplicationSectionStatus> statusDBList) {
		return ServiceUtil.getApplicationSectionResponse(applicationSection, statusDBList);
	}

	/**
	 * Find the application by student reference number.
	 *
	 * @param studentReferenceNumber
	 * @return ApplicationResponse
	 */
	public ApplicationResponse findApplicationByStudentReferenceNumber(long studentReferenceNumber) {
		logger.info("findApplicationByStudentReferenceNumber: {} ", studentReferenceNumber);
		ApplicationResponse applicationResponse = new ApplicationResponse();
		DSAApplicationsMade applicationsMade = applicationsMadeRepository
				.findLatestDSAApplciationByStudentRefNumber(studentReferenceNumber);
		if (applicationsMade == null) {
			applicationResponse.setNewApplication(true);
		} else {
			setDashboardData(applicationResponse, applicationsMade);
			applicationResponse.setNewApplication(false);
		}
		return applicationResponse;
	}

	/**
	 * Find application by student reference number and session code
	 */
	public ApplicationResponse findApplicationByStudentReferenceNumberAnSessionCode(long studentReferenceNumber,
			int sessionCode) {
		logger.info("Find application for studentReferenceNumber: {} and session:  {}", studentReferenceNumber,
				sessionCode);

		ApplicationResponse applicationResponse = new ApplicationResponse();
		DSAApplicationsMade applicationsMade = applicationsMadeRepository
				.findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(studentReferenceNumber,
						sessionCode);
		if (applicationsMade != null) {
			setDashboardData(applicationResponse, applicationsMade);
			applicationResponse.setNewApplication(false);
		} else {
			applicationResponse.setNewApplication(true);
		}
		return applicationResponse;
	}

	/**
	 * Find application by student reference number and session code
	 */
	public ApplicationResponse resetApplicationByStudentReferenceNumberAndSessionCode(long studentReferenceNumber,
			int sessionCode) throws IllegalAccessException {
		logger.info("Reset Application for studentReferenceNumber:{} is {}", studentReferenceNumber, sessionCode);

		ApplicationResponse applicationResponse = new ApplicationResponse();
		DSAApplicationsMade applicationsMade = applicationsMadeRepository
				.findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(studentReferenceNumber,
						sessionCode);

		if (applicationsMade != null) {
			addAdditionalInfoSectionIfNotExist(applicationsMade);
			resetStudentAndAdvisorDeclarations(applicationsMade.getDsaApplicationNumber());
			resetApplicationStatus(applicationsMade);
			setDashboardData(applicationResponse, applicationsMade);
			applicationResponse.setNewApplication(false);
			deleteService.deleteDSAAplicationPDF(sessionCode, applicationsMade.getDsaApplicationNumber(),
					studentReferenceNumber);
			logger.info("Reset declarations, application status, delete PDF for : {} and {}", studentReferenceNumber,
					sessionCode);
		} else {
			applicationResponse.setNewApplication(true);
		}
		return applicationResponse;
	}

	private void addAdditionalInfoSectionIfNotExist(DSAApplicationsMade applicationsMade) {
		List<DSAApplicationSectionStatus> list = applicationSectionStatusRepository
				.findByDsaApplicationNumber(applicationsMade.getDsaApplicationNumber());
		long count = list.stream().filter(t -> t.getSectionCode().equals(ADDITIONAL_INFO)).count();
		if (count <= 0) {

			DSAApplicationSectionStatus additionalInfo = buildStatusFor(ADDITIONAL_INFO, NOT_STARTED,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(), LoggedinUserUtil.getUserId(),
					Timestamp.valueOf(LocalDateTime.now()));
			list.add(additionalInfo);
			applicationSectionStatusRepository.saveAll(list);
			applicationsMade.setDsaApplicationSectionStatus(list);
		}

	}

	public DSAApplicationsMade findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(
			long studentReferenceNumber, int sessionCode) {
		return applicationsMadeRepository.findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(
				studentReferenceNumber, sessionCode);
	}

	private void setDashboardData(ApplicationResponse applicationResponse, DSAApplicationsMade applicationsMade) {
		logger.info("Start DashboardData  {}", applicationsMade);

		applicationResponse.setDsaApplicationNumber(applicationsMade.getDsaApplicationNumber());
		applicationResponse.setStudentReferenceNumber(applicationsMade.getStudentReferenceNumber());
		applicationResponse.setApplicationStatus(applicationsMade.getApplicationSummaryStatus());
		applicationResponse.setOverallApplicationStatus(applicationsMade.getOverallApplicationStatus());

		populateAssessmentFeeInfo(applicationResponse, applicationsMade);
		applicationResponse.setSessionCode(applicationsMade.getSessionCode());
		List<DSAApplicationSectionStatus> statusList = applicationsMade.getDsaApplicationSectionStatus();

		StudentResultVO studentResultVO;
		try {
			studentResultVO = studentDetailsService
					.findStudentDetailsFromDB(applicationsMade.getStudentReferenceNumber());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		applicationResponse.setFirstName(studentResultVO.getFirstName());
		applicationResponse.setLastName(studentResultVO.getLastName());
		logger.info("Start DashboardData for session code {}", applicationResponse);

		// If status list is empty
		if (statusList.isEmpty()) {
			Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

			SectionStatus studentSectionStatus = deriveStudentSectionStatus(
					studentResultVO.getFundingEligibilityStatus());
			DSAApplicationSectionStatus aboutStudentSection = buildStatusFor(ABOUT_STUDENT, studentSectionStatus,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(),
					LoggedinUserUtil.getUserId(), timestamp);

			DSAApplicationSectionStatus aboutCourseSection = buildStatusFor(ABOUT_COURSE, COMPLETED,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(),
					LoggedinUserUtil.getUserId(), timestamp);

			DSAApplicationSectionStatus aboutDisabilities = buildStatusFor(DISABILITIES, NOT_STARTED,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(),
					LoggedinUserUtil.getUserId(), timestamp);

			DSAApplicationSectionStatus aboutAllowances = buildStatusFor(ALLOWANCES, CANNOT_START_YET,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(),
					LoggedinUserUtil.getUserId(), timestamp);
			DSAApplicationSectionStatus needsAssessmentFeeSection = buildStatusFor(NEEDS_ASSESSMENT_FEE,
					CANNOT_START_YET, applicationsMade.getDsaApplicationNumber(),
					applicationsMade.getStudentReferenceNumber(), LoggedinUserUtil.getUserId(), timestamp);
			
			
			
			DSAApplicationSectionStatus additionalInfo = buildStatusFor(ADDITIONAL_INFO,
					NOT_STARTED, applicationsMade.getDsaApplicationNumber(),
					applicationsMade.getStudentReferenceNumber(), LoggedinUserUtil.getUserId(), timestamp);
			
			DSAApplicationSectionStatus advisorDeclarationSection = buildStatusFor(ADVISOR_DECLARATION,
					CANNOT_START_YET, applicationsMade.getDsaApplicationNumber(),
					applicationsMade.getStudentReferenceNumber(), LoggedinUserUtil.getUserId(), timestamp);
			DSAApplicationSectionStatus studDeclarationSection = buildStatusFor(STUDENT_DECLARATION, CANNOT_START_YET,
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber(),
					LoggedinUserUtil.getUserId(), timestamp);

			List<DSAApplicationSectionStatus> dsaAppSectionStatusList = asList(aboutStudentSection, aboutCourseSection,
					aboutDisabilities, aboutAllowances, needsAssessmentFeeSection, additionalInfo, advisorDeclarationSection,
					studDeclarationSection);
			applicationSectionStatusRepository.saveAll(dsaAppSectionStatusList);
			statusList = applicationSectionStatusRepository
					.findByDsaApplicationNumber(applicationsMade.getDsaApplicationNumber());
		}

		ApplicationSectionResponse status = populateSectionStatus(statusList);
		applicationResponse.setSectionStatusData(status);
		applicationResponse.setSectionPartStatusList(populateSectionStatusList(statusList));

		populateSectionsCompletionText(statusList, applicationResponse);
		setBankAccountDetails(applicationResponse, applicationsMade);
		applicationResponse.setConsumables(getConsumables(applicationsMade.getApplicationStudConsumables()));
		applicationResponse.setNmphAllowances(getNMPHAllowances(applicationsMade.getApplicationStudNMPHAllowances()));
		applicationResponse.setEquipments(getEquipments(applicationsMade.getApplicationStudequipments()));
		applicationResponse.setTravelExpeses(getTravelExpenses(applicationsMade.getTravelExpenses()));
		applicationResponse.setAllAllowancesCompleted(allowancesCompleted(applicationsMade));
		applicationResponse.setApplicationUpdated(
				new SimpleDateFormat("dd MMMM yyyy").format(applicationsMade.getLastUpdatedDate()));

		applicationResponse.setAdvisorDeclarationCompleted(
				isCompleted(ServiceUtil.getApplicationSectionResponse(ADVISOR_DECLARATION, statusList)));

		applicationResponse.setStudentDeclarationCompleted(
				isCompleted(ServiceUtil.getApplicationSectionResponse(STUDENT_DECLARATION, statusList)));
		setApplicationFullySubmitted(applicationResponse, applicationsMade);
		logger.info("End of DashboardData for session code {}", applicationsMade.getSessionCode());
	}

	/**
	 * To update over all application status
	 *
	 * @param dsaApplicationNumber
	 */
	public void updateOverallApplicationSummaryStatus(long dsaApplicationNumber) {

		Optional<DSAApplicationsMade> application = applicationsMadeRepository.findById(dsaApplicationNumber);
		if (application.isPresent()) {
			DSAApplicationsMade dsaApplicationsMade = application.get();
			List<DSAApplicationSectionStatus> sectionStatusList = dsaApplicationsMade.getDsaApplicationSectionStatus();
			List<DSAApplicationSectionStatus> list = sectionStatusList.stream()
					.filter(section -> !section.getSectionStatus().equals(SectionStatus.COMPLETED))
					.collect(Collectors.toList());
			int nonCompletedApplictionsSize = list.size();
			
			boolean canIgnoreSectionsCompleteCheck = false;
			if (nonCompletedApplictionsSize > 0) {
				List<Section> nonCompletedSections = list.stream().map(DSAApplicationSectionStatus::getSectionCode).collect(Collectors.toList());
				canIgnoreSectionsCompleteCheck = nonCompletedSections.contains(Section.ABOUT_STUDENT) || nonCompletedSections.contains( Section.ADDITIONAL_INFO);
			}
			
			if (nonCompletedApplictionsSize == 0 || canIgnoreSectionsCompleteCheck) {
				updateStatus(dsaApplicationsMade);
			}

		} else {
			logger.error("NO application found for dsaApplicationNumber {} ", dsaApplicationNumber);
			throw new IllegalArgumentException("No Application found for dsaApplicationNumber:" + dsaApplicationNumber);
		}
	}

	private void updateStatus(DSAApplicationsMade dsaApplicationsMade) {
		dsaApplicationsMade.setApplicationSummaryStatus(ApplicationSummaryStatus.COMPLETED);
		dsaApplicationsMade.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		dsaApplicationsMade.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		applicationsMadeRepository.save(dsaApplicationsMade);
		logger.info("Over all application status updated successfully to Competed");
	}

	private void resetApplicationStatus(DSAApplicationsMade dsaApplicationsMade) {
		dsaApplicationsMade.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);
		dsaApplicationsMade.setOverallApplicationStatus(OverallApplicationStatus.STARTED);
		dsaApplicationsMade.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		dsaApplicationsMade.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		applicationsMadeRepository.save(dsaApplicationsMade);
		logger.info("Over all application status reset successfully");
	}

	/**
	 * Get the section status
	 *
	 * @param dsaApplicationNumber
	 * @param section
	 * @return status of the section
	 */
	public ApplicationSectiponStatusVO getApplicationSectionStatus(long dsaApplicationNumber, Section section) {
		DSAApplicationSectionStatus sectionStatus = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, section);
		ApplicationSectiponStatusVO status = new ApplicationSectiponStatusVO();
		status.setDsaApplicationNumber(sectionStatus.getDsaApplicationNumber());
		status.setStudentReferenceNumber(sectionStatus.getStudentReferenceNumber());
		status.setSectionPart(sectionStatus.getSectionPart());
		status.setSectionCode(sectionStatus.getSectionCode());
		status.setSectionStatus(sectionStatus.getSectionStatus());
		status.setLastUpdatedBy(sectionStatus.getLastUpdatedBy());
		return status;
	}

	public void setSectionStatus(long dsaApplicationNumber, Section section, SectionStatus status) {
		DSAApplicationSectionStatus appSectionInDB = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, section);
		String statusCodeReceived = status.getCode();
		logger.info("Before:: For dsa application number {} the Application part section {} status {}",
				dsaApplicationNumber, section.name(), statusCodeReceived);
		if (appSectionInDB != null) {
			updateStatus(dsaApplicationNumber, status, appSectionInDB);
		} else {
			logger.error("No Application found for dsaApplicationNumber {} ", dsaApplicationNumber);
			throw new IllegalArgumentException("No Application found for dsaApplicationNumber:" + dsaApplicationNumber);
		}
	}

	public void resetStudentAndAdvisorDeclarations(long dsaApplicationNumber) throws IllegalAccessException {
		DSAApplicationSectionStatus advisorSectionInDB = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, ADVISOR_DECLARATION);
		if (!advisorSectionInDB.getSectionStatus().equals(NOT_STARTED)) {
			updateStatus(dsaApplicationNumber, SectionStatus.NOT_STARTED, advisorSectionInDB);
		}

		DSAApplicationSectionStatus studentSectionInDB = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, STUDENT_DECLARATION);
		if (!studentSectionInDB.getSectionStatus().equals(NOT_STARTED)) {
			updateStatus(dsaApplicationNumber, SectionStatus.NOT_STARTED, studentSectionInDB);
		}
	}

	/**
	 * To update the section status
	 *
	 * @param dsaApplicationNumber number
	 * @param section              given section
	 * @param status               given status
	 * @throws IllegalAccessException
	 */
	public void updateSectionStatus(long dsaApplicationNumber, Section section, SectionStatus status)
			throws IllegalAccessException {
		DSAApplicationSectionStatus appSectionInDB = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, section);
		String statusCodeReceived = status.getCode();
		logger.info("Before:: For dsa application number {} the Application part section {} status {}",
				dsaApplicationNumber, section.name(), statusCodeReceived);
		if (appSectionInDB != null) {
			int newRank = status.getRank();
			int rankInDB = appSectionInDB.getSectionStatus().getRank();
			logger.info("new Rank : {}, rank in DB: {}", newRank, rankInDB);
			if (section.equals(Section.ALLOWANCES)) {
				boolean isCompleted = appSectionInDB.getSectionStatus().equals(SectionStatus.COMPLETED);
				if (isCompleted) {
					boolean allowancesCompleted = findApplication(dsaApplicationNumber,
							appSectionInDB.getStudentReferenceNumber()).isAllAllowancesCompleted();
					if (!allowancesCompleted) {
						updateStatus(dsaApplicationNumber, status, appSectionInDB);
						setNeedsAssessmentStatus(dsaApplicationNumber, SectionStatus.CANNOT_START_YET);
					}

				} else {
					checkRankAndUpdate(dsaApplicationNumber, status, appSectionInDB, newRank, rankInDB);
				}
			} else if (section.equals(Section.NEEDS_ASSESSMENT_FEE)) {
				ApplicationResponse applicationResponse = findApplication(dsaApplicationNumber,
						appSectionInDB.getStudentReferenceNumber());

				boolean allowancesCompleted = applicationResponse.isAllAllowancesCompleted();

				if (!allowancesCompleted) {
					updateStatus(dsaApplicationNumber, SectionStatus.CANNOT_START_YET, appSectionInDB);
				} else {
					if (applicationResponse.getAssessmentFeeList().isEmpty()) {
						checkRankAndUpdate(dsaApplicationNumber, status, appSectionInDB, newRank, rankInDB);
					} else { 
						setNeedsAssessmentStatus(dsaApplicationNumber, SectionStatus.COMPLETED);
					}
				}
			} 	else if (section.equals(Section.ADDITIONAL_INFO)) {
				updateStatus(dsaApplicationNumber, status, appSectionInDB);
			} else {
				checkRankAndUpdate(dsaApplicationNumber, status, appSectionInDB, newRank, rankInDB);
			}

		} else {
			logger.error("No Application found for dsaApplicationNumber {} ", dsaApplicationNumber);
			throw new IllegalArgumentException("No Application found for dsaApplicationNumber:" + dsaApplicationNumber);
		}
	}

	private void setNeedsAssessmentStatus(long dsaApplicationNumber, SectionStatus status) {

		DSAApplicationSectionStatus assessmentFeeSection = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE);
		updateStatus(dsaApplicationNumber, status, assessmentFeeSection);
	}

	private void setNeedsAssessmentFeeCannotStart(long dsaApplicationNumber) {
		DSAApplicationSectionStatus assessmentFeeSection = applicationSectionStatusRepository
				.findByDsaApplicationNumberAndSectionCode(dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE);

		updateStatus(dsaApplicationNumber, SectionStatus.CANNOT_START_YET, assessmentFeeSection);
	}

	private void checkRankAndUpdate(long dsaApplicationNumber, SectionStatus status,
			DSAApplicationSectionStatus appSectionInDB, int newRank, int rankInDB) {
		if (newRank > rankInDB) {
			updateStatus(dsaApplicationNumber, status, appSectionInDB);
		}
	}

	private void updateStatus(long dsaApplicationNumber, SectionStatus status,
			DSAApplicationSectionStatus appSectionInDB) {
		appSectionInDB.setSectionStatus(status);
		appSectionInDB.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		appSectionInDB.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		applicationSectionStatusRepository.save(appSectionInDB);
		logger.info("AFTER:: For dsa application number {} the Application part section {} status {}",
				dsaApplicationNumber, appSectionInDB.getSectionCode().name(), appSectionInDB.getSectionStatus());
	}

	private DSAApplicationSectionStatus buildStatusFor(Section applicationSection, SectionStatus sectionStatus,
			long dsaApplicationNumber, long studentReferenceNumber, String userId, Timestamp timestamp) {
		DSAApplicationSectionStatus status = new DSAApplicationSectionStatus();
		status.setStudentReferenceNumber(studentReferenceNumber);
		status.setSectionCode(applicationSection);

		status.setSectionPart(applicationSection.getSectionPart());
		status.setSectionStatus(sectionStatus);
		status.setDsaApplicationNumber(dsaApplicationNumber);
		status.setCreatedBy(userId);
		status.setCreatedDate(timestamp);
		status.setLastUpdatedBy(userId);
		status.setLastUpdatedDate(timestamp);
		return status;
	}

	private void setBankAccountDetails(ApplicationResponse resp, DSAApplicationsMade applicationsMade) {
		List<DSAApplicationBankAccount> bankAccounts = applicationsMade.getBankAccounts();
		if (bankAccounts != null && !bankAccounts.isEmpty()) {
			resp.setBankDetails(ServiceUtil.populateBankDetailsVO(bankAccounts.get(0)));
		} else {
			resp.setBankDetails(new BankAccountVO());
		}
	}

	private void setApplicationFullySubmitted(ApplicationResponse applicationResponse,
			DSAApplicationsMade applicationsMade) {
		if (applicationResponse.isStudentDeclarationCompleted()) {
			DSAApplicationPDF applicationPDF = applicationsMade.getDsaApplicationPDF();
			boolean isFullySubmitted = applicationPDF != null;
			logger.info("Application is fully submitted {}", isFullySubmitted);
			applicationResponse.setApplicationFullySubmitted(isFullySubmitted);
		}
	}

	public void delete(long dsaApplicationNumber) {
		applicationsMadeRepository.deleteById(dsaApplicationNumber);
	}

}
