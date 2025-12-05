package uk.gov.saas.dsa.vo.travelExp;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class TravelExpSelectionFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;
	@NotEmpty(message = "{travelexp.checkBox.required}")
	private List<String> travelExpTypes;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("travelExpTypes");
		return (LinkedHashSet<String>) orderedFields;
	}
}
