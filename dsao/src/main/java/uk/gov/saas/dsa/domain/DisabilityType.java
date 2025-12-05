package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;

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
@Table(name = "DSA_DISABILITY_TYPE", schema = "SGAS")
public class DisabilityType implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7450828755008099226L;
	@Id
	@Column(name = "DISABILITY_TYPE_ID", columnDefinition = "disability type id")
	private long disabilityTypeId;

	@Column(name = "DISABILITY_TYPE_CODE")
	private String disabilityTypeCode;
	
	@Column(name = "DISABILITY_TYPE_DESC")
	private String disabilityTypeDesc;

	@Column(name = "DISABILITY_TYPE_HINT_TEXT")
	private String disabilityTypeHintText;

	
	@Column(name = "IS_ACTIVE")
	private String isActive;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}