package uk.gov.saas.dsa.service.notification;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.saas.dsa.domain.DSAEmailNotification;
import uk.gov.saas.dsa.model.EmailNotificationType;
import uk.gov.saas.dsa.persistence.EmailNotificationRepository;

@Service
public class NotificationHelperService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private EmailNotificationRepository notificationsRepo;

	public NotificationHelperService(EmailNotificationRepository notificationsRepo) {
		this.notificationsRepo = notificationsRepo;
	}

	public void updateSuccessNotification(NotificationVO notificationVO, Date dateAndTime) throws IllegalAccessException {
		DSAEmailNotification dsaEmailNotification = getNotifiction(notificationVO.getId());

		dsaEmailNotification.setNotificationType(notificationVO.getNotificationType());
		dsaEmailNotification.setSuccessDate(dateAndTime);
		
		notificationsRepo.save(dsaEmailNotification);
		logger.info("Existing success notification updated succesfully! {}", notificationVO);
	}
	
	public void updateFailureToSuccess(NotificationVO notificationVO, Date dateAndTime) throws IllegalAccessException {

		DSAEmailNotification dsaEmailNotification = getNotifiction(notificationVO.getId());
		dsaEmailNotification.setFailureDate(null);
		dsaEmailNotification.setFailureReason(null);
		dsaEmailNotification.setSuccessDate(dateAndTime);
		notificationsRepo.save(dsaEmailNotification);
		logger.info("Updated the failure to success {}", notificationVO);
	}
	 
	private DSAEmailNotification getNotifiction(long id) throws IllegalAccessException {
		Optional<DSAEmailNotification> notification = notificationsRepo.findById(id);
		DSAEmailNotification dsaEmailNotification;
		if (notification.isPresent()) {
			dsaEmailNotification = notification.get();
			logger.info("Existing notification {}", dsaEmailNotification);
		} else {
			throw new IllegalAccessException("No notification exist with id:" + id);
		}
		return dsaEmailNotification;
	}

	public void updateFailureNotification(NotificationVO notificationVO, Date dateAndTime, String errroMessage)
			throws IllegalAccessException {

		DSAEmailNotification

		dsaEmailNotification = getNotifiction(notificationVO.getId());
		dsaEmailNotification.setFailureDate(dateAndTime);
		dsaEmailNotification.setFailureReason(errroMessage);
		dsaEmailNotification.setSuccessDate(null);
		notificationsRepo.save(dsaEmailNotification);
		logger.info("update existing FailureNotification success {}", dsaEmailNotification);
	}

	@Transactional
	public void save(DSAEmailNotification notification) {
		logger.info("saving the notification {}", notification);
		try {
			notificationsRepo.save(notification);
			logger.info("Notification saved succesfully {}", notification);
		} catch (Exception exception) {
			logger.info("Notification already exist {}", notification);
			logger.info("Exception {}", exception.getMessage());
			exception.printStackTrace();
		}
	}

	public boolean isFailedNotificationNotExists(NotificationVO notificationVO) {
		List<DSAEmailNotification> failedNotifications = notificationsRepo
				.findBySessionCodeAndStudentReferenceNumberAndNotificationTypeAndFailureDateNotNull(
						notificationVO.getSessionCode(), notificationVO.getStudentReferenceNumber(),
						notificationVO.getNotificationType());

		boolean notExist = failedNotifications == null || failedNotifications.isEmpty();

		return notExist;

	}

	public DSAEmailNotification findFirstByOrderBySuccessDateIsNotNullDesc(List<EmailNotificationType> types) {

		DSAEmailNotification mostRecentSucees = notificationsRepo.findFirstBySuccessForNotificationTypes(types);
		logger.info("Most Recent Sucees for types {} is {}", types, mostRecentSucees);
		return mostRecentSucees;
	}

	public List<DSAEmailNotification> findSuccessNotificationsForTypes(int sessionCode, List<EmailNotificationType> types) {
		List<DSAEmailNotification> list = notificationsRepo
				.findBySessionCodeAndSuccessDateIsNotNullAndNotificationTypeIn(sessionCode, types);
		if (list == null) {
			list = new ArrayList<DSAEmailNotification>();
		}
		logger.info("success notifications for session code: {} and Types : {} are {}", sessionCode, types, list);
		return list;

	}
	
	public List<DSAEmailNotification> findFailureNotifications(int sessionCode, List<EmailNotificationType> types) {
		List<DSAEmailNotification> failedChaserNotifications = notificationsRepo
				.findBySessionCodeAndFailureDateIsNotNullAndNotificationTypeIn(sessionCode, types);
		failedChaserNotifications = failedChaserNotifications != null ? failedChaserNotifications
				: new ArrayList<DSAEmailNotification>();
		logger.info("All failed notifications for session code {} are {}", sessionCode, failedChaserNotifications);
		return failedChaserNotifications;

	}

	public boolean isNotificationAlreadyProcessed(int sessionCode, long studentRefNo,
			EmailNotificationType emailNotificationType) {
		List<DSAEmailNotification> list = notificationsRepo
				.findBySessionCodeAndStudentReferenceNumberAndNotificationTypeAndSuccessDateNotNull(sessionCode,
						studentRefNo, emailNotificationType);
		boolean isNotificationAlreadyProcessed = list != null && !list.isEmpty();
		logger.info("Notification succesfully processed before for studentRefNo : {} notification {}", studentRefNo, emailNotificationType);
		return isNotificationAlreadyProcessed;

	}

	public List<DSAEmailNotification> getExistingNotificationsForStudent(int sessionCode, long studentRefNo,
			EmailNotificationType emailNotificationType) {
		List<DSAEmailNotification> processedList = new ArrayList<DSAEmailNotification>();

		List<DSAEmailNotification> list = notificationsRepo
				.findBySessionCodeAndStudentReferenceNumberAndNotificationType(sessionCode, studentRefNo,
						emailNotificationType);
		if (list != null) {
			processedList = list;
		}
		logger.info("Notification processed list {}", processedList);
		return processedList;

	}

}
