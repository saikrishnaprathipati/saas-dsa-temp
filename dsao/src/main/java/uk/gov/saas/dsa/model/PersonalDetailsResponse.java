package uk.gov.saas.dsa.model;

import lombok.Data;
import uk.gov.saas.dsa.vo.PersonalDetailsFormVO;

@Data
public class PersonalDetailsResponse {
	private PersonalDetailsFormVO personalDetailsFormVO;
	private boolean dobReadOnly;

}
