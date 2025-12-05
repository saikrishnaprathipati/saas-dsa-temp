package uk.gov.saas.dsa.domain;

import static uk.gov.saas.dsa.domain.DSAApplicationsMade.DSA_APPLICATION_NUMBER;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;

import lombok.Data;

/**
 * DSA Application Stud Assessment fee
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_ASSESSMENT_FEE", schema = "SGAS")
public class DSAApplicationAssessmentFee implements Serializable {

	private static final String DSA_APPLICATION_ASS_FEE_ID_SEQ = "dsaApplicationTravelExpIdSeq";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = DSA_APPLICATION_ASS_FEE_ID_SEQ, sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_ASS_FEE_ID_SEQ)
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = DSA_APPLICATION_NUMBER)
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "ASSESSMENT_CENTRE_NAME")
	private String assessmentCentreName;

	@Column(name = "ASSESSOR_NAME")
	private String assessorName;

	@Column(name = "TOTAL_HOURS")
	private int totalHours;

	@Column(name = "COST", nullable = false, precision = 5, scale = 2)
	@Digits(integer = 5, fraction = 2)
	private BigDecimal cost;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}
