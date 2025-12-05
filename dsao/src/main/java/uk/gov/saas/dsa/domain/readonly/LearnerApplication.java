package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "LEARNER_APPLICATION", schema = "STEPS")
public class LearnerApplication implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "LEARNER_APPLICATION_ID")
	private Long learnerApplicationId;

	@Column(name = "LEARNER_ID")
	private String learnerId;

	@Column(name = "COURSE_ID")
	private Long courseId;

	@Column(name = "COURSE_TYPE_ID")
	private Long courseTypeId;

	@Column(name = "PROVIDER_ID")
	private Long providerId;

	@Column(name = "APPLICATION_STATUS_ID")
	private Long applicationsStatusId;

	@Column(name = "REJECTION_ID")
	private Long rejectionId;

	@Column(name = "TOTAL_ANNUAL_INCOME")
	private Long totalAnnualIncome;

	@Column(name = "TOT_ANN_INC_EVID_ID")
	private String totalAnnualIncomeEvidenceId;

	@Column(name = "NO_INCOME")
	private String noIncome;

	@Column(name = "NO_INCOME_EVID_ID")
	private String noIncomeEvidenceId;

	@Column(name = "JOB_SEEKERS_ALLOWANCE")
	private String jobSeekersAllowance;

	@Column(name = "JSA_EVID_ID")
	private String jobSeekersAllowanceEvidenceId;

	@Column(name = "INCOME_SUPPORT")
	private String incomeSupport;

	@Column(name = "INC_SUP_EVID_ID")
	private String incomeSupportEvidenceId;

	@Column(name = "INCAPACITY_BENEFIT")
	private String incapacityBenefit;

	@Column(name = "INC_BEN_EVID_ID")
	private String incapacityBenefitEvidenceId;

	@Column(name = "CARERS_ALLOWANCE")
	private String carersAllowance;

	@Column(name = "CARERS_ALLOWANCE_EVID_ID")
	private String carersAllowanceEvidenceId;

	@Column(name = "PENSION_CREDIT")
	private String pensionCredit;

	@Column(name = "PENSION_CREDIT_EVID_ID")
	private String pensionCreditEvidenceId;

	@Column(name = "MAX_CHILD_TAX_CREDIT")
	private String maxChildTaxCredit;

	@Column(name = "MAX_CHILD_TAX_CREDIT_EVID_ID")
	private String maxChildTaxCreditEvidenceId;

	@Column(name = "SESSION_YEAR")
	private String sessionYear;

	@Column(name = "DATE_APP_RECD")
	private Date dateApplicationReceived;

	@Column(name = "DATE_RECORD_CREATED")
	private Date dateRecordCreated;

	@Column(name = "DATE_OF_LAST_CALC")
	private Date dateOfLastCalculation;

	@Column(name = "COURSE_TITLE")
	private String courseTitle;

	@Column(name = "COURSE_START_DATE")
	private Date courseStartDate;

	@Column(name = "LENGTH_OF_COURSE")
	private Long lengthOfCourse;

	@Column(name = "CURRENT_COURSE_YEAR")
	private Long currentCourseYear;

	@Column(name = "COURSE_END_DATE")
	private Date courseEndDate;

	@Column(name = "HELP_WITH_FEES")
	private String helpWithFees;

	@Column(name = "HELP_AMOUNT")
	private Long helpAmount;

	@Column(name = "FEE_REQUESTED")
	private Long feeRequested;

	@Column(name = "PROVIDER_SIGNATURE_PRESENT")
	private String providerSignaturePresent;

	@Column(name = "ENDORSED_BY")
	private String endorsedBy;

	@Column(name = "DATE_ENDORSED")
	private Date dateEndorsed;

	@Column(name = "STAMPED")
	private String stamped;

	@Column(name = "LEARNER_SIGNATURE_PRESENT")
	private String learnerSignaturePresent;

	@Column(name = "DATE_SIGNED")
	private Date dateSigned;

	@Column(name = "FEE_PAID_BACS")
	private Long feePaidBACS;

	@Column(name = "PAYMENT_DATE")
	private Date paymentDate;

	@Column(name = "RECOVER_FEES")
	private String recoverFees;

	@Column(name = "DEBT_STATUS")
	private String debtStatus;

	@Column(name = "FEE_CALCULATED")
	private Long feeCalculated;

	@Column(name = "COMMENTS_FOR_AWARD_LETTER")
	private String commentsForAwardLetter;

	@Column(name = "AWARD_LETTER_DUPLICATE")
	private String awardLetterDuplicate;

	@Column(name = "NON_ATTENDANCE")
	private String nonAttendance;

	@Column(name = "DATE_WITHDRAWN")
	private Date dateWithdrawn;

	@Column(name = "DATE_ACTIONED")
	private Date dateActioned;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "LAST_LETTER_GENERATED")
	private Date lastLetterGenerated;

	@Column(name = "RECALCULATED_FEE")
	private Long recalculatedFee;

	@Column(name = "LAL_SENT")
	private String lalSent;

	@Column(name = "LAL_SENT_DATE")
	private Date lalSentDate;

	@Column(name = "AWARD_LETTER_NO")
	private Long awardLetterNumber;

	@Column(name = "DUP_AWARD_LETTER")
	private Long duplicateAwardLetter;
}
