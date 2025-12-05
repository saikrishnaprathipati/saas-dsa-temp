package uk.gov.saas.dsa.vo.accommodation;

import java.math.BigDecimal;

import lombok.Data;
import uk.gov.saas.dsa.model.AccommodationType;

@Data
public class AccommodationVO {

	private long id;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private AccommodationType accommodationType;
	private Integer weeks;

	private BigDecimal standardCost;
	private String standardCostStr;
	private BigDecimal enhancedCost;
	private String enhancedCostStr;


}
