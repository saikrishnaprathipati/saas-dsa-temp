package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

@Data
@Entity
@Table(name = "DSA_ADVISOR_AUTH_DETAILS", schema = "SGAS")
@DynamicUpdate(value = true)
public class DsaAdvisorAuthDetails implements Serializable {

	private static final long serialVersionUID = -22223L;

	@Id
	@Column(name = "ADVISOR_ID")
	@SequenceGenerator(name = "dsaAdvisorIdSeq", sequenceName = "ADVISOR_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dsaAdvisorIdSeq")
	private Long advisorId;

	@Column(name = "EMAIL")
	private String email;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EMAIL", insertable = false, updatable = false)
	private DsaAdvisor dsaAdvisor;

	@Column(name = "ACCOUNT_ACTIVATION_STATUS")
	private String activationStatus;

	@Column(name = "ACCOUNT_ACTIVATION_TOKEN")
	private String activationToken;

	@Column(name = "ACTIVATION_REQUEST_DATE")
	private Timestamp activationRequestDate;

	@Column(name = "TEMPORARY_LOCK_DATE_TIME")
	private Timestamp temporaryLockDate;

	@Column(name = "IS_LOGGED_IN")
	private Boolean isLoggedIn;

	@Column(name = "IS_TEMPORARY_LOCKED")
	private Boolean isTemporaryLocked;

	@Column(name = "IS_PERMANENT_LOCKED")
	private Boolean isPermanentLocked;

	@Column(name = "LAST_LOGGED_IN_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date lastLoggedInDate;

	@Column(name = "PASSWORD_RESET_TOKEN")
	private String passwordResetToken;

	@Column(name = "PASSWORD_RESET_REQ_DATE_TIME")
	private Timestamp passwordResetReqDate;

	@Column(name = "PROFILE_SAVED_DATE")
	private Date profileSavedDate;

	@Column(name = "REGISTERED_DATE")
	private Date registeredDate;

	@Column(name = "PERMANENT_LOCK_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date permanentLoginDate;

	@Column(name = "PREVIOUS_LOGIN_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date previousLoginDate;
	
	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "ROLE_NAME")
	private String roleName;
}
