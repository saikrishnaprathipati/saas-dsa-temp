package uk.gov.saas.dsa.service.notification.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DSAEmailNotification;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.domain.readonly.DSAApplicationStatus;
import uk.gov.saas.dsa.domain.readonly.DSASTEPSApplication;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.model.ApplicationSummaryStatus;
import uk.gov.saas.dsa.model.EmailNotificationType;
import uk.gov.saas.dsa.persistence.readonly.DSASTEPSApplicationRepository;
import uk.gov.saas.dsa.persistence.refdata.DSAApplicationStatusRepository;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.award.DSAAwardService;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.service.notification.NotificationHelperService;
import uk.gov.saas.dsa.service.notification.NotificationUtil;
import uk.gov.saas.dsa.service.notification.NotificationVO;
import uk.gov.saas.dsa.vo.ApplicationSectiponStatusVO;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.saas.dsa.model.EmailNotificationType.STEPS_APPLICATION_WITHDRAWN;
import static uk.gov.saas.dsa.model.EmailNotificationType.STEPS_AWARDED;
import static uk.gov.saas.dsa.model.EmailNotificationType.STEPS_PENDED_WITH_SAAS;
import static uk.gov.saas.dsa.model.EmailNotificationType.STEPS_RECEIVED;
import static uk.gov.saas.dsa.model.EmailNotificationType.STEPS_REJECTED;
import static uk.gov.saas.dsa.model.OverallApplicationStatus.SUBMITTED;
import static uk.gov.saas.dsa.model.Section.ADVISOR_DECLARATION;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.STEPS_NOTIFICATION_EMAILS;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.addNewSuccessNotification;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.buildModelMapForEmail;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.getAllFailedNotifications;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.getStepsEmailNotificationTypes;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.getExistingNotifications;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.htmlTemplatepath;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.isFailedToSendTheNotificationEarlier;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.isNotificationNotSentSentEarlier;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.populateAdvisorSignInURL;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.populateStudentSignInURL;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.processCatch;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.processForApplciationStatusUpdate;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.toNotificationVO;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.updateExistingFailureToSuccess;

@Service
public class DSAStepsBatchNotificationService {

	private static final String APPLCIATION_WITHDRAWN = "APPLCIATION WITHDRAWN";
	private static final String PENDED_WITH_HEI_STUDENT = "PENDED WITH HEI/STUDENT";
	private static final String AWARDED = "AWARDED";
	private static final String REJECTED = "REJECTED";
	private static final String RECEIVED = "RECEIVED";
	private static final String PENDED_WITH_SAAS = "PENDED WITH SAAS";
	private static final String IS_ONLINE_YES = "Y";

	private Long batchSize;
	private Long batchDelay;
	private Long batchCounter;
	private String advisorEmail;

	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAEmailConfigProperties dsaEmailConfigProperties;
	private NotificationHelperService emailDBHelperService;
	private DSAApplicationStatusRepository applicationStatusRepository;
	private DSASTEPSApplicationRepository dsaSTEPSApplicationRepository;

	private ApplicationService applicationService;
	private FindStudentService findStudentService;
	private EmailSenderService emailSenderService;
	private ConfigDataService configDataService;
	private AdvisorLookupService advisorLookupService;
	private DSAAwardService dsaAwardService;

	public DSAStepsBatchNotificationService(DSAEmailConfigProperties emailProperties,

			NotificationHelperService notificationsRepo, DSASTEPSApplicationRepository dsaSTEPSApplicationRepository,
			DSAApplicationStatusRepository applicationStatusRepository, ApplicationService applicationService,
			EmailSenderService emailSenderService, ConfigDataService configDataService,
			FindStudentService findStudentService, AdvisorLookupService advisorLookupService, DSAAwardService dsaAwardService) {

		this.dsaEmailConfigProperties = emailProperties;
		this.emailDBHelperService = notificationsRepo;
		this.applicationStatusRepository = applicationStatusRepository;
		this.dsaSTEPSApplicationRepository = dsaSTEPSApplicationRepository;

		this.applicationService = applicationService;
		this.emailSenderService = emailSenderService;
		this.configDataService = configDataService;
		this.findStudentService = findStudentService;
		this.batchSize = dsaEmailConfigProperties.getBatchSize();
		this.batchDelay = dsaEmailConfigProperties.getBatchDelay();
		this.batchCounter = dsaEmailConfigProperties.getBatchCounter();

		this.advisorLookupService = advisorLookupService;
		this.dsaAwardService = dsaAwardService;

		this.advisorEmail = "";

	}

