package uk.gov.saas.dsa.vo.equipment;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class RemoveItemFormVO {
	private String itemName;
	private String removeItem;

	private long itemId;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
