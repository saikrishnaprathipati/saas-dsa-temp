package uk.gov.saas.dsa.vo.quote;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class RemoveQuoteFormVO {
	private String quoteReference;
	private String removeQuote;

	private long quoteId;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private String firstName;
	private String lastName;
	private Integer sessionCode;
	private String supplier;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeQuote");
		return (LinkedHashSet<String>) orderedFields;
	}
}
