package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "STEPS", name = "DSA_AWARD_TRAVEL_DATA")
public class DSAAwardTravelData implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "DSA_TRAVEL_DATA_ID")
	private long travelDataId;

	@Column(name = "DSA_AN_ID")
	private long awardNotificationId;

	@Column(name = "DSA_ALLOWANCE_ID")
	private long allowanceId;

	@Column(name = "TRAVEL_JOURNEYS")
	private int travelJourneys;

	@Column(name = "TRAVEL_COSTS")
	private BigDecimal travelCost;

	@Column(name = "MAX_AMOUNT")
	private BigDecimal maxAmount;
	
	@Column(name = "TRAVEL_WEEKS")
	private int travelWeeks;

	@Column(name = "RECOMMENDED_PROVIDER")
	private String recomendedProvider;

	@Column(name = "TRAVEL_TYPE")
	private String travelType;

	@Column(name = "START_POSTCODE")
	private String startPostcode;

	@Column(name = "END_POSTCODE")
	private String endPostcode;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

}