package uk.gov.saas.dsa.vo;

import lombok.Data;
import uk.gov.saas.dsa.model.FundingEligibilityStatus;
import uk.gov.saas.dsa.model.OverallApplicationStatus;

/**
 * Dashboard form VO
 */
@Data
public class DashboardFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private String firstName;
	private String lastName;
	private String academicYear;
	private Integer sessionCode;
	private String roleName;
	private String dob;
	private String institutionName;
	private String emailAddress;
	private String applicationUpdated;
	private boolean newApplication;

	private String advisorDeclaration;
	private OverallApplicationStatus applicationStatus;
	private FundingEligibilityStatus fundingEligibilityStatus;
}
