package uk.gov.saas.dsa.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinStringValidator implements ConstraintValidator<MinString, String> {
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	private int minValue;

	@Override
	public void initialize(MinString minString) {
		minValue = Integer.valueOf(minString.value());
	}

	@Override
	public boolean isValid(String valueToCheck, ConstraintValidatorContext constraintValidatorContext) {

		if (valueToCheck == null || valueToCheck.isEmpty()) {
			return true;
		}

		try {
			return Double.valueOf(valueToCheck).compareTo((double) minValue) >= 0;
		} catch (NumberFormatException e) {
			logger.info("MinString NumberFormatException --> {}", e.getMessage());
			return false;
		}
	}
}
