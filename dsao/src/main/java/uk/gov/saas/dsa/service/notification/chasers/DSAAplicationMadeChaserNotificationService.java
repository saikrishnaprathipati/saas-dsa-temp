package uk.gov.saas.dsa.service.notification.chasers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DSAEmailNotification;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.model.EmailNotificationType;
import uk.gov.saas.dsa.model.SectionStatusResponse;
import uk.gov.saas.dsa.service.*;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.service.notification.NotificationHelperService;
import uk.gov.saas.dsa.service.notification.NotificationUtil;
import uk.gov.saas.dsa.service.notification.NotificationVO;
import uk.gov.saas.dsa.vo.ApplicationSectiponStatusVO;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.sql.Date;
import java.util.*;

import static uk.gov.saas.dsa.model.Section.ADVISOR_DECLARATION;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.*;

@Service
public class DSAAplicationMadeChaserNotificationService {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final Long batchSize;
	private final Long batchDelay;
	private Long batchCounter;

	private final DSAEmailConfigProperties dsaEmailConfigProperties;
	private final NotificationHelperService emailDBHelperService;
	private final ApplicationService applicationService;
	private final FindStudentService findStudentService;
	private final EmailSenderService emailSenderService;
	private final ConfigDataService configDataService;
	private final AdvisorLookupService advisorLookupService;
	private final DSAApplicationDeletionServcie deleteService;

	public DSAAplicationMadeChaserNotificationService(DSAEmailConfigProperties emailProperties,

													  NotificationHelperService notificationsRepo, ApplicationService applicationService,
													  EmailSenderService emailSenderService, ConfigDataService configDataService,
													  FindStudentService findStudentService, AdvisorLookupService advisorLookupService,
													  DSAApplicationDeletionServcie deleteService) {

		this.dsaEmailConfigProperties = emailProperties;
		this.emailDBHelperService = notificationsRepo;
		this.applicationService = applicationService;
		this.emailSenderService = emailSenderService;
		this.configDataService = configDataService;
		this.findStudentService = findStudentService;
		this.advisorLookupService = advisorLookupService;
		this.batchSize = dsaEmailConfigProperties.getBatchSize();
		this.batchDelay = dsaEmailConfigProperties.getBatchDelay();
		this.batchCounter = dsaEmailConfigProperties.getBatchCounter();
		this.deleteService = deleteService;

	}

	public void initEmailChaserNotificationProcess() throws IllegalAccessException {

		int currentActiveSession = configDataService.getCurrentActiveSession();
		processEmailChaserNotifications(currentActiveSession);

		int previousSession = currentActiveSession-1;
		processEmailChaserNotifications(previousSession);
	}

	private void processEmailChaserNotifications(int sessionCode) throws IllegalAccessException {
		List<DSAApplicationsMade> list = applicationService
				.findAllAdvisorDeclarationCompletedAndNotSubmittedByStudent(sessionCode);

		Set<NotificationVO> uniqueFailedNotifications = getAllFailedNotifications(emailDBHelperService,
				sessionCode, getEmailChaserTypes());
		logger.info("Chasers uniqueFailedNotifications {} for session {}", uniqueFailedNotifications, sessionCode);
		Set<NotificationVO> newNotifications = getAllNonSubmittedNewNotifications(list, sessionCode);
		logger.info("Chasers newNotifications {} for session {}", newNotifications, sessionCode);

		Set<NotificationVO> allNotificationsToProcess = new HashSet<>();
		allNotificationsToProcess.addAll(uniqueFailedNotifications);
		allNotificationsToProcess.addAll(newNotifications);
		logger.info("Chasers allNotificationsToProcess {} for session {} ", newNotifications, sessionCode);
		for (NotificationVO notification : allNotificationsToProcess) {
			Date dateAndTime = new Date(Calendar.getInstance().getTimeInMillis());
			try {
				DSAApplicationsMade dsaApplicationsMade = getDSAApplicationsMade(
						notification.getStudentReferenceNumber(), list);

				if (dsaApplicationsMade != null) {
					processChaserNotification(notification, dsaApplicationsMade, dateAndTime);
				}
			} catch (Exception e) {
				processCatch(emailDBHelperService, notification, dateAndTime, e);

			}

		}
	}

