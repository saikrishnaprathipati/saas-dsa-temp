package uk.gov.saas.dsa.domain;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DSAApplicationsMade DB Table
 */
@Data
@Entity
@Table(name = "DSA_DECLARATION_TYPE", schema = "SGAS")
public class DeclarationType implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7450828755008099226L;
	@Id
	@Column(name = "DECLARATION_TYPE_ID")
	private long declarationTypeId;

	@Column(name = "DECLARATION_TYPE_CODE")
	private String declarationTypeCode;
	
	@Column(name = "DECLARATION_TYPE_DESC")
	private String declarationTypeDesc;

	@Column(name = "DECLARATION_FOR")
	private String declarationFor;

	@Column(name = "IS_ACTIVE")
	private String isActive;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}