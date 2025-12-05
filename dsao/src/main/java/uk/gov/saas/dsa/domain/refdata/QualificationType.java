package uk.gov.saas.dsa.domain.refdata;

import lombok.Getter;

@Getter
public enum QualificationType {
	
	UG_DEGREE("UG", "Undergraduate Degree"),
	PG_DEGREE("PG", "Postgraduate Degree"),
	HNC("HNC", "Higher National Certificate"),
	HND("HND", "Higher National Diploma"),
	NONE("None", "None of the above");
	
	private String dbValue;
	
	private String displayValue;
	
	QualificationType(String dbValue, String displayValue) {
		this.dbValue = dbValue;
		this.displayValue = displayValue;
	}
}
