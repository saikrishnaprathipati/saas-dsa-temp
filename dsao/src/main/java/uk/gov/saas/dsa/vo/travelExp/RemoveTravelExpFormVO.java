package uk.gov.saas.dsa.vo.travelExp;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import uk.gov.saas.dsa.model.TravelExpType;

@Data
public class RemoveTravelExpFormVO {
	private long studentReferenceNumber; 
	private long dsaApplicationNumber;
	private long id;
	private TravelExpType travelExpType;
	private String removeItem;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
