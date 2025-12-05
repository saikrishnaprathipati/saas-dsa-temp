package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class StudentDeclarationFormVO {

	private long studentReferenceNumber;

	private long dsaApplicationNumber;
	private String backAction;

	private List<String> declarationCodes;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("declarationCodes");

		return (LinkedHashSet<String>) orderedFields;
	}
}
