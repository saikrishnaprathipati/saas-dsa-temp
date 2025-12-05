package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "DSA_ADVISOR_DEVICE_METADATA", schema = "SGAS")
public class DeviceMetadata implements Serializable {

	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@SequenceGenerator(name = "deviceIdSeq", sequenceName = "DEVICE_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deviceIdSeq")
	private Long id;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "EMAIL_ID")
	private String emailId;
	
	@Column(name = "DEVICE_DETAILS")
	private String deviceDetails;

	@Column(name = "PLATFORM")
	private String platform;

	@Column(name = "BROWSER")
	private String browser;

	@Column(name = "IP_ADDRESS")
	private String ipAddress;
	
	@Column(name = "LAST_LOGGED_IN")
	private Timestamp lastLoggedIn;
	
	@Column(name = "DEVICE_VERIFICATION_DATE")
	private Timestamp deviceVerificationDate;
	
	@Column(name = "DEVICE_VERIFICATION_STATUS")
	private String deviceVerificationStatus;
	
	@Column(name = "DEVICE_VERIFICATION_TOKEN")
	private String deviceVerificationToken;
	
	@Column(name = "REMEMBER_DEVICE")
	private boolean rememberDevice;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeviceMetadata that = (DeviceMetadata) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getUserId(), that.getUserId())
				&& Objects.equals(getDeviceDetails(), that.getDeviceDetails())
				&& Objects.equals(getBrowser(), that.getBrowser())
				&& Objects.equals(getLastLoggedIn(), that.getLastLoggedIn());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getUserId(), getDeviceDetails(), getBrowser(), getLastLoggedIn());
	}
}