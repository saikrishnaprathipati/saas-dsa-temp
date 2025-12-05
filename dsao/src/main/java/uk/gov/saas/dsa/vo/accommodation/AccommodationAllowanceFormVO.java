package uk.gov.saas.dsa.vo.accommodation;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class AccommodationAllowanceFormVO {

	private long studentReferenceNumber;

	private long dsaApplicationNumber;

	private long id;

	private String accommodationType;

	@NotBlank(message = "{accommodation.standardAccommodationCost.required}")
	private String standardAccommodationCost;

	@NotBlank(message = "{accommodation.enhancedAccommodationCost.required}")
	private String enhancedAccommodationCost;

	@NotBlank(message = "{accommodation.weeks.required}")
	private String weeks;

	private String backAction;
	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("accommodationType");
		orderedFields.add("standardAccommodationCost");
		orderedFields.add("enhancedAccommodationCost");
		orderedFields.add("weeks");
		return (LinkedHashSet<String>) orderedFields;
	}
}
