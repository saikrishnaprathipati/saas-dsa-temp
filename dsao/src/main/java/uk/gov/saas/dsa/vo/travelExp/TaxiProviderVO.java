package uk.gov.saas.dsa.vo.travelExp;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = { "recommendedProvider", "approvedContractor", "cost" })
public class TaxiProviderVO {
	private long id;
	
	private String recommendedProvider;

	private String approvedContractor;

	private String costStr;
	
	private BigDecimal cost;
}
