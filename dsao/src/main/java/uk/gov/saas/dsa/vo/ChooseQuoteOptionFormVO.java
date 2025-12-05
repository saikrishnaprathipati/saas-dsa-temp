package uk.gov.saas.dsa.vo;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class ChooseQuoteOptionFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private Integer sessionCode;
	private String courseYear;
	private String advisorId;
	private Set<String> orderedFields = new LinkedHashSet<String>();
	private String useQuote;

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("useQuote");
		return (LinkedHashSet<String>) orderedFields;
	}
}
