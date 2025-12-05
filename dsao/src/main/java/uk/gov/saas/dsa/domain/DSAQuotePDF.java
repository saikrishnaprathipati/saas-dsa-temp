package uk.gov.saas.dsa.domain;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Date;

@Entity
@Data
@Table(name = "DSA_QUOTE_PDF")
public class DSAQuotePDF implements Serializable {

	private static final long serialVersionUID = -6402934961470808627L;

	@Id
	@Column(name = "QUOTE_ID")
	@SequenceGenerator(name = "quoteIdSeq", sequenceName = "DSA_QUOTE_PDF_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quoteIdSeq")
	private long quoteId;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentRefNumber;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_CRSE_YEAR_ID")
	private long studCrseYearId;

	@Column(name = "QUOTE")
	private Blob quote;

	@Column(name = "QUOTE_FILE_NAME")
	private String fileName;

	@Column(name = "SESSION_CODE")
	private Integer sessionCode;

	@Column(name = "ADVISOR_ID")
	private String advisorId;

	@Column(name = "SUPPLIER")
	private String supplier;

	@Column(name = "QUOTE_REFERENCE")
	private String quoteReference;

	@Column(name = "QUOTE_COST", nullable = false, precision = 7, scale = 2)
	@Digits(integer = 7, fraction = 2)
	private BigDecimal quoteCost;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

}
