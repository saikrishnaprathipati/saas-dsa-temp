//package uk.gov.saas.dsa.service.notification;
//
//import java.sql.Date;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Calendar;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
//import uk.gov.saas.dsa.domain.DSAEmailNotification;
//import uk.gov.saas.dsa.model.EmailNotificationType;
//import uk.gov.saas.dsa.persistence.EmailNotificationsRepository;
//import uk.gov.saas.dsa.persistence.readonly.DSAApplicationRepository;
//import uk.gov.saas.dsa.persistence.refdata.DSAApplicationStatusRepository;
//
//@ExtendWith(SpringExtension.class)
//class EmailNotificationServiceTest {
//	private final Logger logger = LogManager.getLogger(this.getClass());
//	private static final long STUD_REF_NO_1 = 1;
//	private static final long STUD_REF_NO_2 = 2;
//	private static final long STUD_REF_NO_3 = 3;
//
//	@MockitoBean
//	DSAEmailConfigProperties dsaEmailConfigProperties;
//	@MockitoBean
//	EmailNotificationsRepository notificationsRepo;
//	@MockitoBean
//	DSAApplicationRepository dsaApplicationRepository;
//	@MockitoBean
//	DSAApplicationStatusRepository applicationStatusRepository;
//
//	private EmailNotificationService subject;
//
//	@Test
//	void test() {
//		subject = new EmailNotificationService(dsaEmailConfigProperties, notificationsRepo, dsaApplicationRepository,
//				applicationStatusRepository);
//		mockFailures();
//		subject.initialiseNotificationProcess();
//	}
//
//	private void mockFailures() {
//
//		DSAEmailNotification pending1_stud_1_failedToday = mockFailureNotification(EmailNotificationType.STEPS_PENDING,
//				STUD_REF_NO_1, mockDate(0));
//
//		DSAEmailNotification pending2_stud_1_failedToday = mockFailureNotification(EmailNotificationType.STEPS_PENDING,
//				STUD_REF_NO_1, mockDate(0));
//
//		DSAEmailNotification rejected_stud_1_failedToday = mockFailureNotification(EmailNotificationType.STEPS_REJECTED,
//				STUD_REF_NO_1, mockDate(0));
//		DSAEmailNotification rejected_stud_1_failed_1dayBack = mockFailureNotification(
//				EmailNotificationType.STEPS_REJECTED, STUD_REF_NO_1, mockDate(-1));
//		DSAEmailNotification rejected_stud_1_failed_2daysBack = mockFailureNotification(
//				EmailNotificationType.STEPS_REJECTED, STUD_REF_NO_1, mockDate(-2));
//
//		DSAEmailNotification rejected_stud_2_failedToday = mockFailureNotification(EmailNotificationType.STEPS_REJECTED,
//				STUD_REF_NO_2, mockDate(0));
//
//		Mockito.when(notificationsRepo.findByFailureDateIsNotNull())
//				.thenReturn(Arrays.asList(pending1_stud_1_failedToday, pending2_stud_1_failedToday,
//						rejected_stud_1_failedToday, rejected_stud_1_failed_1dayBack, rejected_stud_1_failed_2daysBack,
//						rejected_stud_2_failedToday));
//	}
//
//	private Date mockDate(int dates) {
//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.DATE, dates);
//
//		Date date = new Date(calendar.getTimeInMillis());
//		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
//		String dateString = format.format(date);
//		logger.info("failure date is {}", dateString);
//
//		return new Date(calendar.getTimeInMillis());
//
//	}
//
//	private DSAEmailNotification mockSuccessNotification(EmailNotificationType type, long studRefNo, Date date) {
//		DSAEmailNotification notification = setData(type, studRefNo);
//		notification.setSuccessDate(date);
//
//		return notification;
//	}
//
//	private DSAEmailNotification mockFailureNotification(EmailNotificationType type, long studRefNo, Date date) {
//		DSAEmailNotification notification = setData(type, studRefNo);
//		notification.setFailureDate(date);
//		return notification;
//	}
//
//	private DSAEmailNotification setData(EmailNotificationType type, long studRefNo) {
//		DSAEmailNotification notification = new DSAEmailNotification();
//		notification.setNotificationType(type);
//		notification.setStudentReferenceNumber(studRefNo);
//
//		return notification;
//	}
//
//}
