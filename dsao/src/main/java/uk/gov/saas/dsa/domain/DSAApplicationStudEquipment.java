package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

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
import jakarta.validation.constraints.Digits;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.LargeEquipmentPaymentTypeConverter;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;

/**
 * DSA Application Stud Equipment Allowances
 */
@Data
@Entity
@Table(name = "DSA_APP_STUD_LARGE_ITEMS", schema = "SGAS")
public class DSAApplicationStudEquipment implements Serializable {
	private static final String APP_ID_SEQ = "APP_ID_SEQ";
	private static final String DSA_APPLICATION_EQUIPMENT_ID_SEQ = "dsaApplicationEquipmentIdSeq";

	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = DSA_APPLICATION_EQUIPMENT_ID_SEQ, sequenceName = APP_ID_SEQ, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DSA_APPLICATION_EQUIPMENT_ID_SEQ)
	private long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "PRODUCT_NAME")
	private String productName;

	@Column(name = "DESCRIPTION")
	private String description;
 

	@Column(name = "COST", nullable = false, precision = 7, scale = 2)
	@Digits(integer = 7, fraction = 2)
	private BigDecimal cost;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;
}
