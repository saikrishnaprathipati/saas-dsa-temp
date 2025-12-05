package uk.gov.saas.dsa.model;

import lombok.Data;

@Data
public class ApplicationSectionResponse {
	private SectionStatusResponse aboutStudentSectionData;
	private SectionStatusResponse aboutCourseSectionData;
	private SectionStatusResponse disabilitySectionData;
	private SectionStatusResponse allowanceSectionData;
	private SectionStatusResponse needsAssessmentFeeSectionData;
	private SectionStatusResponse additionalInfoData;
	private SectionStatusResponse advisorDeclarationSectionData;
	private SectionStatusResponse studentDeclarationSectionData;

}
