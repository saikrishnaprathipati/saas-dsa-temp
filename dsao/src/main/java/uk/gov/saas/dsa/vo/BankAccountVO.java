package uk.gov.saas.dsa.vo;

import lombok.Data;
import uk.gov.saas.dsa.model.PaymentFor;

@Data
public class BankAccountVO {
	private Long id;
	private long dsaApplicationNumber;
	private long studentReferenceNumber;
	private String accountName;
	private String sortCode;
	private PaymentFor paymentFor;
	private String accountNumber; 
	private String sortCodeForUI;
	private String accountNumberForUI;

}
