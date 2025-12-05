package uk.gov.saas.dsa.domain.refdata;

import lombok.Getter;

@Getter
public enum UkCountriesType {
	
	SCOTLAND("299", "Scotland"),
	
	ENGLAND("1", "England"),
	
	NORTHERN_IRELAND("4", "Northern Ireland"),
	
	WALES("579", "Wales");
	
	private String dbValue;
	
	private String displayValue;
	
	
	UkCountriesType(String dbValue, String displayValue) {
		this.dbValue = dbValue;
		this.displayValue = displayValue;
	}

}
