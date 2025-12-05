package uk.gov.saas.dsa.vo;

import lombok.Data;
import uk.gov.saas.dsa.model.ApplicationSectionPart;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;

@Data
public class ApplicationSectiponStatusVO {
	private long dsaApplicationNumber;

	private long studentReferenceNumber;

	private ApplicationSectionPart sectionPart;
	private Section sectionCode;
	private SectionStatus sectionStatus;
	private String lastUpdatedBy;
}
