package uk.gov.saas.dsa.vo;

import lombok.Data;

/**
 * Course details VO
 */
@Data
public class CourseDetailsVO {
	private String institutionName;
	private String courseName;
	private String academicYear;
	private String academicYearFull;
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
}
