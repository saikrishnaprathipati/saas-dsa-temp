package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "STEPS", name = "DSA_AWARD_ITEMISED_DATA")
public class DSAAwardItemisedData implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "DSA_ITEMISED_ID")
	private long itemisedId;

	@Column(name = "DSA_AN_ID")
	private long awardNotificationId;

	@Column(name = "DSA_ALLOWANCE_ID")
	private long allowanceId;

	@Column(name = "DSA_ALLOWANCE_ITEM_ID")
	private long allowanceItemId;

	@Column(name = "TYPE")
	private String type;

	@Column(name = "ITEM_TYPE")
	private String itemType;

	@Column(name = "PRODUCT_NAME")
	private String productName;

	@Column(name = "PRODUCT_DESCRIPTION")
	private String productDescription;
	 
	@Column(name = "PAY_HEI")
	private String payToHEI;
	
	@Column(name = "PRODUCT_COST")
	private BigDecimal productCost;

}