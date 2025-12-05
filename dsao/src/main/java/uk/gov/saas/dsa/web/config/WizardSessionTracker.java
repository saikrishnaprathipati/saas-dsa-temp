package uk.gov.saas.dsa.web.config;

import lombok.Data;
import uk.gov.saas.dsa.vo.ApplicationDetailsFormVO;
import uk.gov.saas.dsa.vo.CourseDetailsFormVO;
import uk.gov.saas.dsa.vo.PersonalDetailsFormVO;
import uk.gov.saas.dsa.vo.ResidencyDetailsFormVO;

@Data
public class WizardSessionTracker {
	
	private ApplicationDetailsFormVO applicationDetailsFormVO;
	
	private PersonalDetailsFormVO detailsWizardFormVO;
	private PersonalDetailsFormVO formDataInDB;
	private boolean dobReadOnly;
	
	private ResidencyDetailsFormVO residencyWizardFormVO;
	private ResidencyDetailsFormVO residencyFormDataInDB;
	
	private CourseDetailsFormVO courseWizardFormVO;
	private CourseDetailsFormVO courseFormDataInDB;
	
}
