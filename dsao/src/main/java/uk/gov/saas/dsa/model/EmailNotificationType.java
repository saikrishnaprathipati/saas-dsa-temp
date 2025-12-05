package uk.gov.saas.dsa.model;

import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

import lombok.Getter;

@Getter
public enum EmailNotificationType {

	STEPS_REJECTED("Rejected", "stepsRejectedNotification", new String[] { ADVISOR, STUDENT }, true, true),
	STEPS_AWARDED("Awarded", "stepsAwardedNotification", new String[] { ADVISOR, STUDENT }, true, true),
	STEPS_PENDED_WITH_SAAS("Inprogress", "", new String[] {}, false, true),

	// start: Defined not to process for emails or status update
	STEPS_CONFIRMED("Confirmed", "", new String[] {}, false, false),
	STEPS_PENDED_WITH_HEI_STUDENT("Pending", "", new String[] {}, false, false),
	STEPS_PENDING("Pending", "", new String[] {}, false, false),
	STEPS_RECEIVED("Received", "", new String[] {}, false, false),
	STEPS_APPLICATION_WITHDRAWN("Withdrawn", "", new String[] {}, false, false),
	// End: Defined not to process for emails or status update

	DSA_5_DAY_CHASER("5 days email chaser", "fiveDaysEmailChaser", new String[] { STUDENT }, true, false),
	DSA_10_DAY_CHASER("10 days email chaser", "tenDaysEmailChaser", new String[] { STUDENT, HEI_TEAM }, true, false),
	DSA_30_DAY_CHASER("30 days email chaser", "thirtyDaysEmailChaser", new String[] { STUDENT, HEI_TEAM }, true, false),
	DSA_83_DAY_CHASER("83 days email chaser", "eightyThreeDaysEmailChaser", new String[] { STUDENT, HEI_TEAM }, true,
			false),
	DSA_90_DAY_WITHDRAW("90 days withdraw Application", "", new String[] {}, false, true),
	DSA_91_DAY_DELETION("91 days delete Application", "", new String[] {}, false, false);

	private final String description;
	private final String emailTemplate;
	private final String[] sendTo;
	private final boolean isSystemGeneratedNotification;
	private final boolean updateDSAStatus;

	EmailNotificationType(String description, String emailTemplate, String[] sendTo, boolean isSystemGenerated,
			boolean canUpdateDSAStatus) {

		this.description = description;
		this.emailTemplate = emailTemplate;
		this.sendTo = sendTo;
		this.isSystemGeneratedNotification = isSystemGenerated;
		this.updateDSAStatus = canUpdateDSAStatus;
	}
}
