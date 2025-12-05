package uk.gov.saas.dsa.vo.accommodation;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

@Data
public class AccommodationTypeFormVO {

	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;
	private String accommodationType;
	private String backAction;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("accommodationType");
		return (LinkedHashSet<String>) orderedFields;
	}
}
