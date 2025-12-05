package uk.gov.saas.dsa.vo.nmph;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = { "typeOfSupport", "recommendedProvider", "weeks", "hours", "hourlyRate", "cost" })
public class NMPHAllowanceVO {
	private long id;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private String typeOfSupport;
	private String recommendedProvider;
	private BigDecimal hourlyRate;
	private String hourlyRateStr;
	private Integer weeks;
	private Integer hours;
	private BigDecimal cost;
	private String costStr;

}
