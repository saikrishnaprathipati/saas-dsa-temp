package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

@Data
@Entity
@Table(name = "DSA_ADVISOR_LOGIN_DETAILS", schema = "SGAS")
@DynamicUpdate(value = true)
public class DsaAdvisorLoginDetails implements Serializable {

	private static final long serialVersionUID = -212223L;
	
	@Column(name = "USER_ID")
	private String userId;

	@Id
	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "PREVIOUS_PASSWORD1")
	private String previousPassword1;

	@Column(name = "PREVIOUS_PASSWORD2")
	private String previousPassword2;

	@Column(name = "SALT")
	private String salt;

	@Column(name = "SECRET_KEY")
	private String secretKey;

	@Column(name = "ROLE")
	private String role;

	@Column(name = "FAILED_PASSWORD_COUNT")
	private int failedPasswordCount;

	@Column(name = "IS_ACTIVE")
	private Boolean isActive;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}
