package uk.gov.saas.dsa.model;

import java.util.List;

import lombok.Data;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;

/**
 * Application response
 */
@Data
public class ApplicationResponse {

	private String firstName;

	private String lastName;

	private String academicYear;

	private String dateOfBirth;

	private Integer sessionCode;

	private FundingEligibilityStatus fundingEligibilityStatus;

	private boolean isNewApplication;

	private long dsaApplicationNumber;

	private long studentReferenceNumber;

	private ApplicationSummaryStatus applicationStatus;

	private OverallApplicationStatus overallApplicationStatus;

	private ApplicationSectionResponse sectionStatusData;
	/**
	 * Created for temporary purpose, this might not be required.
	 */
	private List<SectionStatusResponse> sectionPartStatusList;

	private List<DisabilityTypeVO> applicationDisabilities;

	private String part1CompletionStatusText;

	private String part2CompletionStatusText;

	private String part3CompletionStatusText;


	private boolean allAllowancesCompleted;

	private boolean advisorDeclarationCompleted;

	private boolean studentDeclarationCompleted;

	private boolean needsAssessmentCompleted;

	private String institutionName;

	private String applicationUpdated;

	private BankAccountVO bankDetails;

	private List<ConsumableTypeVO> consumables;

	private List<NMPHAllowanceVO> nmphAllowances;

	private List<EquipmentAllowanceVO> equipments;

	private List<TravelExpAllowanceVO> travelExpeses;
	
	private List<AccommodationVO> accommodations;
	
	
	private List<AssessmentFeeVO> assessmentFeeList;
	
	private boolean applicationFullySubmitted;
	private String additionalInfoText;
}
