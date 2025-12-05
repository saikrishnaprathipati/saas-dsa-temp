package uk.gov.saas.dsa.service.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.StringUtils;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DSAEmailNotification;
import uk.gov.saas.dsa.domain.helpers.EncryptionHelper;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.model.EmailNotificationType;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.EmailTokenGenerator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.model.EmailNotificationType.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

public class NotificationUtil {
	public static final String CHASER_EMAILS = "chaserEmails/";
	public static final String STEPS_NOTIFICATION_EMAILS = "notificationEmails/";
	public static final String BATCH_PROGRAM = "BATCH_PROGRAM";
	private static final Logger logger = LogManager.getLogger(NotificationUtil.class);
	public static final String MAIL_TEMPLATES = "mail-templates/";
	private static final String STUDENT_SIGN_IN_URL = "STUDENT_SIGN_IN_URL";
	private static final String ADVISOR_SIGN_IN_URL = "ADVISOR_SIGN_IN_URL";

	private static final String STUDENT_APPLICATION_PATH = "/dsa/dsaApplication?token=";

	public static List<EmailNotificationType> getEmailChaserTypes() {
		return Arrays.asList(DSA_5_DAY_CHASER, DSA_10_DAY_CHASER, DSA_30_DAY_CHASER, DSA_83_DAY_CHASER,
				DSA_90_DAY_WITHDRAW, DSA_91_DAY_DELETION);
	}

	public static List<EmailNotificationType> getStepsEmailNotificationTypes() {
		return Arrays.asList(STEPS_REJECTED, STEPS_AWARDED, STEPS_PENDED_WITH_SAAS);
	}

	public static String htmlTemplatepath(String emailType, String notificationTemplate, String userType) {
		String stringToFormat = MAIL_TEMPLATES + "%s" + "%s" + "For" + "%s" + ".html";
		String emailTemplate = String.format(stringToFormat, emailType, notificationTemplate, userType);
		logger.info("Email html template is {}", emailTemplate);
		return emailTemplate;

	}

	public static String deriveEmailSubject(String userType, EmailNotificationType notificationType) {
		String subject;
		if (userType.equals(STUDENT)) {
			switch (notificationType) {
				case DSA_5_DAY_CHASER:
				case DSA_10_DAY_CHASER:
					subject = "A reminder to complete your DSA application";
					break;
				case DSA_30_DAY_CHASER:
				case DSA_83_DAY_CHASER:
					subject = "Important information about your DSA application";
					break;
				default:
					throw new IllegalArgumentException("No email subject for notification type " + notificationType);
			}
		} else if (userType.equals(HEI_TEAM)) {

			switch (notificationType) {


				case DSA_10_DAY_CHASER:
					subject = "DSA pending application";
					break;
				case DSA_30_DAY_CHASER:
					subject = "DSA pending application - action needed";
					break;
				case DSA_83_DAY_CHASER:
					subject = "DSA application scheduled for deletion";
					break;
				default:
					throw new IllegalArgumentException("No email subject for notification type " + notificationType);

			}
		} else {
			throw new IllegalArgumentException("No email subject for user type " + userType);
		}
		logger.info("email subject for {} is {}", notificationType, subject);
		return subject;
	}

	public static EmailNotificationType getEligibleNotificationType(Date lastUpdatedDate) {
		Optional<EmailNotificationType> first = getEmailChaserTypes().stream()
				.filter(t -> canProcessNotification(lastUpdatedDate, t)).findFirst();
		EmailNotificationType type = null;
		if (first.isPresent()) {
			type = first.get();
		}
		logger.info("Eligible notification type {}", type);
		return type;
	}

	public static boolean canProcessNotification(Date lastUpdatedDate, EmailNotificationType type) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int actualNotificationDays = notificationTypeToDate(type);

		int daysSinceAdvisorSubmitted = daysDifference(dateFormat.format(lastUpdatedDate),
				dateFormat.format(new Date()));

		boolean canProcessNotification = actualNotificationDays == daysSinceAdvisorSubmitted;
		if (type.equals(EmailNotificationType.DSA_91_DAY_DELETION)) {
			canProcessNotification = daysSinceAdvisorSubmitted >= actualNotificationDays;
		}

