package uk.gov.saas.dsa.domain;

import java.io.Serializable;
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

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.ApplicationSectionConverter;
import uk.gov.saas.dsa.domain.converters.SectionStatusConverter;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.ApplicationSectionPart;
import uk.gov.saas.dsa.model.SectionStatus;

/**
 * DSAApplicationSectionStatus table
 */
@Data
@Entity
@Table(name = "DSA_APPLICATION_SECTION_STATUS", schema = "SGAS")
public class DSAApplicationSectionStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106836806612404042L;

	@Id
	@Column(name = "ID")

	@SequenceGenerator(name = "dsaApplicationSectionStatusIdSeq", sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dsaApplicationSectionStatusIdSeq")
	private Long id;

	@Column(name = "DSA_APPLICATION_NO", columnDefinition = "dsaApplicationNumber")
	private long dsaApplicationNumber;

	@Column(name = "STUD_REF_NO", columnDefinition = "studentReferenceNumber")
	private long studentReferenceNumber;

	@Column(name = "SECTION_PART")
	@Enumerated(EnumType.STRING)
	private ApplicationSectionPart sectionPart;

	@Column(name = "SECTION_CODE")
	@Enumerated(EnumType.STRING)
	@Convert(converter = ApplicationSectionConverter.class)
	private Section sectionCode;

	@Column(name = "SECTION_STATUS")
	@Enumerated(EnumType.STRING)
	@Convert(converter = SectionStatusConverter.class)
	private SectionStatus sectionStatus;

	@Column(name = "CREATED_By")
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private Timestamp createdDate;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_DATE")
	private Timestamp lastUpdatedDate;

}
