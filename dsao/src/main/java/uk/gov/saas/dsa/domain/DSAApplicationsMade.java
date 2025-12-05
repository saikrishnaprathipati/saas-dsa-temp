package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.ApplicationStatusConverter;
import uk.gov.saas.dsa.domain.converters.OverallApplicationStatusConverter;
import uk.gov.saas.dsa.model.ApplicationSummaryStatus;
import uk.gov.saas.dsa.model.OverallApplicationStatus;

/**
 * DSAApplicationsMade DB Table
 */
@Data
@Entity
@Table(name = "DSA_APPLICATIONS_MADE", schema = "SGAS")
public class DSAApplicationsMade implements Serializable {

	private static final String DSA_APPLICATION_NO_SEQ = "dsaApplicationNoSeq";

	public static final String DSA_APPLICATION_NUMBER = "dsaApplicationNumber";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7450828755008099226L;

	@Id
	@SequenceGenerator(name = DSA_APPLICATION_NO_SEQ, sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_NO_SEQ)
	@Column(name = "DSA_APPLICATION_NO", columnDefinition = DSA_APPLICATION_NUMBER)
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	//To show the application summary status 
	@Column(name = "APP_SUMMARY_STATUS")
	@Enumerated(EnumType.STRING)
	@Convert(converter = ApplicationStatusConverter.class)
	private ApplicationSummaryStatus applicationSummaryStatus;

	// To show overall application status in the dashboard.
	@Column(name = "APP_STATUS")
	@Enumerated(EnumType.STRING)
	@Convert(converter = OverallApplicationStatusConverter.class)
	private OverallApplicationStatus overallApplicationStatus;

	@Column(name = "SESSION_CODE", columnDefinition = "sessionCode")
	private int sessionCode;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.EAGER)
	private List<DSAApplicationSectionStatus> dsaApplicationSectionStatus;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationStudDisabilities> applicationStudDisabilities;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationStudConsumables> applicationStudConsumables;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationStudNMPH> applicationStudNMPHAllowances;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationStudEquipment> applicationStudequipments;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAQuotePDF> applicationQuotes;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationTravelExp> travelExpenses;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationStudAccommodation> accommodations;
	
	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationAssessmentFee> assessmentFeeList;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = DSA_APPLICATION_NUMBER, fetch = FetchType.LAZY)
	private List<DSAApplicationBankAccount> bankAccounts;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "DSA_APPLICATION_NO")
	private DSAApplicationPDF dsaApplicationPDF;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DSA_APPLICATION_NO")
	private DSAAwardAccess awardAccess; 
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DSA_APPLICATION_NO")
	private DSAAppAdditionalInformation additionalInfo;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}