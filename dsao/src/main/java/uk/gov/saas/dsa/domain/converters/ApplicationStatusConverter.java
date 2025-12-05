package uk.gov.saas.dsa.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import uk.gov.saas.dsa.model.ApplicationSummaryStatus;
/**
 * To convert ApplicationStatus enum to String and String to Enum
 */
@Converter
public class ApplicationStatusConverter implements AttributeConverter<ApplicationSummaryStatus, String> {
	@Override
	public String convertToDatabaseColumn(ApplicationSummaryStatus arg0) {
		if (arg0 == null) {
			return null;
		}
		return arg0.toString();
	}

	@Override
	public ApplicationSummaryStatus convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		try {
			return ApplicationSummaryStatus.valueOf(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
