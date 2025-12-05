package uk.gov.saas.dsa.model;

import static uk.gov.saas.dsa.model.ApplicationSectionPart.PART1;
import static uk.gov.saas.dsa.model.ApplicationSectionPart.PART2;
import static uk.gov.saas.dsa.model.ApplicationSectionPart.PART3;

import lombok.Getter;

/**
 * Application Section details.
 */
@Getter
public enum Section {

	ABOUT_STUDENT("ABOUT_STUDENT", "About student", PART1, 1), ABOUT_COURSE("ABOUT_COURSE", "About Course", PART1, 2),

	DISABILITIES("DISABILITIES", "Disabilities", PART2, 1), ALLOWANCES("ALLOWANCES", "Allowances", PART2, 2),
	NEEDS_ASSESSMENT_FEE("NEEDS_ASSESSMENT_FEE", "Needs Assessment Fee", PART2, 3),
	ADDITIONAL_INFO("ADDITIONAL_INFO", "Additional information", PART2, 4),

	ADVISOR_DECLARATION("ADVISOR_DECLARATION", "Advisor", PART3, 1),
	STUDENT_DECLARATION("STUDENT_DECLARATION", "Student", PART3, 2);

	private String code;
	private String description;
	private ApplicationSectionPart sectionPart;
	private int orderOfCompletion;

	Section(String sectionCode, String sectionDesc, ApplicationSectionPart sectionPart,
			int orderOfCompletion) {
		this.code = sectionCode;
		this.description = sectionDesc;
		this.sectionPart = sectionPart;
		this.orderOfCompletion = orderOfCompletion;
	}

}