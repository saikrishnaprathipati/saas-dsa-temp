package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.TravelExpType;

/**
 * To convert TravelExpType enum to String and String to enum
 */
@Converter
public class TravelExpTypeConverter implements AttributeConverter<TravelExpType, String> {

	@Override
	public String convertToDatabaseColumn(TravelExpType arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public TravelExpType convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return TravelExpType.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}