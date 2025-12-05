package uk.gov.saas.dsa.model;

import lombok.Data;
import uk.gov.saas.dsa.vo.ResidencyDetailsFormVO;

@Data
public class ResidencyDetailsResponse {
	private ResidencyDetailsFormVO residencyDetailsFormVO;
	private boolean dobReadOnly;
}
