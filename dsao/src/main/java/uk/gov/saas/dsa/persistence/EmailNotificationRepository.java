package uk.gov.saas.dsa.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAEmailNotification;
import uk.gov.saas.dsa.model.EmailNotificationType;

@Repository("emailNotificationsRepository")
public interface EmailNotificationRepository extends JpaRepository<DSAEmailNotification, Long> {
	public static final String SESSION_CODE = "sessionCode";
	public static final String FAILURE_DATE = "failureDate";
	public static final String SUCCESS_DATE = "successDate";
	public static final String NOTIFICATION_TYPE = "notificationType";
	public static final String STUDENT_REFERENCE_NUMBER = "studentReferenceNumber";
//	static final String WHERE_CLAUSE = " where notification.sessionCode =:sessionCode and notification.studentReferenceNumber = :studentReferenceNumber and notification.notificationType = :notificationType";

	void deleteByStudentReferenceNumberAndSessionCode(long studentReferenceNumber, int sessionCode);

	List<DSAEmailNotification> findBySessionCodeAndFailureDateIsNotNullAndNotificationTypeIn(int sessionCode,
			Collection<EmailNotificationType> types);

	List<DSAEmailNotification> findBySessionCodeAndSuccessDateIsNotNullAndNotificationTypeIn(int sessionCode,
			Collection<EmailNotificationType> types);
	
//	@Modifying
//	@Query("update DSAEmailNotification notification set notification.failureDate = null, notification.failureReason = null, notification.successDate = :successDate "
//			+ WHERE_CLAUSE)
//
//	void updateAllFailuresToSuccessForStudentRefNo(@Param(SESSION_CODE) int sessionCode,
//			@Param(SUCCESS_DATE) Date successDate, @Param(STUDENT_REFERENCE_NUMBER) long studentReferenceNumber,
//			@Param(NOTIFICATION_TYPE) EmailNotificationType notificationType);

//	@Modifying
//	@Query("update DSAEmailNotification notification set notification.failureDate = :failureDate, notification.failureReason = :failureReason "
//			+ WHERE_CLAUSE)
//
//	void updateAllFailuresToLatestDateForStudentRefNo(@Param(SESSION_CODE) int sessionCode,
//			@Param(FAILURE_DATE) Date failureDate, @Param("failureReason") String failureReason,
//			@Param(STUDENT_REFERENCE_NUMBER) long studentReferenceNumber,
//			@Param(NOTIFICATION_TYPE) EmailNotificationType notificationType);

//	@Query(value = "select * from (select * from DSA_EMAIL_NOTIFICATION e where E.SUCCESS_DATE is not null order by   E.SUCCESS_DATE DESC)   WHERE ROWNUM = 1", nativeQuery = true)
//	DSAEmailNotification findFirstBySuccessDateNotNullOrderBySuccessDateDesc();

	@Query(value = "select * from (select * from DSA_EMAIL_NOTIFICATION e where e.NOTIFICATION_TYPE in(?1) and e.SUCCESS_DATE is not null order by   E.SUCCESS_DATE DESC)   WHERE ROWNUM = 1", nativeQuery = true)
	DSAEmailNotification findFirstBySuccessForNotificationTypes(Collection<EmailNotificationType> types);

	List<DSAEmailNotification> findBySessionCodeAndStudentReferenceNumberAndNotificationTypeAndSuccessDateNotNull(
			int sessionCode, long studentRefNo, EmailNotificationType type);

	List<DSAEmailNotification> findBySessionCodeAndStudentReferenceNumberAndNotificationType(int sessionCode,
			long studentRefNo, EmailNotificationType type);

	List<DSAEmailNotification> findBySessionCodeAndStudentReferenceNumberAndNotificationTypeAndFailureDateNotNull(
			int sessionCode, long studentRefNo, EmailNotificationType type);
}
