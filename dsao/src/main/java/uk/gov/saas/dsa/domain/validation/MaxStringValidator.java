package uk.gov.saas.dsa.domain.validation;

import jakarta.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaxStringValidator implements jakarta.validation.ConstraintValidator<MaxString, String> {
	private double maxValue;

    private final Logger logger = LogManager.getLogger(this.getClass());
	@Override
	public void initialize(MaxString maxString) {
		maxValue = Double.valueOf(maxString.value());
	}

	@Override
	public boolean isValid(String valueToCheck, ConstraintValidatorContext constraintValidatorContext) {
		if (valueToCheck == null || valueToCheck.isEmpty()) {
			return true;
		}
		try {
			return Double.valueOf(valueToCheck).compareTo(maxValue) <= 0;
		} catch (NumberFormatException e) {
			logger.info("MaxString NumberFormatException --> {}", e.getMessage());			
			return false;
		}
	}

}
