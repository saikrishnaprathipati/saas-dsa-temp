package uk.gov.saas.dsa.model;

import static java.util.Arrays.asList;
import static uk.gov.saas.dsa.model.CourseMode.DISTANCE_LEARNING;
import static uk.gov.saas.dsa.model.CourseMode.FULL_TIME;
import static uk.gov.saas.dsa.model.CourseMode.PART_TIME;

import java.util.List;

import lombok.Getter;

/**
 * Course type
 * 
 * @author Siva Chimpiri
 *
 */
@Getter
public enum FundingType {

	TUTION_FEE("Tution fees", asList(FULL_TIME.name())),
	STUDENT_BURSERY("Student Bursary", asList(FULL_TIME.name())),
	PARTTIME_FEE_GRANT("Part-time Fee grant", asList(PART_TIME.name(), DISTANCE_LEARNING.name())),
	PG_TUTION_FEE("Postgraduate Tution Fee Loan", asList(PART_TIME.name())),
	STUDENT_LOAN("Student loan", asList(FULL_TIME.name())),
	DSA("Disabled Students' Allowance (DSA)", asList(FULL_TIME.name(), PART_TIME.name(), DISTANCE_LEARNING.name()));

	private String description;
	private List<String> courseModes;

	FundingType(String description, List<String> courseModes) {
		this.description = description;
		this.courseModes = courseModes;
	}

}