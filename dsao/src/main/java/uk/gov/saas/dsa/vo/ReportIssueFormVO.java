package uk.gov.saas.dsa.vo;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class ReportIssueFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;

	@NotEmpty(message = "{generic.message.option}")
	private String issueType;

	@Size(max = 300, message = "{issue.description.maxLength}")
	private String notListedText;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("issueType");
		orderedFields.add("notListedText");
		return (LinkedHashSet<String>) orderedFields;
	}
}
