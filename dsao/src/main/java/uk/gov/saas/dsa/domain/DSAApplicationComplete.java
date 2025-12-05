package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * DSAApplicationsMade DB Table
 */
@Data
@Entity
@Table(name = "COMPLETE_WEB_APP_DSA", schema = "SGAS")
public class DSAApplicationComplete implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "DSA_APPLICATION_ID")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO")
	private long studentReferenceNumber;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "WEB_SUBMITTED")
	private Date webSubmitted;

	@Column(name = "DSA_APPLICATION_TYPE")
	private String applicationType;

}