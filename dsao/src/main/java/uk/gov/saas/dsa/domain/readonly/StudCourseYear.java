package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "STUD_CRSE_YEAR", schema = "STEPS")
public class StudCourseYear implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 3409753711843795966L;

	@Id
	@Column(name = "STUD_CRSE_YEAR_ID")
	private long studentCourseYearId;

	/**
	 * The student reference number.
	 */
	@Column(name = "STUD_REF_NO")
	private long studentReferenceNumber;

	/**
	 * The inst code.
	 */
	@Column(name = "INST_CODE")
	private String instCode;

	/**
	 * The inst name.
	 */
	@Column(name = "INST_NAME")
	private String instituteName;

	@Column(name = "CRSE_NAME")
	private String courseName;

	@Column(name = "LATEST_CRSE_IND")
	private String latestCourseIndicator;

	/**
	 * The application status.
	 */
	@Column(name = "APPLICATION_STATUS")
	private String applicationStatus;

	@Column(name = "STUD_SESSION_ID")
	private long studentSessionId;

	@Column(name = "SESSION_CODE")
	private Integer sessionCode;

	@Column(name = "GA_STUDENT")
	private String gaStudent;
}