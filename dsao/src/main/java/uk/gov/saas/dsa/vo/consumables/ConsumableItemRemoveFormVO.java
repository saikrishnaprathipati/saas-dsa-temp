package uk.gov.saas.dsa.vo.consumables;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import uk.gov.saas.dsa.model.ConsumableItem;

@Data
public class ConsumableItemRemoveFormVO {
	private ConsumableItem consumableItem;

	private long consumableItemId;

	private long dsaApplicationNumber;
	private long studentReferenceNumber;

	private String removeItem;
	
	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
