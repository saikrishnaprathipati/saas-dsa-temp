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
@Table(schema = "STEPS", name = "DSA_AWARD_ACCOM_DATA")
public class DSAAwardAccommData implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "ACCOM_AN_ID")
	private long accomAnId;

	@Column(name = "DSA_AN_ID")
	private long awardNotificationId;

	@Column(name = "DSA_ALLOWANCE_ID")
	private long allowanceId;

	@Column(name = "ACCOM_ENHANCED_COST")
	private BigDecimal enhancedCost;

	@Column(name = "ACCOM_STANDARD_COST")
	private BigDecimal standardCost;

	@Column(name = "ACCOM_WEEKS")
	private int weeks;

	@Column(name = "ACCOM_TYPE")
	private String accommType;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

}