package uk.gov.saas.dsa.model;

import lombok.Getter;

import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

@Getter
public enum SectionStatus {
	/**
	 * Status to represent it in the dashboard screen
	 */
	CANNOT_START_YET(1, "CANNOT_START_YET", "CANNOT START", GREY),
	NOT_STARTED(2, "NOT_STARTED", "NOT STARTED", YELLOW),
	PENDING(2, "PENDING", "PENDING", YELLOW),
	STARTED(3, "STARTED", "STARTED", YELLOW),
	REVIEW(3, "REVIEW", "REVIEW", YELLOW),
	SKIPPED(3, "SKIPPED", "SKIPPED", GREY),
	COMPLETED(4, "COMPLETED", "COMPLETED", GREEN),
	REJECTED(5, "REJECTED", "REJECTED", RED);

	private final int rank;
	private final String code;
	private final String description;
	private final String color;

	SectionStatus(int rank, String code, String desc, String color) {
		this.rank = rank;
		this.code = code;
		this.description = desc;
		this.color = color;
	}
}