    public void initialiseNotificationProcess() throws IllegalAccessException {

		int currentActiveSession = configDataService.getCurrentActiveSession();
		processNotifications(currentActiveSession);

		int previousSession = currentActiveSession-1;
		processNotifications(previousSession);
	}

	private void processNotifications(Integer session) throws IllegalAccessException {
		Set<NotificationVO> uniqueFailedNotifications = getAllFailedNotifications(emailDBHelperService,
				session, getStepsEmailNotificationTypes());
		logger.info("Steps batch uniqueFailedNotifications {}", uniqueFailedNotifications);
		Set<NotificationVO> newNotifications = getAllStatusChangeNotificationsFromSteps(session);
		
		logger.info("Steps batch newNotifications {}", newNotifications);
		Set<NotificationVO> allNotificationsToProcess = new HashSet<NotificationVO>();
		allNotificationsToProcess.addAll(uniqueFailedNotifications);
		allNotificationsToProcess.addAll(newNotifications);
		logger.info("Steps batch allNotificationsToProcess {}", allNotificationsToProcess);
		for (NotificationVO notification : updateRecordsWithLastUpdatedBy(session,
				allNotificationsToProcess)) {
			logger.info("Executing batchSize  of {} Start", batchSize);
			sendEmailAndUpdateStatus(notification);
			logger.info("Executing batchSize  of {} End", batchSize);
		}
	}

	private void sendEmailAndUpdateStatus(NotificationVO notificationVO) throws IllegalAccessException {
		logger.info("Processing the notification {}", notificationVO);
		Date dateAndTime = new Date(Calendar.getInstance().getTimeInMillis());
		try {
			logger.info("batchCounter before execution  {}", batchCounter);
			if (batchSize != null && batchDelay != null) { // if no batch config don't wait
				if (batchCounter % batchSize == 0 && batchCounter != 0) {
					logger.info("sleep {} start", batchDelay);
					Thread.sleep(batchDelay);
					logger.info("sleep {} end", batchDelay);
				}
				batchCounter++;
				
				DSAApplicationsMade applicationsMade = applicationService
						.getDSAApplciationByStudRefNoAndSessionCode(notificationVO.getStudentReferenceNumber(), notificationVO.getSessionCode());
				
				logger.info(
						"applciation with stud ref: {}, dsa id: {}, sessioncode: {},  overall status: {}, summary status: {} ",
						applicationsMade.getStudentReferenceNumber(), applicationsMade.getDsaApplicationNumber(),
						applicationsMade.getSessionCode(), applicationsMade.getOverallApplicationStatus(),
						applicationsMade.getApplicationSummaryStatus());
				
				logger.info("sleep {} end", batchDelay);
				
				if (applicationsMade != null) {
					if (!isIncompleteApplication(applicationsMade)) {
						processNotification(notificationVO, applicationsMade, dateAndTime);
					} else {
						logger.info("Incomplete applciation found for student reference no: {} with status: {}",
								applicationsMade.getStudentReferenceNumber(),
								applicationsMade.getApplicationSummaryStatus());

					}
				} else {
					logger.info("NO DSA Applications exists for stud ref no {}",
							notificationVO.getStudentReferenceNumber());
				}

			}
		} catch (Exception e) {
			logger.error("Batch Exexution exception {}", e.getMessage());
			processCatch(emailDBHelperService, notificationVO, dateAndTime, e);
		}
		logger.info("BatchCounter After execution {}", batchCounter);
	}

