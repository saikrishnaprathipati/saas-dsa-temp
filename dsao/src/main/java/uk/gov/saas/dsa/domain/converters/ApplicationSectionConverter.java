package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.Section;

/**
 * To convert ApplicationSection enum to String and String to Enum
 */
@Converter
public class ApplicationSectionConverter implements AttributeConverter<Section, String> {

	@Override
	public String convertToDatabaseColumn(Section arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public Section convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return Section.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}