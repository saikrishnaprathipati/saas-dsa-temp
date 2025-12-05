package uk.gov.saas.dsa.model;

import lombok.Getter;

/**
 * Consumable Item
 */
@Getter
public enum ConsumableItem {

	PAPER(1, "Paper"), INK_CARTRIDGE(2, "Ink cartridges"), PRINTING(3, "Printing"), PHOTO_COPYING(4, "Photocopying"),
	OTHER(5, "Other");

	int order;
	private String itemName;

	ConsumableItem(int order, String description) {
		this.order = order;
		this.itemName = description;
	}

}