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
@Table(schema = "STEPS", name = "DSA_AWARD_NMPH_DATA")
public class DSAAwardNMPHData implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "NMPH_AN_ID")
	private long nmphId;

	@Column(name = "DSA_AN_ID")
	private long awardNotificationId;

	@Column(name = "DSA_ALLOWANCE_ID")
	private long allowanceId;

	@Column(name = "TYPE_OF_SUPPORT")
	private String typeOfSupport;

	@Column(name = "PROVIDER")
	private String provider;

	@Column(name = "HOURLY_RATE")
	private BigDecimal hourleyRate;

	@Column(name = "HOURS")
	private int hours;

	@Column(name = "WEEKS")
	private int weeks;

	@Column(name = "COST")
	private BigDecimal cost;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

}