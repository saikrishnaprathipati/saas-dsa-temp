package uk.gov.saas.dsa.vo.travelExp;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class RemoveTaxiProviderFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	
	private String removeItem;

	private TaxiProviderFormVO taxiProviderToRemove;
	private List<TaxiProviderFormVO> taxiProviderList;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("removeItem");
		return (LinkedHashSet<String>) orderedFields;
	}
}
