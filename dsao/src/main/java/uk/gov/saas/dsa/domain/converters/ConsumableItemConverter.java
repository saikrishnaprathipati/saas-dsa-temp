package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.ConsumableItem;

/**
 * To convert ConsumableItem enum to String and String to enum
 */
@Converter
public class ConsumableItemConverter implements AttributeConverter<ConsumableItem, String> {

	@Override
	public String convertToDatabaseColumn(ConsumableItem arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public ConsumableItem convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return ConsumableItem.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}