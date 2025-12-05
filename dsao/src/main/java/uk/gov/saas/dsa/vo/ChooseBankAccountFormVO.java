package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ChooseBankAccountFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private String nameOnAccount;
	private String accountNumber;
	private String sortCode;
	private String accountNumberForUI;
	private String sortCodeForUI;

	private String paymentFor;
	private Set<String> orderedFields = new LinkedHashSet<String>();
	private String useExistingDetails;

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("useExistingDetails");
		return (LinkedHashSet<String>) orderedFields;
	}
}
