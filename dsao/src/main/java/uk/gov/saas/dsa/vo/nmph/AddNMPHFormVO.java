package uk.gov.saas.dsa.vo.nmph;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AddNMPHFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;

	@NotBlank(message = "{nmph.typeOfSupport.required}")
	@Size(max = 50, message = "{nmph.typeOfSupport.maxLength}")
	private String typeOfSupport;

	@NotBlank(message = "{nmph.recommendedProvider.required}")
	@Size(max = 50, message = "{nmph.recommendedProvider.maxLength}")
	private String recommendedProvider;

	@NotBlank(message = "{nmph.hourlyRate.required}")
	private String hourlyRate;

	@NotBlank(message = "{nmph.hours.required}")
	private String hours;

	@Min(value = 1, message = "{nmph.weeks.required}")
	private Integer weeks;

	private String cost;
	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("typeOfSupport");
		orderedFields.add("recommendedProvider");
		orderedFields.add("hourlyRate");
		orderedFields.add("hours");
		orderedFields.add("weeks");
		return (LinkedHashSet<String>) orderedFields;
	}
}
