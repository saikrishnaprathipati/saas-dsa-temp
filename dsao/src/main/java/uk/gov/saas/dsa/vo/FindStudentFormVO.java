package uk.gov.saas.dsa.vo;

import java.sql.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class FindStudentFormVO {

	public static final String NAME_REGEX = "^[a-zA-Z \\-\\']*$";

	@NotBlank(message = "{firstName.required}")
	@Size(max = 25, message = "{firstName.maxLength}")
	@Pattern(regexp = NAME_REGEX, message = "{firstName.invalid}")
	private String firstName;

	@NotBlank(message = "{lastName.required}")
	@Size(max = 25, message = "{lastName.maxLength}")
	@Pattern(regexp = NAME_REGEX, message = "{lastName.invalid}")
	private String lastName;
  
	private String dobDay;
  
	private String dobMonth;
  	
	private String dobYear;
 
	private Date dateOfBirth;

	private Set<String> orderedFields = new LinkedHashSet<String>();

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("firstName");
		orderedFields.add("lastName");
		orderedFields.add("dateOfBirth");
		orderedFields.add("dobDay");
		orderedFields.add("dobMonth");
		orderedFields.add("dobYear");
		return (LinkedHashSet<String>) orderedFields;
	}
}
