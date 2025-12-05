package uk.gov.saas.dsa.domain.refdata;

import lombok.Getter;

@Getter
public enum YesNoType {
	
	YES("Y", "Yes"),
	
	NO("N", "No");
	
	private String dbValue;
	
	private String displayValue;
	
	
	YesNoType(String dbValue, String displayValue) {
		this.dbValue = dbValue;
		this.displayValue = displayValue;
	}
}
