package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;

import lombok.Data;

/**
 * DSA Application Stud NMPH Allowances
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_NMPH", schema = "SGAS")
public class DSAApplicationStudNMPH implements Serializable {

	private static final String APP_ID_SEQ = "APP_ID_SEQ";

	private static final String DSA_APPLICATION_NMPH_ID_SEQ = "dsaApplicationConsumablesIdSeq";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = DSA_APPLICATION_NMPH_ID_SEQ, sequenceName = APP_ID_SEQ, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_NMPH_ID_SEQ)
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "TYPE_OF_SUPPORT")
	private String typeOfSupport;

	@Column(name = "RECOMMENDED_PROVIDER")
	private String recommendedProvider;

	@Column(name = "HOURLY_RATE", nullable = false, precision = 5, scale = 2)
	@Digits(integer = 5, fraction = 2)
	private BigDecimal hourlyRate;

	@Column(name = "HOURS_PER_WEEK")
	private int hoursPerWeek;

	@Column(name = "WEEKS")
	private int weeks;

	@Column(name = "COST", nullable = false, precision = 5, scale = 2)
	@Digits(integer = 11, fraction = 2)
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
