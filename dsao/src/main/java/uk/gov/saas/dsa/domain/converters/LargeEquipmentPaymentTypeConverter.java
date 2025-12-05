package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;

@Converter
public class LargeEquipmentPaymentTypeConverter implements AttributeConverter<LargeEquipmentPaymentType, String> {
	@Override
	public String convertToDatabaseColumn(LargeEquipmentPaymentType arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public LargeEquipmentPaymentType convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return LargeEquipmentPaymentType.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
