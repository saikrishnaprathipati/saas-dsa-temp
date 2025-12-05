package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class FindStudentByStudrefFormVO {

	public static final String NAME_REGEX = "^[a-zA-Z \\-\\']*$";

	@NotBlank(message = "{findStudent.studentReferenceNumber.required}")
	
	private String studentReferenceNumber;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("studentReferenceNumber");
		return (LinkedHashSet<String>) orderedFields;
	}
}
