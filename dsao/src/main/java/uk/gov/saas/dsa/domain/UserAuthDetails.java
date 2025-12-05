package uk.gov.saas.dsa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

import org.hibernate.annotations.DynamicUpdate;


@Entity
@Table(name="USER_AUTH_DETAILS")
@DynamicUpdate(value = true)
public class UserAuthDetails implements Serializable {
	
	private static final long serialVersionUID = 5L;
	
	@Id
	@Column(name="USER_ID")
//	@NotBlank(message = "UserID is mandatory")
	private String userId;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	@MapsId
	private UserPersonalDetails user;
	
	@Column(name="PASSWORD")
	private String password;

	@Column(name="AUTHORITY")
//	@NotBlank(message = "Authority is mandatory")
	private int authority;

	@Column(name="FAILED_PASSWORD_COUNT")
	private int failedPasswordCount;
	
	@Column(name="TEMPORARY_LOCK_COUNT")
	private int temporaryLockCount;

	@Column(name="LAST_LOGIN_TOKEN")
	private String lastLoginToken;



	/**NOT USED
	 *@Column(name="PREVIOUS_PASSWORD1")
	 *@Column(name="PREVIOUS_PASSWORD2")
	 *@Column(name="USERNAME")
	 *@Column(name="PASSWORD_SET_DATE")
	 *private Date passwordSetDate;
	 */

	public int getAuthority(){return authority;}

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public UserPersonalDetails getUser() {
		return user;
	}

	public void setUser(UserPersonalDetails user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getFailedPasswordCount() {
		return failedPasswordCount;
	}

	public void setFailedPasswordCount(int failedPasswordCount) {
		this.failedPasswordCount = failedPasswordCount;
	}

	public int getTemporaryLockCount() {
		return temporaryLockCount;
	}

	public void setTemporaryLockCount(int temporaryLockCount) {
		this.temporaryLockCount = temporaryLockCount;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getLastLoginToken() {
		return lastLoginToken;
	}

	public void setLastLoginToken(String lastLoginToken) {
		this.lastLoginToken = lastLoginToken;
	}

	public UserAuthDetails() {
		super();
	}

	/**
	 * @param userId
	 * @param user
	 * @param password
	 * @param authority
	 * @param failedPasswordCount
	 * @param temporaryLockCount
	 * @param lastLoginToken
	 */
	public UserAuthDetails(String userId, UserPersonalDetails user, String password, int authority, int failedPasswordCount,
			int temporaryLockCount, String lastLoginToken) {
		super();
		this.userId = userId;
		this.user = user;
		this.password = password;
		this.authority = authority;
		this.failedPasswordCount = failedPasswordCount;
		this.temporaryLockCount = temporaryLockCount;
		this.lastLoginToken = lastLoginToken;
	}
	
	
}
