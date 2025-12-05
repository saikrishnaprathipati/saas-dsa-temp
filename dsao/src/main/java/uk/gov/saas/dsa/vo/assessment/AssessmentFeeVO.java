package uk.gov.saas.dsa.vo.assessment;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = { "assessmentFeeCentreName", "assessorName", "totalHours", "hourlyRate" , "cost"})
public class AssessmentFeeVO {
	private long id;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private String assessmentFeeCentreName;
	private String assessorName;
	private BigDecimal hourlyRate;
	private Integer totalHours;
	private String hourlyRateStr;
	private BigDecimal cost;
	private String costStr;

}
