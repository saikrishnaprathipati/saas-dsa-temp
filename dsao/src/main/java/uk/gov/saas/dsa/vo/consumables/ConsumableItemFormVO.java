package uk.gov.saas.dsa.vo.consumables;

import lombok.Data;
import uk.gov.saas.dsa.model.ConsumableItem;

@Data
public class ConsumableItemFormVO { 
	private String cost;
	private String description;
	private ConsumableItem consumableItem;
}
