package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.PaymentFor;

@Converter
public class PaymentForConverter implements AttributeConverter<PaymentFor, String> {
	@Override
	public String convertToDatabaseColumn(PaymentFor arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public PaymentFor convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return PaymentFor.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
