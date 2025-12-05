package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.EmailNotificationType;

@Converter
public class EmailNotificationTypeConverter implements AttributeConverter<EmailNotificationType, String> {
	@Override
	public String convertToDatabaseColumn(EmailNotificationType arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public EmailNotificationType convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return EmailNotificationType.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
