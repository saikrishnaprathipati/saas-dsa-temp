package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * The Class DsaAdvisor.
 */
@Data
@Entity
@Table(name = "DSA_ROLES", schema = "SGAS")
public class DsaRoles implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9182589405080215L;

	@Id
	@Column(name="ROLE_ID")
	private Long roleId;

	@Column(name = "ROLE_NAME")
	private String roleName;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;
}
