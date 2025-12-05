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
@Table(name = "DSA_ADVISOR", schema = "SGAS")
public class DsaAdvisor implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9182589405080234815L;

	@Column(name="ADVISOR_ID")
	private Long advisorId;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Id
	@Column(name = "EMAIL")
	private String email;

	@Column(name = "INSTITUTION")
	private String institution;

	@Column(name = "TEAM_EMAIL")
	private String teamEmail;

	@Column(name = "INST_CODE")
	private String instCode;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;

	@Column(name = "IS_ACTIVE")
	private boolean isActive;

	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "ROLE_NAME")
	private String roleName;
}