	private Set<NotificationVO> getAllNonSubmittedNewNotifications(List<DSAApplicationsMade> list,
																   int currentActiveSession) {

		Set<NotificationVO> newNotifications = new HashSet<>();

		for (DSAApplicationsMade applicationMade : list) {
			EmailNotificationType type = checkTheNotificationEligibility(applicationMade);
			boolean isNotificationSuccesfullySent = false;
			if (type != null) {
				logger.info("Eligible chaser type for stud {} is {}", applicationMade.getStudentReferenceNumber(),
						type);
				isNotificationSuccesfullySent = isEmailAlreadySentSuccessfully(currentActiveSession,
						applicationMade.getStudentReferenceNumber(), type);
			}
			if (type != null && !isNotificationSuccesfullySent) {
				newNotifications.add(toNotificationVO(applicationMade.getStudentReferenceNumber(), type,
						currentActiveSession, NotificationUtil.BATCH_PROGRAM));
			}

		}
		logger.info("non submitted new notifications {}", newNotifications);
		return newNotifications;
	}

	private EmailNotificationType checkTheNotificationEligibility(DSAApplicationsMade applicationMade) {
		EmailNotificationType type = null;
		if (applicationMade != null) {

			SectionStatusResponse advisorDeclaration = ServiceUtil.getApplicationSectionResponse(ADVISOR_DECLARATION,
					applicationMade.getDsaApplicationSectionStatus());
			type = getEligibleNotificationType(advisorDeclaration.getLastUpdatedDate());
			long studentReferenceNumber = applicationMade.getStudentReferenceNumber();
			if (type == null) {
				logger.info("DSA Application stud_ref_no {} is not yet eligible to process the chaser notification",
						studentReferenceNumber);
			} else {
				logger.info("DSA Application stud_ref_no {} is processing the for notification: {}",
						studentReferenceNumber, type);
			}
		}
		return type;
	}

	private void processChaserNotification(NotificationVO notificationVO, DSAApplicationsMade applicationsMade,
										   Date dateAndTime) throws IllegalAccessException {
		logger.info("Processing the notification {}", notificationVO);

		try {
			logger.info("batchCounter before execution  {}", batchCounter);
			if (batchSize != null && batchDelay != null) { // if no batch config don't wait
				if (batchCounter % batchSize == 0 && batchCounter != 0) {
					logger.info("sleep {} start", batchDelay);
					Thread.sleep(batchDelay);
					logger.info("sleep {} end", batchDelay);
				}
				batchCounter++;

				logger.info("-sleep {} end", batchDelay);

				if (applicationsMade != null) {

					processNotification(notificationVO, applicationsMade, dateAndTime);

				} else {
					logger.info("NO DSA Applications exists for stud ref no {}",
							notificationVO.getStudentReferenceNumber());
				}

			}
		} catch (Exception e) {
			logger.error("Batch Execution exception {}", e.getMessage());
			processCatch(emailDBHelperService, notificationVO, dateAndTime, e);
		}
		logger.info("BatchCounter After execution {}", batchCounter);
	}

	private void processNotification(NotificationVO notificationVO, DSAApplicationsMade applicationsMade,
									 Date dateAndTime) throws IllegalAccessException {
		long dsaApplicationNumber = applicationsMade.getDsaApplicationNumber();

		List<DSAEmailNotification> existingNotifications = getExistingNotifications(emailDBHelperService,
				notificationVO);
		boolean isNotificationNotSent = isNotificationNotSentSentEarlier(existingNotifications);
		boolean isNotificationFailedEarlier = NotificationUtil
				.isFailedToSendTheNotificationEarlier(existingNotifications);

		if (isNotificationNotSent) {
			processForEmailNotification(notificationVO, dsaApplicationNumber);
			processForApplciationStatusUpdate(applicationService, notificationVO, applicationsMade, dateAndTime);
			boolean notDeleted = deleteDSAFootprint(notificationVO, applicationsMade);
			if (notDeleted) {
				if (isNotificationFailedEarlier) {
					updateExistingFailureToSuccess(emailDBHelperService, notificationVO, dateAndTime);
				} else {
					addNewSuccessNotification(emailDBHelperService, notificationVO, dateAndTime);
				}
			}
		}
	}

