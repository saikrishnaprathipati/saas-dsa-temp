package uk.gov.saas.dsa.vo.quote;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteResultVO {
	private long quoteId;
	private String advisorId;
	private String firstName;
	private String lastName;
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private Integer sessionCode;
	private String academicYear;
	private String supplier;
	private String quoteReference;
	private BigDecimal cost;
	private String costStr;
	private byte[] quote;
	private String fileName;
	private String size;

	@Override
	public String toString() {
		return "QuoteResultVO{" +
				"quoteId=" + quoteId +
				", advisorId='" + advisorId + '\'' +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", studentReferenceNumber=" + studentReferenceNumber +
				", dsaApplicationNumber=" + dsaApplicationNumber +
				", sessionCode=" + sessionCode +
				", academicYear='" + academicYear + '\'' +
				", supplier='" + supplier + '\'' +
				", quoteReference='" + quoteReference + '\'' +
				", cost=" + cost +
				", costStr='" + costStr + '\'' +
				", fileName='" + fileName + '\'' +
				", size='" + size + '\'' +
				'}';
	}
}
