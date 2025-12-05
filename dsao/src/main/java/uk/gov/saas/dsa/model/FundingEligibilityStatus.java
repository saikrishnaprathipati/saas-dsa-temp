package uk.gov.saas.dsa.model;

import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

import lombok.Getter;

/**
 * Application Section details.
 */
@Getter
public enum FundingEligibilityStatus {

	CONFIRMED("Confirmed", GREEN), REJECTED("Rejected", RED), PENDING("Pending", YELLOW), UNKNOWN("Unknown", RED);

	private String description;
	private String color;

	FundingEligibilityStatus(String description, String color) {
		this.description = description;
		this.color = color;
	}

}