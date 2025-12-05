package uk.gov.saas.dsa.vo.consumables;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import uk.gov.saas.dsa.model.ConsumableItem;

@Data
public class AddConsumableFormVO {

	private long dsaApplicationNumber;

	private long studentReferenceNumber;

	private List<ConsumableItemFormVO> consumableItems;

	private Set<String> orderedFields = new LinkedHashSet<String>();
	private LinkedHashMap<String, ConsumableError> errorIdMapp = new LinkedHashMap<String, ConsumableError>();

	public HashMap<String, ConsumableError> getErrorIdMapp() {
		List<ConsumableItem> orderedConsumableItems = this.getConsumableItems().stream()
				.map(ConsumableItemFormVO::getConsumableItem).sorted(Comparator.comparing(ConsumableItem::getOrder))
				.collect(Collectors.toList());
		orderedConsumableItems.forEach(consuItem -> {
			ConsumableError error = new ConsumableError();
			error.setErrorField("consumableItems[" + orderedConsumableItems.indexOf(consuItem) + "].cost");
			error.setFieldName(consuItem.getItemName());
			errorIdMapp.put(consuItem.name(), error);
		});
		return errorIdMapp;
	}

	public LinkedHashSet<String> getOrderedFields() {

		return (LinkedHashSet<String>) orderedFields;
	}

}
