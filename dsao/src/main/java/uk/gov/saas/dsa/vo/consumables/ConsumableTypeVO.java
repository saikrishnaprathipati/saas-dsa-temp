package uk.gov.saas.dsa.vo.consumables;

import java.math.BigDecimal;

import lombok.Data;
import uk.gov.saas.dsa.model.ConsumableItem;

/**
 * Used to populate or transfer the Consumable item data
 */
@Data
public class ConsumableTypeVO {
	/**
	 * Consumable item Id in the DB
	 */
	private long id;

	/**
	 * DSA Application Number in the DB
	 */
	private long dsaApplicationNumber;

	/**
	 * Student Reference Number in the DB
	 */
	private long studentReferenceNumber;

	/**
	 * Consumable Item
	 */
	private ConsumableItem consumableItem;

	private String otehrItemText;

	/**
	 * Used to calculate or to insert in to DB
	 */
	private BigDecimal cost;

	/**
	 * Used to render the value in the HTML
	 */
	private String costString;

}
