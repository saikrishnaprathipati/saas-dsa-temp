package uk.gov.saas.dsa.service.notification.steps;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.gov.saas.dsa.cache.config.DSABatchJobConfig;
import uk.gov.saas.dsa.service.notification.NotificationUtil;

@Component
public class DSAStepsBatchNotificationScheduler {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAStepsBatchNotificationService notificationService;
	private DSABatchJobConfig batchJobConfig;

	@Value("${steps.update.cron}")
	private String stepsUpdateCron;

	@Autowired
	public DSAStepsBatchNotificationScheduler(DSABatchJobConfig batchJobConfig,
			DSAStepsBatchNotificationService notificationService) {
		this.batchJobConfig = batchJobConfig;
		this.notificationService = notificationService;
	}

	@Scheduled(cron = "${steps.update.cron}")
	public void execute() throws IllegalAccessException {
		if (batchJobConfig.isEnabled()) {
			logger.info("******************* DSA Steps Update batch job started *******************");
			logger.info("stepsUpdateCron scheduled with --> {}", stepsUpdateCron);

			notificationService.initialiseNotificationProcess();
			logger.info("******************* DSA Steps Update batch job ended *******************");
			Timestamp timestamp = NotificationUtil.nextExecutionDateTime(stepsUpdateCron);
			logger.info("******************* DSA Steps Update batch run again on {} *******************", timestamp);
		} else {
			logger.info("$$$$$$$$ DSA Steps Update batch job is Disabled $$$$$$$$");
		}

	}

	
}