package uk.gov.saas.dsa.domain.readonly;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.util.List;

/**
 * The Class Stud.
 */
@Data
@Entity
@Table(name = "STUD", schema = "STEPS")
public class Stud implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	/**
	 * The student reference number.
	 */
	@Id
	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	/**
	 * The web user id.
	 */
	@Column(name = "WEB_USER_ID")
	private String webUserId;
	@Column(name = "DOB")
	private Date dob;

	/**
	 * The title.
	 */
	@Column(name = "TITLE")
	private String title;

	/**
	 * The forenames.
	 */
	@Column(name = "FORENAMES")
	private String forenames;

	/**
	 * The surname.
	 */
	@Column(name = "SURNAME")
	private String surname;

	/**
	 * The national insurance number.
	 */
	@Column(name = "NI_NO")
	private String nationalInsuranceNumber;

	/**
	 * The mobile telephone number.
	 */
	@Column(name = "MOBILE_TEL_NO")
	private String mobileTelephoneNumber;

	@Column(name = "SEX")
	private String sex;

	/**
	 * The marital status code.
	 */
	@Column(name = "MARITAL_STATUS")
	private String maritalStatus;

	/**
	 * The account number.
	 */
	@Column(name = "ACCOUNT_NO")
	private String accountNumber;

	/**
	 * The sort code.
	 */
	@Column(name = "SORT_CODE")
	private String sortCode;

	/**
	 * The email address.
	 */
	@Column(name = "EMAIL_ADDR")
	private String emailAddress;

	/**
	 * The district birth cert issued.
	 */
	@Column(name = "DISTRICT_BIRTH_CERT_ISSUED")
	private String districtBirthCertIssued;

	/**
	 * The residency category.
	 */
	@Column(name = "RESIDENCY_CATEGORY")
	private String residencyCategory;

	/**
	 * The addr corr flag.
	 */
	@Column(name = "ADDR_CORR_FLAG")
	private String addrCorrFlag;

	/**
	 * The care exp evidence recvd.
	 */
	@Column(name = "CARE_EXP_EVIDENCE_RECVD")
	private String careExpEvidenceReceived;

	/**
	 * The Estranged Flag.
	 */
	@Column(name = "ESTRANGED")
	private String estranged;

	/**
	 * The EU SETTLED STATUS.
	 */
	@Column(name = "EU_SETTLED_STATUS")
	private Long euSettledStatus;

	/**
	 * The EU SETTLED STATUS CONFIRMED.
	 */
	@Column(name = "EU_SETTLED_STATUS_CONFIRMED")
	private String euSettledStatusConfirmed;

	@Column(name = "BIRTH_COUNTRY_CODE")
	private String birthCountryCode;

	@Column(name = "RESIDENCE_COUNTRY_CODE")
	private String resCountryCode;

	@Column(name = "NATION_COUNTRY_CODE")
	private String nationCountryCode;

	@Column(name = "ORD_RES_UK")
	private String ordResUK;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "studentReferenceNumber", fetch = FetchType.LAZY)
	private List<StudCourseYear> studCourseYear;
}