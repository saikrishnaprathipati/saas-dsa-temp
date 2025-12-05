package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AdditionalInfoFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;

	@Size(max = 500, message = "{additionalInfo.infoText.maxLength}")
	private String infoText;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("infoText");
		return (LinkedHashSet<String>) orderedFields;
	}
}
