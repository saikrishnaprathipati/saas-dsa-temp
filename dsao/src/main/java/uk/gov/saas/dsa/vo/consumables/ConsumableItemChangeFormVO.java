package uk.gov.saas.dsa.vo.consumables;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import uk.gov.saas.dsa.model.ConsumableItem;

@Data
public class ConsumableItemChangeFormVO {

	private String cost;
	private String description;
	private ConsumableItem consumableItem;
	private long id;
	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("cost");
		return (LinkedHashSet<String>) orderedFields;
	}
}
