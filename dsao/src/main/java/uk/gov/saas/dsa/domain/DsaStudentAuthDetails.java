package uk.gov.saas.dsa.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "DSA_STUDENT_AUTH_DETAILS", schema = "SGAS")
@DynamicUpdate(value = true)
public class DsaStudentAuthDetails implements Serializable {

	private static final long serialVersionUID = -1214L;

	@Id
	@Column(name = "SUID")
	private String suid;

	@Column(name = "STUD_REF_NO")
	private Long studRefNumber;

	@Column(name = "DSA_APPLICATION_NO")
	private Long dsaApplicationNumber;

	@Column(name = "ACTIVATION_TOKEN")
	private String activationToken;

	@Column(name = "ACTIVATION_REQUEST_DATE")
	private Timestamp activationRequestDate;

	@Column(name = "IS_LOGGED_IN")
	private Boolean isLoggedIn;

	@Column(name = "LAST_LOGGED_IN_DATE")
	private Timestamp lastLoggedInDate;
}
