package uk.gov.saas.dsa.model;

import lombok.Getter;

/**
 * Consumable Item
 */
@Getter
public enum AccommodationType {

	PRIVATE(1, "Private Accommodation", "Private"),  
	OTHER(1, "Other", "Other (Residental Accomodation)");

	int order;
	private String description;
	private String stepsDescription;
	AccommodationType(int order, String description, String stepsDescription) {
		this.order = order;
		this.description = description;
		this.stepsDescription = stepsDescription;
	}

}