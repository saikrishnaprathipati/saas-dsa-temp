package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.AccommodationTypeConverter;
import uk.gov.saas.dsa.model.AccommodationType;

/**
 * DSA Application Stud accommodation
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_ACCOMMODATION", schema = "SGAS")
public class DSAApplicationStudAccommodation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "dsaApplicationAccommodationIdSeq", sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dsaApplicationAccommodationIdSeq")
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "ACCOMMODATION_TYPE")
	@Enumerated(EnumType.STRING)
	@Convert(converter = AccommodationTypeConverter.class)
	private AccommodationType accommodationType;

	@Column(name = "ENHANCED_COST", nullable = true, precision = 5, scale = 2)
	@Digits(integer = 5, fraction = 2)
	private BigDecimal enhancedCost;

	@Column(name = "STANDARD_COST", nullable = true, precision = 5, scale = 2)
	@Digits(integer = 5, fraction = 2)
	private BigDecimal standardCost;

	@Column(name = "WEEKS")
	private int weeks;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}
