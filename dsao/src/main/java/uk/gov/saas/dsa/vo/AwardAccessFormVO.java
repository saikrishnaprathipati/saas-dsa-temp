package uk.gov.saas.dsa.vo;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class AwardAccessFormVO {
	private String canAccess;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("canAccess");
		return (LinkedHashSet<String>) orderedFields;
	}
}
