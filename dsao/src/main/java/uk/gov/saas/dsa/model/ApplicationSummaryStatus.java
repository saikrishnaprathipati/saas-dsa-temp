package uk.gov.saas.dsa.model;

import lombok.Getter;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

/**
 * Application status
 */
@Getter
public enum ApplicationSummaryStatus {
	/**
	 * Status to represent it in the application summary screen
	 */
	APPLICATION_INCOMPLETE("APPLICATION_INCOMPLETE", "APPLICATION INCOMPLETE", GREY),
	COMPLETED("APPLICATION_COMPLETE", "APPLICATION COMPLETE", GREEN);

	private String code;
	private String description;

	/**
	 * To Derive the Button status in the html
	 */
	private String color;

	ApplicationSummaryStatus(String code, String description, String color) {
		this.code = code;
		this.description = description;
		this.color = color;
	}

}