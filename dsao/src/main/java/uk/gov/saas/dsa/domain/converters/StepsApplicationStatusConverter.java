package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.STEPSAwardStatusCode;
import uk.gov.saas.dsa.model.Section;

/**
 * To convert ApplicationSection enum to String and String to Enum
 */
@Converter
public class StepsApplicationStatusConverter implements AttributeConverter<STEPSAwardStatusCode, String> {

	@Override
	public String convertToDatabaseColumn(STEPSAwardStatusCode arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public STEPSAwardStatusCode convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return STEPSAwardStatusCode.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}