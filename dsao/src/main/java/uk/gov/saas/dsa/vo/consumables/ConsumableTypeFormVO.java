package uk.gov.saas.dsa.vo.consumables;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ConsumableTypeFormVO {

	private long dsaApplicationNumber;

	private long studentReferenceNumber;

	private String studentFirstName;

	private String backAction;
	@Size(max = 25, message = "{consumable.otherDescription.maxLength}")
	private String otherDescription;

	@NotEmpty(message = "{consumable.checkBox.required}")
	private List<String> consumableItemCodes;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("consumableItemCodes");
		orderedFields.add("otherDescription");
		return (LinkedHashSet<String>) orderedFields;
	}

}