	private void processNotification(NotificationVO notificationVO, DSAApplicationsMade applicationsMade,
			Date dateAndTime) throws Exception {
		List<DSAEmailNotification> existingNotifications = getExistingNotifications(emailDBHelperService,
				notificationVO);
		boolean isNotificationNotSent = isNotificationNotSentSentEarlier(existingNotifications);
		boolean isNotificationFailedEarlier = isFailedToSendTheNotificationEarlier(existingNotifications);
		boolean topUpApplication = isTopUpApplication(notificationVO, existingNotifications);
		if (isNotificationNotSent || topUpApplication) {
			processForEmailNotification(notificationVO, applicationsMade.getDsaApplicationNumber());
			processForApplciationStatusUpdate(applicationService, notificationVO, applicationsMade, dateAndTime);
			if (isNotificationFailedEarlier) {
				updateExistingFailureToSuccess(emailDBHelperService, notificationVO, dateAndTime);
			} else {
				if (!topUpApplication) {
					addNewSuccessNotification(emailDBHelperService, notificationVO, dateAndTime);
				} else {
					NotificationVO notification = setExistingNotificationToUpdate(notificationVO, existingNotifications);
					
					NotificationUtil.updateExistingSuccess(emailDBHelperService, notification, dateAndTime);
					
				}
			}
		}
	}

	private NotificationVO setExistingNotificationToUpdate(NotificationVO notificationVO,
			List<DSAEmailNotification> existingNotifications) {
		logger.info("setNotificationForSuccess");
		logger.info("notificationVO {}", notificationVO);
		logger.info("existingNotifications {}", existingNotifications);
		DSAEmailNotification dsaEmailNotification = existingNotifications.get(0);
		NotificationVO vo = NotificationUtil.toNotificationVOWithIdAndStatus(dsaEmailNotification.getId(),
				notificationVO.getStudentReferenceNumber(), notificationVO.getNotificationType(),
				notificationVO.getSessionCode());
		logger.info("NotificationVO after setting to update the existing notification {}", existingNotifications);
		return vo;
	}
	
	private boolean isTopUpApplication(NotificationVO notificationVO,
			List<DSAEmailNotification> existingNotifications) {

		boolean isTopupApplication = false;
		boolean isSuccessNotificationSentEarlier = NotificationUtil
				.isNotificationSentSentEarlier(existingNotifications);
		if (isSuccessNotificationSentEarlier) {
			logger.info("SuccessNotificationSentEarlier for notificationVO: {}", notificationVO);
			DSAApplicationsMade application = applicationService
					.findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(
							notificationVO.getStudentReferenceNumber(), notificationVO.getSessionCode());
			isTopupApplication = application.getOverallApplicationStatus().equals(SUBMITTED);
			if (isTopupApplication) {
				logger.info(
						"Found Top-up applciation with stud ref: {}, dsa id: {}, sessioncode: {},  overall status: {}, summary status: {} ",
						application.getStudentReferenceNumber(), application.getDsaApplicationNumber(),
						application.getSessionCode(), application.getOverallApplicationStatus(),
						application.getApplicationSummaryStatus());
			}
		}
		return isTopupApplication;
	}
	
 
	private void processForEmailNotification(NotificationVO notificationVO, long dsaApplicationNumber)
			throws Exception {
		if (notificationVO.getNotificationType().isSystemGeneratedNotification()) {
			sendEmail(dsaApplicationNumber, notificationVO);
		}
	}

	private boolean isIncompleteApplication(DSAApplicationsMade applicationsMade) {
		return applicationsMade != null && applicationsMade.getApplicationSummaryStatus()
				.equals(ApplicationSummaryStatus.APPLICATION_INCOMPLETE);
	}
	

	private Set<NotificationVO> getAllStatusChangeNotificationsFromSteps(int currentActiveSession) {

		DSAEmailNotification latestSuccess = emailDBHelperService
				.findFirstByOrderBySuccessDateIsNotNullDesc(getStepsEmailNotificationTypes());
		List<DSASTEPSApplication> stepsRecordsToProcess = new ArrayList<DSASTEPSApplication>();
		if (latestSuccess == null) {
			stepsRecordsToProcess = getAllStepsApplicationChanges(currentActiveSession);
			logger.info("1 Total {} Steps applciations for session {} are: ", stepsRecordsToProcess.size(),
					currentActiveSession, stepsRecordsToProcess);
		} else {
			stepsRecordsToProcess = getAllStepsApplicationChangesAfterSuccessDate(currentActiveSession, latestSuccess);
			logger.info("2 Total {} Steps applciations for session {} are: ", stepsRecordsToProcess.size(),
					currentActiveSession, stepsRecordsToProcess);
		}

		Set<NotificationVO> newNotifications = filterNewNotifications(stepsRecordsToProcess);
		logger.info("All New Notifications count {} ", newNotifications.size(), newNotifications);
		logger.info("All New Notifications are {} ", newNotifications);
		return newNotifications;
	}

