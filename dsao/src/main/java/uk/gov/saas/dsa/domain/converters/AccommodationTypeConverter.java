package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.AccommodationType;

/**
 * To convert AccommodationType enum to String and String to enum
 */
@Converter
public class AccommodationTypeConverter implements AttributeConverter<AccommodationType, String> {

	@Override
	public String convertToDatabaseColumn(AccommodationType arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public AccommodationType convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return AccommodationType.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}