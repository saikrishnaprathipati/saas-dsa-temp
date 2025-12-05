package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DSA Application Stud Travel Exp provider
 */
@Data
@Entity
@ToString(  of = {"providerName", "isApprovedContract", "cost"})
@Table(name = "DSA_APP_STUD_TRAVEL_PROVIDER", schema = "SGAS")
@EqualsAndHashCode(of = { "providerName", "isApprovedContract", "cost", "travelExp" })
public class DSAApplicationTravelProvider implements Serializable {

	private static final String DSA_APPLICATION_TRAVEL_EXP_PROV_ID_SEQ = "dsaApplicationTravelExpProviderIdSeq";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = DSA_APPLICATION_TRAVEL_EXP_PROV_ID_SEQ, sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_TRAVEL_EXP_PROV_ID_SEQ)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TRAVEL_EXP_NO", nullable = false)
	private DSAApplicationTravelExp travelExp;

	@Column(name = "PROVIDER_NAME")
	private String providerName;

	@Column(name = "IS_APPROVED_CONTRACT")
	private String isApprovedContract;

	@Column(name = "COST", nullable = false, precision = 5, scale = 2)
	@Digits(integer = 5, fraction = 2)
	private BigDecimal cost;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}
