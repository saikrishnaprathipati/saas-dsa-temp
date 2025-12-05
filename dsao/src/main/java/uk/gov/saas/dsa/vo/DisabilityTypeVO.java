package uk.gov.saas.dsa.vo;

import lombok.Data;

/**
 * To populate the disabilities on the UI
 */
@Data
public class DisabilityTypeVO {
	public static final String DISABILITY_NOT_LISTED = "DISABILITY_NOT_LISTED";
	private long disabilityTypeId;
	private String disabilityCode;
	private String disabilityTypeDesc;

	private String disabilityTypeHintText;
	private String disabilityNotlistedText;
	private String isActive;
	private boolean isSelected;

}