	private List<DSASTEPSApplication> getAllStepsApplicationChangesAfterSuccessDate(int currentActiveSession,
			DSAEmailNotification latestSuccess) {
		List<DSASTEPSApplication> listToProcess;

		logger.info("Getting the Steps applciations records after last success date {}",
				latestSuccess.getSuccessDate());
		listToProcess = dsaSTEPSApplicationRepository.findAllBySessionCodeAndIsOnlineAndLastUpdatedOnAfter(
				currentActiveSession, IS_ONLINE_YES, latestSuccess.getSuccessDate());
		logger.info("Steps applciations status updates after success date {}", listToProcess);
		return listToProcess == null ? new ArrayList<DSASTEPSApplication>() : listToProcess;
	}

	private List<DSASTEPSApplication> getAllStepsApplicationChanges(int currentActiveSession) {
		List<DSASTEPSApplication> listToProcess;
		logger.info("Theere are no emails ran earlier job hence getting all applications to process");

		listToProcess = dsaSTEPSApplicationRepository.findAllBySessionCodeAndIsOnline(currentActiveSession,
				IS_ONLINE_YES);
		
		return listToProcess == null ? new ArrayList<DSASTEPSApplication>() : listToProcess;
	}

	private Set<NotificationVO> updateRecordsWithLastUpdatedBy(int currentActiveSession,
			Set<NotificationVO> notifications) {
		Set<NotificationVO> finalSet = new HashSet<NotificationVO>();
		for (NotificationVO vo : notifications) {
			if (vo.getUpdatedBy() == null && isNotExist(finalSet, vo)) {
				logger.info("No updated by for this Notification {}", vo);
				NotificationVO notificationVO = toNotificationVO(vo.getStudentReferenceNumber(),
						vo.getNotificationType(), vo.getSessionCode(),
						dsaSTEPSApplicationRepository
								.findByStudentReferenceNumberAndSessionCodeAndIsOnline(vo.getStudentReferenceNumber(),
										currentActiveSession, IS_ONLINE_YES).stream().findFirst().get()
								.getLastUpdatedBy());

				finalSet.add(notificationVO);
			} else if (isNotExist(finalSet, vo)) {
				finalSet.add(vo);
			}
		}
		logger.info("finalSet {}", finalSet);
		return finalSet;
	}

	private boolean isNotExist(Set<NotificationVO> finalSet, NotificationVO vo) {

		long count = finalSet.stream().filter(n -> n.getNotificationType().equals(vo.getNotificationType())
				&& n.getStudentReferenceNumber() == vo.getStudentReferenceNumber()).count();

		return count == 0;
	}

	private Set<NotificationVO> filterNewNotifications(List<DSASTEPSApplication> listToProcess) {
		Set<NotificationVO> newNotificationsToProcess = new HashSet<NotificationVO>();
		for (DSASTEPSApplication stepsRecord : listToProcess) {
			EmailNotificationType notificationType = deriveNotificationType(getAplpciationStatuses(),
					stepsRecord.getDsaAppStatus());
			if (notificationType != null
					&& (notificationType.isSystemGeneratedNotification() || notificationType.isUpdateDSAStatus())) {

				NotificationVO notificationVO = toNotificationVO(stepsRecord.getStudentReferenceNumber(),
						notificationType, stepsRecord.getSessionCode(), stepsRecord.getLastUpdatedBy());
				logger.info("Adding Notification to process {}", notificationVO);
				newNotificationsToProcess.add(notificationVO);
			} else {
				logger.info("Not processing the STEPS applciation status update {}", stepsRecord);
			}

		}
		return newNotificationsToProcess;
	}

	private List<DSAApplicationStatus> getAplpciationStatuses() {
		List<DSAApplicationStatus> allStatuses = applicationStatusRepository.findAll();
		logger.info("All Steps Statuses {}", allStatuses);
		return allStatuses;
	}

