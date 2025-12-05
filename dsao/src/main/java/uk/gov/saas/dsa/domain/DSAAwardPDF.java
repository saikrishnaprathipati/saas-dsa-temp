package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * DSA Application PDF
 */
@Data
@Entity
@Table(name = "DSA_AWARD_PDF", schema = "SGAS")
public class DSAAwardPDF implements Serializable {
	private static final long serialVersionUID = 7450828755008099226L;
	@Id
	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "STUD_CRSE_YR_ID")
	private int courseYearId;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "AWARD_PDF")
	private Blob awardPDF;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}