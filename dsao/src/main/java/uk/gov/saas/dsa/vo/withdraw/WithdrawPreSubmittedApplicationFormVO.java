package uk.gov.saas.dsa.vo.withdraw;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class WithdrawPreSubmittedApplicationFormVO {
	private long dsaApplicationNumber;
	private long studentReferenceNumber;

	private String doWithdraw;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("doWithdraw");
		return (LinkedHashSet<String>) orderedFields;
	}
}
