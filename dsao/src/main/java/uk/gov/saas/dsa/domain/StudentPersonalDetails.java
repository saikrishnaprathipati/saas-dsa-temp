package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "STUDENT_PERSONAL_DETAILS")
@DynamicUpdate(value = true)
@Getter
@Setter
@ToString(exclude = "user")
public class StudentPersonalDetails implements Serializable {

	private static final long serialVersionUID = 40L;

	@Id
	@Column(name = "USER_ID")
	private String userId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	@MapsId
	private UserPersonalDetails user;

	@Column(name = "STUD_REF_NO")
	private long studentRefNumber;

	@Column(name = "LEARNER_ID")
	private String learnerId;

	@Column(name = "DATE_OF_BIRTH")
	private Date dateOfBirth;

	@Column(name = "HOUSE_NO_NAME")
	private String houseNumbName;

	@Column(name = "ADDR_L1")
	private String addressLine1;

	@Column(name = "ADDR_L2")
	private String addressLine2;

	@Column(name = "ADDR_L3")
	private String addressLine3;

	@Column(name = "ADDR_L4")
	private String addressLine4;

	@Column(name = "POST_CODE")
	private String postCode;

	@Column(name = "HOME_PHONE")
	private String homePhone;

	@Column(name = "NI_NUMBER")
	@NotBlank(message = "NI Number is mandatory")
	private String niNumber;

	@Column(name = "HELP_VIDEO_DISMISSED")
	private int helpVideoDismissed;

	@Column(name = "IS_EU_STUDENT")
	private int isEUStudent;

	@Column(name = "EU_RESIDENCE_TYPE")
	private Long euResidenceType;

	@Column(name = "SEX")
	@NotBlank(message = "Gender is mandatory")
	private String sex;

	@Column(name = "MARITAL_STATUS_CODE")
	@NotBlank(message = "Marital status is mandatory")
	private String maritalStatusCode;
 

	@Column(name = "RESIDENCY_CATEGORY")
	private String residencyCategory;

	@Column(name = "ACCEPTED_PRIVACY_STATEMENT")
	private Integer privacyStatementAccepted;

	@Column(name = "PRIVACY_STATEMENT_DATE")
	private Date privacyStatementAcceptedDate;

}
