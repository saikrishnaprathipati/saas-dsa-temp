package uk.gov.saas.dsa.domain;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "INST_CRSE")
public class DSAInstCrse implements Serializable {

	private static final long serialVersionUID = 19618820928936332L;

	@Id
	@Column(name = "APPLICATION_ID")
	private long applicationId;

	@Column(name = "INST_CODE")
	private String instCode;

	@Column(name = "INST_NAME")
	private String instName;

	@Column(name = "CRSE_NAME")
	private String courseName;

	@Column(name = "CURRENT_YEAR")
	private int currentYear;

	@Column(name = "YEARS_TO_COMPLETE_COURSE")
	private int yearsToCompleteCourse;

	@Column(name = "COURSE_MODE")
	private String courseMode;

	@Column(name = "PT_CREDITS_OVER_HALF")
	private String ptCreditsOverHalf;

	@Column(name = "DSA_CRSE_START_DATE")
	private String dsaCourseStartDate;

	@Column(name = "DSA_QUAL_TYPE")
	private String dsaQualificationType;

	@Column(name = "DSA_FEE_PAYER")
	private String dsaFeePayer;

	@Column(name = "DSA_ONLY_CRSE_NAME")
	private String dsaOnlyCourseName;
}