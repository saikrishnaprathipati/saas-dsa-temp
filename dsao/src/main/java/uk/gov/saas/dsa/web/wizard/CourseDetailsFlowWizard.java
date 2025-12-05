package uk.gov.saas.dsa.web.wizard;

import static java.util.Arrays.asList;

import java.util.List;

import lombok.Getter;

@Getter
public enum CourseDetailsFlowWizard {
	
	COURSE_MODE("courseMode", asList("courseMode"), "dsaQualificationType", ""),
	QUALIFICATION_TYPE("dsaQualificationType", asList("dsaQualificationType", "customQualType"), "institution", "courseMode"),
	INSTITUTION("institution", asList("instName"), "course", "dsaQualificationType"),
	COURSE("course", asList("courseName"), "courseStart", "institution"),
	COURSE_START("courseStart", asList("startMonth", "startYear"), "courseDuration", "course"),
	COURSE_DURATION("courseDuration", asList("yearsToCompleteCourse"), "courseYear", "courseStart"),
	COURSE_YEAR("courseYear", asList("currentYear"), "", "courseDuration");

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

	CourseDetailsFlowWizard(String pageSection, List<String> subFields, String next, String previous) {
		this.pageSection = pageSection;
		this.next = next;
		this.previous = previous;
		this.subFields = subFields;
	}
}
