package uk.gov.saas.dsa.domain;

import static uk.gov.saas.dsa.web.helper.DSAConstants.APP_ID_SEQ;

import java.io.Serializable;

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

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.PaymentForConverter;
import uk.gov.saas.dsa.model.PaymentFor;

/**
 * Bank account.
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_BANK_ACCOUNT", schema = "SGAS")
public class DSAApplicationBankAccount implements Serializable {

	private static final String DSA_APPLICATION_BANK_ACCOUNT_ID_SEQ = "dsaApplicationBankACIdSeq";

	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = DSA_APPLICATION_BANK_ACCOUNT_ID_SEQ, sequenceName = APP_ID_SEQ, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_BANK_ACCOUNT_ID_SEQ)
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "ACCOUNT_NAME")
	private String accountName;

	@Column(name = "SORT_CODE")
	private String sortCode;

	@Column(name = "ACCOUNT_NUMBER")
	private String accountNumber;

	@Column(name = "PAYMENT_FOR")
	@Enumerated(EnumType.STRING)
	@Convert(converter = PaymentForConverter.class)
	private PaymentFor paymentFor;
}
