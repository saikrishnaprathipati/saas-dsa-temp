package uk.gov.saas.dsa.service.notification.chasers;

import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.saas.dsa.cache.config.DSABatchJobConfig;
import uk.gov.saas.dsa.service.notification.NotificationUtil;

@Component
public class DSAApplciationMadeChaserScheduler {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAAplicationMadeChaserNotificationService notificationService;
	private DSABatchJobConfig batchJobConfig;

	@Value("${email.chaser.cron}")
	private String emailChaserCron;

	@Autowired
	public DSAApplciationMadeChaserScheduler(DSABatchJobConfig batchJobConfig,
			DSAAplicationMadeChaserNotificationService notificationService) {
		this.batchJobConfig = batchJobConfig;
		this.notificationService = notificationService;
	}

	@Scheduled(cron = "${email.chaser.cron}")
	public void execute() throws IllegalAccessException {
		if (batchJobConfig.isEnabled()) {
			logger.info("******************* DSAEmailChaserScheduler batch job started *******************");
			logger.info("emailChaserCron--> scheduled with {}", emailChaserCron);
			notificationService.initEmailChaserNotificationProcess();
			logger.info("******************* DSAEmailChaserScheduler batch job ended *******************");
			Timestamp timestamp = NotificationUtil.nextExecutionDateTime(emailChaserCron);
			logger.info("******************* DSAEmailChaserScheduler run again on {} *******************", timestamp);
		} else {
			logger.info("$$$$$$$$ DSAEmailChaserScheduler batch job is Disabled $$$$$$$$");
		}

	}
}