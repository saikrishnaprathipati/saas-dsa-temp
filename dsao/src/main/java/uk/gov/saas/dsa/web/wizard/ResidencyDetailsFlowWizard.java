package uk.gov.saas.dsa.web.wizard;

import static java.util.Arrays.asList;

import java.util.List;

import lombok.Getter;

@Getter
public enum ResidencyDetailsFlowWizard {
	
	NATIONALITY("nationality", asList("nationalityId"), "birthCountry", ""),
	BIRTH_COUNTRY("birthCountry", asList("birthCountry", "ukBirthCountryCode"), "ordResidentCountry", "nationality"),
	ORD_RES_COUNTRY("ordResidentCountry", asList("ordResidentCountry"), "dualNationality", "birthCountry"),
	DUAL_NATIONALITY("dualNationality", asList("dualNationality"), "ordResidentScot", "ordResidentCountry"),
	ORD_RES_SCOT("ordResidentScot", asList("ordResidentScot"), "inScotYear", "dualNationality"),
	IN_SCOT_YEAR("inScotYear", asList("inScotYear"), "ordResidentUK", "ordResidentScot"),
	ORD_RES_UK("ordResidentUK", asList("ordResidentUK"), "", "inScotYear");
	
	
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

	/**
	 * Constructor
	 * @param pageSection the pageSection appearing in the page URL
	 * @param subFields backing bean member variables to be validated
	 * @param next next form field appearing in the page URL
	 * @param previous previous form field appearing in the page URL
	 */
	ResidencyDetailsFlowWizard(String pageSection, List<String> subFields, String next, String previous) {
		this.pageSection = pageSection;
		this.subFields = subFields;
		this.next = next;
		this.previous = previous;
	}
}
