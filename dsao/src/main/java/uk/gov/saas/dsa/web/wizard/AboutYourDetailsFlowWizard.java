package uk.gov.saas.dsa.web.wizard;

import static java.util.Arrays.asList;

import java.util.List;

import lombok.Getter;

@Getter
public enum AboutYourDetailsFlowWizard {
	FIRST_AND_LAST_NAME("firstAndLastName", asList("forename", "surname", "titleType"), "dob", ""),
	DATE_OF_BIRTH("dob", asList("dobDay", "dobMonth", "dobYear", "dateOfBirth"), "gender", "firstAndLastName"),
	GENDER("gender", asList("gender"), "niNumber", "dob"),
	NI_NUMBER("niNumber", asList("niNumber"), "postCodeSearch", "gender"),
	POST_CODE_SEARCH("postCodeSearch", asList("postCode"), "addressSelection", "niNumber"),
	ADDRESS_SELECTION("addressSelection", asList("selectedAddress"), "phoneNumbers", "postCodeSearch"),
	MANUAL_ADDRESS("manualAddress",
			asList("postCode", "manualHouseNumber", "manualAddress1", "manualAddress2", "manualAddress3",
					"manualAddress4"),
			"phoneNumbers", "postCodeSearch"),
	PHONE_NUMBERS("phoneNumbers", asList("mobileNumber", "homePhoneNumber"), "", "addressSelection,manualAddress");

	/**
	 * This is the current page section appearing in the page URL
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

	AboutYourDetailsFlowWizard(String pageSection, List<String> subFields, String next, String previous) {
		this.pageSection = pageSection;
		this.next = next;
		this.previous = previous;
		this.subFields = subFields;
	}
}
