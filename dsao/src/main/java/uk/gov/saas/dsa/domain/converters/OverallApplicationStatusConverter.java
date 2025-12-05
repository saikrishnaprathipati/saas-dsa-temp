package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.OverallApplicationStatus;

@Converter
public class OverallApplicationStatusConverter implements AttributeConverter<OverallApplicationStatus, String> {
	@Override
	public String convertToDatabaseColumn(OverallApplicationStatus arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public OverallApplicationStatus convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return OverallApplicationStatus.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
