package uk.gov.saas.dsa.vo.travelExp;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.saas.dsa.model.TravelExpType;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = {"startLocationPostcode", "endLocationPostcode", "returnJourneys", "weeks"})
public class TravelExpAllowanceVO {
	private long travelExpNo;
	private long dsaApplicationNumber;
	private long id;
	private long studentReferenceNumber;
	private TravelExpType travelExpType;
	private String startLocationPostcode;
	private String endLocationPostcode;
	private Integer returnJourneys;
	private Integer weeks;
	private List<TaxiProviderVO> taxiProvidersList;
	private String vehicleType;
	private BigDecimal fuelCost;
	private String fuelCostStr;
	private Integer milesPerGallon;
	private BigDecimal kwhCost;
	private String kwhCostStr;
	private Integer kwhCapacity;
	private Integer rangeOfCar;
}
