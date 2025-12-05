package uk.gov.saas.dsa.vo.travelExp;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class TravelExpAwardAllowanceVO {
	String transportType;
	String startLocation;
	String endLocation;
	Integer returnJourneys;
	Integer weeks;
	String costStr;
	Double cost;
	Double maxAmount;
	String maxAmountStr;
	

}
