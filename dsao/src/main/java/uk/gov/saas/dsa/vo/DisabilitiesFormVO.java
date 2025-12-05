package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class DisabilitiesFormVO {

	private long studentReferenceNumber;

	private long dsaApplicationNumber;
	
	@Size(max = 300, message = "{disability.notListedText.maxLength}")
	private String notListedText;

	@NotEmpty(message = "{disability.checkBox.required}")
	private List<String> disabilityCodes;

	private String studentFirstName;

	private String disabilityNotListedAndText;

	private String disabilityNotListedChekBox;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("disabilityCodes");
		orderedFields.add("disabilityNotListedChekBox");
		orderedFields.add("notListedText");
		orderedFields.add("disabilityNotListedAndText");

		return (LinkedHashSet<String>) orderedFields;
	}
}
