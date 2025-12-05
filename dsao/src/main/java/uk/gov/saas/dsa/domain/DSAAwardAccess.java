package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * DSA Award access for DSA advisor.
 */
@Data
@Entity
@Table(name = "DSA_AWARD_ACCESS", schema = "SGAS")
public class DSAAwardAccess implements Serializable {

	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "ADVISOR_CAN_ACCESS")
	private String advisorCanAccess;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}
