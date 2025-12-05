package uk.gov.saas.dsa.model;

import lombok.Getter;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Getter
public enum DSAAwardProcessedStatus {

	FULLY_AWARDED("FULLY AWARDED", "PAID AS CLAIMED", DSAConstants.GREEN),
	PARTIALLY_AWARDED("PARTIALLY AWARDED", "NOT PAID AS CLAIMED", DSAConstants.YELLOW);

	private String description;
	private String code;
	private String color;

	DSAAwardProcessedStatus(String description, String code, String color) {

		this.description = description;
		this.code = code;
		this.color = color;
	}
}
