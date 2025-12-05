package uk.gov.saas.dsa.domain.refdata;

import lombok.Getter;

@Getter
public enum LargeEquipmentPaymentType {

	STUDENT("Student"),

	INSTITUTION("Instution");

	private String displayValue;

	LargeEquipmentPaymentType(String displayValue) {

		this.displayValue = displayValue;
	}
}
