package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.SectionStatus;

/**
 * To convert SectionStatus enum to String and String to Enum
 */
@Converter
public class SectionStatusConverter implements AttributeConverter<SectionStatus, String> {

	@Override
	public String convertToDatabaseColumn(SectionStatus arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public SectionStatus convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return SectionStatus.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
