package uk.gov.saas.dsa.web.wizard;

import static java.util.Arrays.asList;

import java.util.List;

import lombok.Getter;

@Getter
public enum ApplicationDetailsFlowWizard {
	COURSE_MODE("course", asList("courseMode"), "fundingType", ""),
	FUNDING_TYPE("fundingType", asList("fundingType"), "mainFunding", "course"),
	MAIN_FUNDING_APPLICATION("mainFunding", asList(""), "", "fundingType");

	/**
	 * This is the field appearing in the page URL
	 */
	private String pageSection;
	/**
	 * backing bean member variables to be validated
	 */
	private List<String> subFields;
	/**
	 * Next form field appearing in the page URL
	 */
	private String next;
	/**
	 * previous form field appearing in the page URL
	 */

	private String previous;

	ApplicationDetailsFlowWizard(String pageSection, List<String> subFields, String next, String previous) {
		this.pageSection = pageSection;
		this.next = next;
		this.previous = previous;
		this.subFields = subFields;
	}
}
