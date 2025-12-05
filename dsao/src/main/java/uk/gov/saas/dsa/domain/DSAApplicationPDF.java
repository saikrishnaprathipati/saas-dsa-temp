package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Blob;

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
@Table(name = "DSA_APPLICATION_PDF", schema = "SGAS")
public class DSAApplicationPDF implements Serializable {
	private static final long serialVersionUID = 7450828755008099226L;
	@Id
	@Column(name = "DSA_APPLICATION_ID", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "LEARNER_ID")
	private String learnerId;

	@Column(name = "PDF")
	private Blob pdf;


}