		logger.info("notification type {}, days since advisor completed their declaration {} canSendNotification {}",

				type, daysSinceAdvisorSubmitted, canProcessNotification);
		return canProcessNotification;
	}

	public static Set<NotificationVO> filterUniqueFailedNotifications(List<DSAEmailNotification> failures) {

		Set<NotificationVO> filteredFailures = failures.stream().map(NotificationUtil::toNotificationVOWithId)
				.collect(Collectors.toSet());

		logger.info("All failed  Notifications size: {} : {} ", failures.size(), failures);
		logger.info("Unique failed Notifications size: {} : {} ", filteredFailures.size(), filteredFailures);
		return filteredFailures;

	}

	public static NotificationVO toNotificationVOWithId(DSAEmailNotification dbNotification) {
		NotificationVO notificationVO = NotificationVO.builder().id(dbNotification.getId())
				.studentReferenceNumber(dbNotification.getStudentReferenceNumber())
				.notificationType(dbNotification.getNotificationType()).sessionCode(dbNotification.getSessionCode())
				.updatedBy(BATCH_PROGRAM).build();
		logger.info("notificationVO with ID - {}", notificationVO);
		return notificationVO;

	}
	
	public static NotificationVO toNotificationVOWithIdAndStatus(long notificationId, long studRef, EmailNotificationType type, int sessionCode ) {
		NotificationVO notificationVO = NotificationVO.builder().id(notificationId)
				.studentReferenceNumber(studRef)
				.notificationType(type).sessionCode(sessionCode)
				.updatedBy(BATCH_PROGRAM).build();
		logger.info("notificationVO with ID - {}", notificationVO);
		return notificationVO;

	}

	public static NotificationVO toNotificationVO(long studRefNo, EmailNotificationType type, int sessionCode,
												  String updatedBy) {

		NotificationVO notificationVO = NotificationVO.builder().studentReferenceNumber(studRefNo)
				.notificationType(type).sessionCode(sessionCode).updatedBy(updatedBy).build();
		logger.info("notificationVO -- {}", notificationVO);
		return notificationVO;
	}

	private static DSAEmailNotification toNotificationEntity(NotificationVO notificationVO, java.sql.Date dateAndTime,
															 String message) {
		DSAEmailNotification notification = new DSAEmailNotification();
		notification.setStudentReferenceNumber(notificationVO.getStudentReferenceNumber());
		notification.setNotificationType(notificationVO.getNotificationType());
		if (StringUtils.hasLength(message)) {
			notification.setFailureDate(dateAndTime);
			notification.setFailureReason(message);
		} else {
			notification.setSuccessDate(dateAndTime);
		}
		notification.setSessionCode(notificationVO.getSessionCode());
		logger.info("Notification Entity {}", notification);
		return notification;
	}

	private static int daysDifference(String date1, String date2) {

		LocalDate dt1 = LocalDate.parse(date1);
		LocalDate dt2 = LocalDate.parse(date2);
		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);
		logger.debug("original diffDays {}", diffDays);
		int absoluteVal = Math.abs((int) diffDays);
		logger.debug("original diffDays after getting the absolute val {}", diffDays);
		logger.info("Date difference between {}  and {} is {}", date1, date2, absoluteVal);
		return absoluteVal;
	}

	private static int notificationTypeToDate(EmailNotificationType type) {

		int days = -1;
		switch (type) {
			case DSA_5_DAY_CHASER:
				days = 5;
				break;
			case DSA_10_DAY_CHASER:
				days = 10;
				break;
			case DSA_30_DAY_CHASER:
				days = 30;
				break;
			case DSA_83_DAY_CHASER:
				days = 83;
				break;
			case DSA_90_DAY_WITHDRAW:
				days = 90;
				break;
			case DSA_91_DAY_DELETION:
				days = 91;
				break;
			default:
				break;
		}
		logger.debug("EmailNotificationType {}  to chaser days is {} ", type, days);
		return days;
	}

	public static void addNewSuccessNotification(NotificationHelperService emailDBHelperService,
												 NotificationVO notificationVO, java.sql.Date dateAndTime) {

		DSAEmailNotification notification = toNotificationEntity(notificationVO, dateAndTime, null);
		logger.info("Saving new email notification entry {}", notification);
		emailDBHelperService.save(notification);
	}

	public static void addNewFailureNotification(NotificationHelperService emailDBHelperService,
												 NotificationVO notificationVO, java.sql.Date dateAndTime, String message) {
		DSAEmailNotification notificationEntity = toNotificationEntity(notificationVO, dateAndTime, message);
		logger.info("Saving new failed email notification entry {}", notificationEntity);
		emailDBHelperService.save(notificationEntity);
	}

	public static void updateExistingFailureToSuccess(NotificationHelperService emailDBHelperService,
													  NotificationVO notificationVO, java.sql.Date dateAndTime) throws IllegalAccessException {
		logger.info("updateExistingFailureToSuccess {}", notificationVO);
		emailDBHelperService.updateFailureToSuccess(notificationVO, dateAndTime);
	}

	public static void updateExistingSuccess(NotificationHelperService emailDBHelperService,
			NotificationVO notificationVO, java.sql.Date dateAndTime) throws IllegalAccessException {
		logger.info("updateExistingSuccess {}", notificationVO);
		emailDBHelperService.updateSuccessNotification(notificationVO, dateAndTime);
	}
	
	public static void updateFailureNotification(NotificationHelperService emailDBHelperService,
												 NotificationVO notificationVO, java.sql.Date dateAndTime, String message) throws IllegalAccessException {
		logger.info("update existing Failure Notification {}", notificationVO);
		emailDBHelperService.updateFailureNotification(notificationVO, dateAndTime, message);

	}

	public static Set<NotificationVO> getAllFailedNotifications(NotificationHelperService emailDBHelperService,
																int currentActiveSession, List<EmailNotificationType> list) {

		List<DSAEmailNotification> failedNotifications = emailDBHelperService
				.findFailureNotifications(currentActiveSession, list);

		Set<NotificationVO> uniqueFailedNotifications = NotificationUtil
				.filterUniqueFailedNotifications(failedNotifications);
		logger.info("All existing unique failed Notification {}", uniqueFailedNotifications);
		return uniqueFailedNotifications;

	}

	public static List<DSAEmailNotification> getExistingNotifications(NotificationHelperService emailDBHelperService,
																	  NotificationVO notificationVO) {
		EmailNotificationType emailNotificationType = notificationVO.getNotificationType();

		long studentRefNo = notificationVO.getStudentReferenceNumber();
		List<DSAEmailNotification> existingNotifications = emailDBHelperService.getExistingNotificationsForStudent(
				notificationVO.getSessionCode(), studentRefNo, emailNotificationType);
		logger.info("All existingNotifications {}", existingNotifications);
		return existingNotifications;
	}
 
	public static boolean isNotificationNotSentSentEarlier(List<DSAEmailNotification> existingNotifications) {
		List<DSAEmailNotification> successList = getNotificationWasSentSuccessfullyInThePreviousRun(existingNotifications);
		boolean isEmailSentSuccesfully = successList.isEmpty();
		logger.info("isEmailSentSuccesfully in the previous run {}", isEmailSentSuccesfully);
		return isEmailSentSuccesfully;
	}

	public static boolean isNotificationSentSentEarlier(List<DSAEmailNotification> existingNotifications) {
		List<DSAEmailNotification> inThePreviousRun = getNotificationWasSentSuccessfullyInThePreviousRun(existingNotifications);
		logger.info("List of SUCCESS Notifications Sent Earlier {}", inThePreviousRun);
		boolean hasNotificationSentEarlier = !inThePreviousRun.isEmpty();
		return hasNotificationSentEarlier;
		
	}
	private static List<DSAEmailNotification> getNotificationWasSentSuccessfullyInThePreviousRun(List<DSAEmailNotification> existingNotifications) {
		List<DSAEmailNotification> successList = existingNotifications.stream().filter(t -> t.getSuccessDate() != null)
				.collect(Collectors.toList());
		if (successList == null) {
			successList = new ArrayList<DSAEmailNotification>();
		}
		logger.info("All SUCCESS notifications size {}", successList.size());
		logger.info("All SUCCESS notifications list {}", successList);
		return successList;
	}

	public static boolean isFailedToSendTheNotificationEarlier(List<DSAEmailNotification> existingNotifications) {
		List<DSAEmailNotification> failureList = existingNotifications.stream().filter(t -> t.getFailureDate() != null)
				.collect(Collectors.toList());
		boolean isEmailSentFailedEarlier = !failureList.isEmpty();
		logger.info("isEmailSentFailedEarlier {}", isEmailSentFailedEarlier);
		return isEmailSentFailedEarlier;
	}

	public static void processForApplciationStatusUpdate(ApplicationService applicationService,
														 NotificationVO notificationVO, DSAApplicationsMade applicationsMade, java.sql.Date dateAndTime)
			throws IllegalAccessException {
		if (notificationVO.getNotificationType().isUpdateDSAStatus()) {
			logger.info("Updating the dsa application status {} for ", notificationVO);
			applicationService.updateDSAAplicationStatus(notificationVO, applicationsMade, dateAndTime);
			logger.info("Application update status successful");

		}
	}

	public static void processCatch(NotificationHelperService emailDBHelperService, NotificationVO notification,
									java.sql.Date dateAndTime, Exception exception) throws IllegalAccessException {
		logger.info("Error while processing the notification {}", notification);
		exception.printStackTrace();
		String originalMsg = exception.getMessage();
		if (originalMsg == null || originalMsg.isEmpty()) {
			originalMsg = "Failed to process Notification: " + notification.toString();
		}

		String message = ServiceUtil.getCharactersFromString(originalMsg, 500);
		List<DSAEmailNotification> existingNotifications = NotificationUtil
				.getExistingNotifications(emailDBHelperService, notification);
		boolean isNotificationNotSent = NotificationUtil.isNotificationNotSentSentEarlier(existingNotifications);
		boolean isNotificationFailedEarlier = NotificationUtil
				.isFailedToSendTheNotificationEarlier(existingNotifications);
		if (isNotificationNotSent) {
			if (isNotificationFailedEarlier) {
				NotificationUtil.updateFailureNotification(emailDBHelperService, notification, dateAndTime, message);
			} else {

				NotificationUtil.addNewFailureNotification(emailDBHelperService, notification, dateAndTime, message);
			}
		}
		logger.info("Error while processing the notification {}, message: {}", notification, message);
	}

	public static Map<String, Object> buildModelMapForEmail(FindStudentService findStudentService,

															NotificationVO notificationVO) {
		Map<String, Object> modelMap = new HashMap<>();

		Stud personalDetails = findStudentService.findStud(notificationVO.getStudentReferenceNumber());
		modelMap.put(STUD_FIRST_NAME, ServiceUtil.capitalizeFully(personalDetails.getForenames()));
		modelMap.put(STUDENT_FULL_NAME,
				ServiceUtil.capitalizeFully(personalDetails.getForenames() + " " + personalDetails.getSurname()));
		modelMap.put(SAAS_REFERNECE_NUMBER, notificationVO.getStudentReferenceNumber());
		logger.info("email modelMap {}", modelMap);
		return modelMap;
	}

	public static void populateStudentSignInURL(DSAEmailConfigProperties dsaEmailConfigProperties, String suid,
												long studRefNo, Map<String, Object> model) {
		String token = EmailTokenGenerator.generateRegistrationCode();

		model.put(STUDENT_SIGN_IN_URL,
				org.apache.commons.lang3.StringUtils.join(dsaEmailConfigProperties.getHost(), STUDENT_APPLICATION_PATH,
						EncryptionHelper.encrypt(token), "&suid=", EncryptionHelper.encrypt(suid), "&studRefNum=",
						EncryptionHelper.encrypt(String.valueOf(studRefNo))));
	}

	public static void populateAdvisorSignInURL(DSAEmailConfigProperties dsaEmailConfigProperties,
												Map<String, Object> model) {
		model.put(ADVISOR_SIGN_IN_URL, dsaEmailConfigProperties.getHost() + "/dsa/login");
	}

	public static String buildCommonUrls(Map<String, Object> model, DSAEmailConfigProperties dsaEmailConfigProperties) {
		String contactUsURL = "https://www.saas.gov.uk/contact-us";
		String dsaHelpURL = dsaEmailConfigProperties.getHost() + "/dsa/help";
		String dsaStudentContactUsURL = dsaHelpURL + "#tab1";
		String dsaTeamContactUsURL = dsaHelpURL + "#tab3";

		model.put(CONTACT_US_URL, contactUsURL);
		model.put("DSA_HELP_URL", dsaHelpURL);
		model.put(STUDENT_CONTACT_US_URL, dsaStudentContactUsURL);
		model.put(DSA_TEAM_CONTACT_US_URL, dsaTeamContactUsURL);
		model.put("PRIVACY_POLICY_URL", "https://www.saas.gov.uk/data-protection");
		return dsaTeamContactUsURL;
	}

	// This Method can be deleted
	public static boolean isDevProfile(Environment environment) {
		String[] activeProfiles = environment.getActiveProfiles();
		boolean isDeveloperProfile = Arrays.stream(activeProfiles).anyMatch(DSAConstants.LOCALDEV_PROFILES::contains);

		return isDeveloperProfile || mockSitAccessViaLocal();
	}

	// This Method can be deleted
	private static boolean mockSitAccessViaLocal() {
		// this method is being used to skip the email in the local dev if DB pointed to
		// SIT env
		String sitEnvLocalAccess = System.getProperty("sit.local.access");
		return sitEnvLocalAccess != null;
	}

	public static Timestamp nextExecutionDateTime(String cron) {
		CronExpression cronExpression = CronExpression.parse(cron);
		LocalDateTime nextRunDate = cronExpression.next(LocalDateTime.now());
		assert nextRunDate != null;
		return Timestamp.valueOf(nextRunDate);
	}

	public static int daysDiff(Date date1, Date date2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return daysDifference(dateFormat.format(date1), dateFormat.format(date2));
	}


}
