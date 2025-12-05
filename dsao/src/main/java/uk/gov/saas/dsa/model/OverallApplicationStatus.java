package uk.gov.saas.dsa.model;

import lombok.Getter;

import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

/**
 * Application status
 */
@Getter
public enum OverallApplicationStatus {
	NOT_STARTED("NOT_STARTED", "NOT STARTED", YELLOW),
	STARTED("STARTED", "STARTED", YELLOW),
	NOT_COMPLETE("NOT_COMPLETE", "NOT COMPLETE", YELLOW),
	SUBMITTED("SUBMITTED", "SUBMITTED", YELLOW),
	AWARDED("AWARDED", "ASSESSED", GREEN), NOT_AWARDED("NOT_AWARDED", "NOT AWARDED", RED),
	RECEIVED("RECEIVED", "RECEIVED", YELLOW), WITHDRAWN("WITHDRAWN", "WITHDRAWN", RED),
	PENDING("PENDING", "PENDING", YELLOW), IN_PROGRESS("IN_PROGRESS", "IN PROGRESS", YELLOW);

	private final String code;
	private final String description;

	private final String color;

	OverallApplicationStatus(String code, String description, String color) {
		this.code = code;
		this.description = description;
		this.color = color;
	}
}