	private void processForEmailNotification(NotificationVO notificationVO, long dsaApplicationNumber)
			throws IllegalAccessException {
		if (notificationVO.getNotificationType().isSystemGeneratedNotification()) {
			sendEmail(dsaApplicationNumber, notificationVO);
		}
	}

	private boolean deleteDSAFootprint(NotificationVO notificationVO, DSAApplicationsMade applicationsMade) {
		boolean notDeleted = true;
		if (notificationVO.getNotificationType().equals(EmailNotificationType.DSA_91_DAY_DELETION)) {

			deleteService.deleteDSAAplicationFootPrints(notificationVO.getSessionCode(),
					applicationsMade.getDsaApplicationNumber(), applicationsMade.getStudentReferenceNumber());
			notDeleted = false;
		}
		return notDeleted;
	}

	private void sendEmail(long dsaApplicationNumber, NotificationVO notificationVO) throws IllegalAccessException {
		logger.info("Sending email notification for {}", notificationVO);
		EmailNotificationType notificationType = notificationVO.getNotificationType();
		Map<String, Object> modelMap = buildModelMapForEmail(findStudentService, notificationVO);
		for (String userType : notificationVO.getNotificationType().getSendTo()) {
			String htmlTemplatePath = htmlTemplatepath(NotificationUtil.CHASER_EMAILS,
					notificationType.getEmailTemplate(), userType);
			String emailSubject = deriveEmailSubject(userType, notificationType);
			String toEmail = null;
			if (userType.equals(DSAConstants.STUDENT)) {
				Stud stud = findStudentService.findStud(notificationVO.getStudentReferenceNumber());

				toEmail = stud.getEmailAddress();

				final StudentPersonalDetails studentPersonalDetails = findStudentService
						.findStudentPersonDetailsStudByRefNumber(notificationVO.getStudentReferenceNumber());
				String userId = studentPersonalDetails.getUserId();
				populateStudentSignInURL(dsaEmailConfigProperties, userId, notificationVO.getStudentReferenceNumber(),
						modelMap);

			}
			if (userType.equals(DSAConstants.HEI_TEAM)) {
				ApplicationSectiponStatusVO status = applicationService
						.getApplicationSectionStatus(dsaApplicationNumber, ADVISOR_DECLARATION);
				String advisorEmail = status.getLastUpdatedBy();
				DsaAdvisor advisor = advisorLookupService.findByEmail(advisorEmail);
				toEmail = advisor.getTeamEmail();
				modelMap.put(DSAConstants.HEI_NAME, advisor.getInstitution());
				populateAdvisorSignInURL(dsaEmailConfigProperties, modelMap);
			}

			emailSenderService.sendEmailNotification(new String[]{toEmail}, new String[]{}, emailSubject, htmlTemplatePath, modelMap);
		}

	}

	private boolean isEmailAlreadySentSuccessfully(int sessionCode, long studentRefNo, EmailNotificationType type) {
		return emailDBHelperService.isNotificationAlreadyProcessed(sessionCode, studentRefNo, type);

	}

	private DSAApplicationsMade getDSAApplicationsMade(long studRefNo, List<DSAApplicationsMade> list)
			throws IllegalAccessException {
		DSAApplicationsMade dsaApplicationsMade = null;

		Optional<DSAApplicationsMade> application = list.stream()
				.filter(t -> t.getStudentReferenceNumber() == studRefNo).findFirst();
		if (application.isPresent()) {
			dsaApplicationsMade = application.get();
		}
		if (dsaApplicationsMade == null && !list.isEmpty()) {
			throw new IllegalAccessException("No DSA Application found for studRefNo: " + studRefNo);
		}
		return dsaApplicationsMade;
	}
}