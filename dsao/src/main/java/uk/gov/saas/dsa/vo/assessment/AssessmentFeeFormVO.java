package uk.gov.saas.dsa.vo.assessment;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AssessmentFeeFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;

	@NotBlank(message = "{assessmentFee.assessmentFeeCentreName.required}")
	@Size(max = 50, message = "{assessmentFee.assessmentFeeCentreName.maxLength}")
	private String assessmentFeeCentreName;

	@NotBlank(message = "{assessmentFee.assessorName.required}")
	@Size(max = 50, message = "{assessmentFee.assessorName.maxLength}")
	private String assessorName;

	@NotBlank(message = "{assessmentFee.totalHours.required}")
	private String totalHours;

	@NotBlank(message = "{assessmentFee.cost.required}")
	private String cost;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("assessmentFeeCentreName");
		orderedFields.add("assessorName");
		orderedFields.add("totalHours");
		orderedFields.add("cost");
		return (LinkedHashSet<String>) orderedFields;
	}
}
