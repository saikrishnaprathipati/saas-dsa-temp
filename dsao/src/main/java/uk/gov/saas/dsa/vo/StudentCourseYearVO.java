package uk.gov.saas.dsa.vo;

import lombok.Data;

@Data
public class StudentCourseYearVO {
	private String institutionName;
	private Integer sessionCode;
	private String academicYear;
	private String academicYearFull;
	private String courseName;

}
