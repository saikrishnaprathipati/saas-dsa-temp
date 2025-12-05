package uk.gov.saas.dsa.web.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import static uk.gov.saas.dsa.web.helper.DSAConstants.OTHER_TEXT_PATTERN;

/**
 * Validation helper
 */
public class ValidationHelper {
	private static final Logger logger = LogManager.getLogger(ValidationHelper.class);
	public static final String _00_00_00 = "00-00-00";
	public static final String _00000000 = "00000000";
	public static final String _000000 = "000000";
	public static final String EIGHT_DIGITS = "^\\d{8}$";
	public static final String SIX_DIGITS = "^\\d{6}$";

	public static boolean matches(Pattern pattern, String value) {
		boolean matches = pattern.matcher(value).matches();

		logger.info("Pattern {} -> {} : {}", pattern, value, (matches ? "Matched" : "NOT Matched"));

		return matches;
	}

	public static void addError(BindingResult bindingResult, MessageSource messageSource, String beanName,
								String fieldName, String messageResourceName) {
		String message = messageSource.getMessage(messageResourceName, null, Locale.UK);
		bindingResult.addError(new FieldError(beanName, fieldName, message));
	}

	public static void rejectFieldValue(BindingResult bindingResult, String field, String fieldMessage) {
		bindingResult.rejectValue(field, fieldMessage);
	}

	public static void validateFieldValue(BindingResult bindingResult, String fieldName, String value,
										  String errorMessage) {

		if (!bindingResult.hasFieldErrors(fieldName) && !matches(Pattern.compile(OTHER_TEXT_PATTERN), value)) {
			ValidationHelper.rejectFieldValue(bindingResult, fieldName, String.format(errorMessage, fieldName));

		}
	}

	public static void validateNumber(BindingResult bindingResult, String accountNumber, String property,
									  String messageProperty) {
		if (StringUtils.hasLength(accountNumber)) {
			boolean isSixDigits = ValidationHelper.matches(Pattern.compile(SIX_DIGITS), accountNumber);
			boolean isEightDigits = ValidationHelper.matches(Pattern.compile(EIGHT_DIGITS), accountNumber);
			boolean isValidAccountNumber = (isSixDigits || isEightDigits)
					&& (!Arrays.asList(_00000000, _000000).contains(accountNumber));
			if (!isValidAccountNumber) {
				bindingResult.rejectValue(property, messageProperty);
			}
		}
	}

}
