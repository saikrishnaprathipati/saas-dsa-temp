package uk.gov.saas.dsa.vo.nmph;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

@Data
public class RemoveItemFormVO {
	private String itemName;

	private long itemId;

	private long dsaApplicationNumber;
	private long studentReferenceNumber;

	private String removeItem;
	private String backAction;
	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
