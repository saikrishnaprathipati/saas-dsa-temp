package uk.gov.saas.dsa.vo.quote;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class QuoteDetailsFormVO {

	private long quoteId;

	@Size(max = 100, message = "{quote.supplier.maxLength}")
	private String supplier;

	@Size(max = 100, message = "{quote.reference.maxLength}")
	private String quoteReference;

	private String totalCost;
	private String firstName;
	private String lastName;
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private String academicYear;
	private Integer sessionCode;
	private String advisorId;
	private MultipartFile[] files;
	private String existingAction;

	private Set<String> orderedFields = new LinkedHashSet<>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("supplier");
		orderedFields.add("quoteReference");
		orderedFields.add("totalCost");
		orderedFields.add("files");
		return (LinkedHashSet<String>) orderedFields;
	}
}
