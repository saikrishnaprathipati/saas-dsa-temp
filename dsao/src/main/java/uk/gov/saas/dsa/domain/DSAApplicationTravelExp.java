package uk.gov.saas.dsa.domain;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.TravelExpTypeConverter;
import uk.gov.saas.dsa.model.TravelExpType;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static uk.gov.saas.dsa.domain.DSAApplicationsMade.DSA_APPLICATION_NUMBER;

/**
 * DSA Application Stud Travel Exp
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_TRAVEL_EXP", schema = "SGAS")
public class DSAApplicationTravelExp implements Serializable {

	private static final String DSA_APPLICATION_TRAVEL_EXP_ID_SEQ = "dsaApplicationTravelExpIdSeq";
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "TRAVEL_EXP_NO")
	@SequenceGenerator(name = DSA_APPLICATION_TRAVEL_EXP_ID_SEQ, sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_TRAVEL_EXP_ID_SEQ)
	private Long travelExpNo;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = DSA_APPLICATION_NUMBER)
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "TRAVEL_EXP_TYPE")
	@Enumerated(EnumType.STRING)
	@Convert(converter = TravelExpTypeConverter.class)
	private TravelExpType travelExpType;

	@Column(name = "START_LOCATION_POSTCODE")
	private String startLocationPostcode;

	@Column(name = "END_LOCATION_POSTCODE")
	private String endLocationPostcode;

	@Column(name = "RETURN_JOURNEYS")
	private int returnJourneys;

	@Column(name = "WEEKS_PER_ACADEMIC_YEAR")
	private int weeks;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

	@Column(name = "VEHICLE_TYPE")
	private String vehicleType;

	@Column(name = "FUEL_COST", nullable = false, precision = 7, scale = 2)
	@Digits(integer = 7, fraction = 2)
	private BigDecimal fuelCost;

	@Column(name = "MILES_PER_GALLON", nullable = false, precision = 2)
	private int milesPerGallon;

	@Column(name = "KWH_COST", nullable = false, precision = 2)
	private BigDecimal kwhCost;

	@Column(name = "KWH_CAPACITY", nullable = false, precision = 2)
	private int kwhCapacity;

	@Column(name = "RANGE_OF_CAR", nullable = false, precision = 3)
	private int rangeOfCar;

	@OneToMany(mappedBy = "travelExp", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<DSAApplicationTravelProvider> travelProviders;

}
