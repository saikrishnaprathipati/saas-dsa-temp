package uk.gov.saas.dsa.vo;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AddBankAccountFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;

	@NotBlank(message = "{bankAccount.nameOnAccount.required}")
	@Size(max = 25, message = "{bankAccount.nameOnAccount.maxLength}")
	@Pattern(regexp = FindStudentFormVO.NAME_REGEX, message = "{bankAccount.nameOnAccount.invalid}")
	private String nameOnAccount;
	
	@NotBlank(message = "{bankAccount.sortCode.required}")
	private String sortCode;
	
	@NotBlank(message = "{bankAccount.accountNumber.required}")
	private String accountNumber;

	private String backAction;
	
	private String duplicateBankDetails;
	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("nameOnAccount");
		orderedFields.add("sortCode");
		orderedFields.add("accountNumber");
		orderedFields.add("duplicateBankDetails");
		
		return (LinkedHashSet<String>) orderedFields;
	}
}
