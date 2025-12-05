package uk.gov.saas.dsa.model;

import lombok.Getter;

/**
 * NumberType Item
 */
@Getter
public enum NumberType {

	COST_XXXXX_YY(0.01, 99999.99), COST_XXX_YY(0.01, 999.99), COST_X_YY(0.01, 0.99), 
	HOURS(1d, 50d),	WEEKS(1d, 52d), RETURN_JOURNEYS(1d, 21d), NUMBER_XX(1d, 99d), NUMBER_XXX(1d, 999d);

	private double min;
	private double max;
	
	NumberType(double min, double max) {
		this.min = min;
		this.max = max;
	}
}