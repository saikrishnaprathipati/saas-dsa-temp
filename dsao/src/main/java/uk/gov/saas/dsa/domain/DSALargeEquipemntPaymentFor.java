package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.LargeEquipmentPaymentTypeConverter;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;

@Entity
@Data
@Table(name = "DSA_LRG_EQP_PAYMENT_FOR")
public class DSALargeEquipemntPaymentFor implements Serializable {

	private static final long serialVersionUID = -6402934961470808627L;

	@Id
	@Column(name = "DSA_APPLICATION_NO")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO")
	private long studentRefNumber;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "PAYMENT_FOR")
	@Enumerated(EnumType.STRING)
	@Convert(converter = LargeEquipmentPaymentTypeConverter.class)
	private LargeEquipmentPaymentType paymentFor;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;
}
