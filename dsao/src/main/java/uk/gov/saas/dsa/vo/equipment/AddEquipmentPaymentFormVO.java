package uk.gov.saas.dsa.vo.equipment;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

@Data
public class AddEquipmentPaymentFormVO {
	private String paymentForItem = "INSTITUTION";

	private long dsaApplicationNumber;
	private long studentReferenceNumber;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("paymentForItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