	private EmailNotificationType deriveNotificationType(List<DSAApplicationStatus> allStatuses, int dsaAppStatus) {
		String status = allStatuses.stream().filter(t -> t.getDsaAppStatusId() == dsaAppStatus)
				.map(t -> t.getDsaAppStatus()).findFirst().get().toUpperCase();
		EmailNotificationType type = null;

		switch (status) {
		case AWARDED:
			type = STEPS_AWARDED;
			break;
		case REJECTED:
			type = STEPS_REJECTED;
			break;
		case RECEIVED:
			type = STEPS_RECEIVED;
			break;
		case PENDED_WITH_SAAS:
			type = STEPS_PENDED_WITH_SAAS;
			break;
		case PENDED_WITH_HEI_STUDENT:
			type = STEPS_PENDED_WITH_SAAS;
			break;
		case APPLCIATION_WITHDRAWN:
			type = STEPS_APPLICATION_WITHDRAWN;
			break;
		default:
			logger.info("Not processing the Steps status  [{}]", status);
			break;
		}
		logger.info("Derived stus from Steps status code [{}] to EmailNotificationType [{}]", status, type);
		return type;
	}

	private void sendEmail(long applicationNumber, NotificationVO notificationVO) throws IllegalAccessException {
		logger.info("Sending email for notification ", notificationVO);
		EmailNotificationType notificationType = notificationVO.getNotificationType();
		Map<String, Object> modelMap = buildModelMapForEmail(findStudentService, notificationVO);
		modelMap.put("STEPS_AWARD_STATUS", dsaAwardService
				.getAwardFundStatus(notificationVO.getStudentReferenceNumber(), notificationVO.getSessionCode()));
		for (String userType : notificationVO.getNotificationType().getSendTo()) {
			String[] toEmails = deriveEmailsList(applicationNumber, notificationVO, userType, modelMap);
			String[] ccEmails = new String[] {};
			String emailSubject = getEmailSubject(notificationType);
			String htmlTemplatePath = htmlTemplatepath(STEPS_NOTIFICATION_EMAILS, notificationType.getEmailTemplate(),
					userType);
			if (userType.equalsIgnoreCase(DSAConstants.ADVISOR)) {
				ccEmails = getCCEmails((String) modelMap.get(DSAConstants.HEI_TEAM_EMAIL));
				populateAdvisorSignInURL(dsaEmailConfigProperties, modelMap);
			}
			emailSenderService.sendEmailNotification(toEmails, ccEmails, emailSubject, htmlTemplatePath, modelMap);
		}
	}

	private String[] getCCEmails(String heiEmail) {
		List<String> ccList = new ArrayList<String>();
		if (heiEmail != null) {
			logger.info("******* Sending email to HEI team {}", heiEmail);
			ccList.add(heiEmail);
		}
		return Arrays.copyOf(ccList.toArray(), ccList.size(), String[].class);

	}

	private String getEmailSubject(EmailNotificationType notificationType) {
		return "DSA award decision";
	}

	private String[] deriveEmailsList(long dsaApplicationNumber, NotificationVO notificationVO, String userType,
			Map<String, Object> modelMap) throws IllegalAccessException {
		List<String> toList = new ArrayList<String>();

		if (userType.equalsIgnoreCase(DSAConstants.STUDENT)) {
			Stud stud = findStudentService.findStud(notificationVO.getStudentReferenceNumber());

			toList.add(stud.getEmailAddress());

			final StudentPersonalDetails studentPersonalDetails = findStudentService
					.findStudentPersonDetailsStudByRefNumber(notificationVO.getStudentReferenceNumber());
			String userId = studentPersonalDetails.getUserId();
			populateStudentSignInURL(dsaEmailConfigProperties, userId, notificationVO.getStudentReferenceNumber(),
					modelMap);
		}
		if (userType.equalsIgnoreCase(DSAConstants.ADVISOR)) {
			ApplicationSectiponStatusVO status = applicationService.getApplicationSectionStatus(dsaApplicationNumber,
					ADVISOR_DECLARATION);
			toList.add(status.getLastUpdatedBy());
			DsaAdvisor advisor = advisorLookupService.findByEmail(status.getLastUpdatedBy());
			modelMap.put(DSAConstants.HEI_TEAM_EMAIL, advisor.getTeamEmail());

			modelMap.put("ADVISOR_NAME", advisor.getFirstName() + " " + advisor.getLastName());

		}

		String[] stringArray = toList.stream().toArray(String[]::new);
		return stringArray;
	}

}