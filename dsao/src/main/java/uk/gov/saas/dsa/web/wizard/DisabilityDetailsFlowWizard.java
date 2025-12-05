package uk.gov.saas.dsa.web.wizard;

import static java.util.Arrays.asList;

import java.util.List;

import lombok.Getter;

@Getter
public enum DisabilityDetailsFlowWizard {
	DISABILITY_TODO("disabilityName", asList(""), "", "");

	/**
	 * This is the pageSection appearing in the page URL
	 */
	private String pageSection;
	
	/**
	 * backing bean member variables to be validated
	 */
	private List<String> subFields;
	
	/**
	 * Next form pageSection appearing in the page URL
	 */
	private String next;
	
	/**
	 * previous form pageSection appearing in the page URL
	 */
	private String previous;

	DisabilityDetailsFlowWizard(String pageSection, List<String> subFields, String next, String previous) {
		this.pageSection = pageSection;
		this.next = next;
		this.previous = previous;
		this.subFields = subFields;
	}
}
