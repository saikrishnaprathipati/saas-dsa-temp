package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * DSAApplicationStudDisabilities
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_DISABILITIES", schema = "SGAS")

public class DSAApplicationStudDisabilities implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "dsaApplicationDisabilitiesIdSeq", sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dsaApplicationDisabilitiesIdSeq")
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "DISABILITY_TYPE_CODE")
	private String disabilityTypeCode;

	@Column(name = "DISABILITY_NOTLISTED_TEXT")
	private String disabilityNotlistedText;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}
