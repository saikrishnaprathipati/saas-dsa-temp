package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.math.BigDecimal;
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

@Data
@Entity
@Table(schema = "STEPS", name = "DSA_AWARD_NOTICE")
public class DSAAwardNotice implements Serializable {

	private static final String AWARD_NOTIFICATION_ID = "awardNotificationId";

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "DSA_AN_ID")
	private long awardNotificationId;

	@Column(name = "DSA_APPLICATION_ID")
	private long dsaApplicationId;

	@Column(name = "STUD_REF_NO")
	private long studentReferenceNumber;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "CRSE_CODE")
	private String courseCode;

	@Column(name = "INST_CODE")
	private String instituteCode;

	@Column(name = "DSA_AN_STATUS")
	private String awardNoticeStatus;

	@Column(name = "PUBLISHED_DATE")
	private Date publishedDate;

	@Column(name = "ACCOM_AMT")
	private BigDecimal accomTotal;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = AWARD_NOTIFICATION_ID, fetch = FetchType.LAZY)
	private List<DSAAwardTravelData> travelDataElements;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = AWARD_NOTIFICATION_ID, fetch = FetchType.LAZY)
	private List<DSAAwardAccommData> accommodations;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = AWARD_NOTIFICATION_ID, fetch = FetchType.LAZY)
	private List<DSAAwardItemisedData> itemisedDataElements;

	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = AWARD_NOTIFICATION_ID, fetch = FetchType.LAZY)
	private List<DSAAwardNMPHData> nmphDataElements;

}