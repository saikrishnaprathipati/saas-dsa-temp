package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class FindStudentOptionsFormVO {
	@NotBlank(message = "{findStudent.serch.option.required}")
	private String findOption;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("findOption");
		return (LinkedHashSet<String>) orderedFields;
	}
}
