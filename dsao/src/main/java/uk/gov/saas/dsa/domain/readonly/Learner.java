package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "LEARNER", schema = "STEPS")
@Data
public class Learner implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "LEARNER_ID")
	private String learnerId;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "learnerId", fetch = FetchType.LAZY)
	private List<LearnerApplication> learnerApplication;

	@Column(name = "TITLE_ID")
	private Integer titleID;

	@Column(name = "OTHER_TITLE")
	private String otherTitle;

	@Column(name = "FORENAME")
	private String forename;

	@Column(name = "SURNAME")
	private String surname;

	@Column(name = "HOUSENAME_NO")
	private String houseNameNumber;

	@Column(name = "ADDRESS_LINE1")
	private String addressLine1;

	@Column(name = "ADDRESS_LINE2")
	private String addressLine2;

	@Column(name = "ADDRESS_LINE3")
	private String addressLine3;

	@Column(name = "ADDRESS_LINE4")
	private String addressLine4;

	@Column(name = "POSTCODE")
	private String postcode;

	@Column(name = "DOB")
	private Date dob;

	@Column(name = "GENDER")
	private String gender;

	@Column(name = "TELEPHONE_NO")
	private String telephoneNumber;

	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;

	@Column(name = "LIVES_SCOTLAND_FLAG")
	private String livesInScotlandFlag;

	@Column(name = "LIVES_AWAY_FLAG")
	private String livesAwayFlag;

	@Column(name = "DECEASED_FLAG")
	private String deceasedFlag;

	@Column(name = "MAIL_RETURNED_DATE")
	private String mailReturnedDate;

	@Column(name = "HOLD_PAYMENTS")
	private String holdPayments;

	@Column(name = "HOLD_LETTERS")
	private String holdLetters;

	@Column(name = "GRASS_CHECKED")
	private String checkedGRASS;

	@Column(name = "CASES_GRASS_CHECKED")
	private Integer checkedCasesGRASS;

	@Column(name = "STEPS_CHECKED")
	private String checkedSTEPS;

	@Column(name = "CASES_STEPS_CHECKED")
	private Integer checkedCasesSTEPS;

	@Column(name = "ILA200_CHECKED")
	private String checkedILA200;

	@Column(name = "CASES_ILA200_CHECKED")
	private Integer checkedCasesILA200;

	@Column(name = "ILA500_CHECKED")
	private String checkedILA500;

	@Column(name = "CASES_ILA500_CHECKED")
	private Integer checkedCasesILA500;

	@Column(name = "ADDR_MAIL_SORT")
	private String addressMailSort;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "WEB_USER_ID")
	private String webUserId;

	@Column(name = "MOBILE_TEL_NO")
	private String mobileTelNo;

	@Column(name = "NI_NO")
	private String nationalInsuranceNumber;
}
