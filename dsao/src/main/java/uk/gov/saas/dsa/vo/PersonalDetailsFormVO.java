package uk.gov.saas.dsa.vo;

import static org.springframework.util.StringUtils.hasText;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.Getter;
import uk.gov.saas.dsa.domain.validation.MaxString;
import uk.gov.saas.dsa.domain.validation.MinString;

/**
 * To hold the UI fields and validations
 * 
 * @author Siva Chimpiri
 *
 */
@Data
public class PersonalDetailsFormVO implements Serializable {
	public static final String HOUSE_NUMBER_REGEX = "^[A-Za-z0-9 \\-\\.\\'\\/]*$";

	public static final String ADDRESS_REGEX = "^[a-zA-Z0-9 \\-\\.\\'\\/]*$";

	@Getter(lombok.AccessLevel.NONE)
	@lombok.Setter(lombok.AccessLevel.NONE)
	private static final long serialVersionUID = 1L;

	@Getter(lombok.AccessLevel.NONE)
	@lombok.Setter(lombok.AccessLevel.NONE)
	private static final Logger logger = LogManager.getLogger(PersonalDetailsFormVO.class);

	private Long applicationId;

	@NotBlank(message = "{title.required}")
	private String titleType;

	@NotBlank(message = "{gender.required}")
	private String gender;

	@NotBlank(message = "{lastname.required}")
	@Size(max = 25, message = "{lastname.maxLength}")
	@Pattern(regexp = "^[a-zA-Z \\-\\']*$", message = "{lastname.invalid}")
	private String surname;

	private String niNumber;

	@NotBlank(message = "{forename.required}")
	@Size(max = 25, message = "{forename.maxLength}")
	@Pattern(regexp = "^[a-zA-Z \\-\\']*$", message = "{forename.invalid}")
	private String forename;

	@NotBlank(message = "{dateOfBirthDay.invalid}")
	@MaxString(value = 31, message = "{dateOfBirthDay.invalid}")
	private String dobDay;

	@NotBlank(message = "{dateOfBirthMonth.invalid}")
	@MaxString(value = 12, message = "{dateOfBirthMonth.invalid}")
	private String dobMonth;

	@NotBlank(message = "{dateOfBirthYear.invalid}")
	@MinString(value = 1900, message = "{dateOfBirthYear.invalid}")
	private String dobYear;

	@Past(message = "{dateOfBirth.past}")
	private Date dateOfBirth;

	@Length(max = 32, message = "{houseNumber.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{houseNumber.invalid}")
	@NotBlank(message = "{houseNumber.required}")
	private String houseNumber;

	private String postCode;

	@Length(max = 65, message = "{address1.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{address1.invalid}")
	@NotBlank(message = "{address1.required}")
	private String address1;

	@Length(max = 65, message = "{address2.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{address2.invalid}")
	@NotBlank(message = "{address2.required}")
	private String address2;

	@Length(max = 32, message = "{address3.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{address3.invalid}")
	private String address3;

	@Length(max = 32, message = "{address4.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{address4.invalid}")
	private String address4;

	@Length(max = 32, message = "{manualHouseNumber.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{manualHouseNumber.invalid}")
	@NotBlank(message = "{manualHouseNumber.required}")
	private String manualHouseNumber;

	@Length(max = 65, message = "{manualAddress1.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{manualAddress1.invalid}")
	@NotBlank(message = "{manualAddress1.required}")
	private String manualAddress1;

	@Length(max = 65, message = "{manualAddress2.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{manualAddress2.invalid}")
	private String manualAddress2;

	@Length(max = 32, message = "{manualAddress3.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{manualAddress3.invalid}")
	@NotBlank(message = "{manualAddress3.required}")
	private String manualAddress3;

	@Length(max = 32, message = "{manualAddress4.maxLength}")
	@Pattern(regexp = ADDRESS_REGEX, message = "{manualAddress4.invalid}")
	private String manualAddress4;

	private String mobileNumber;

	private String homePhoneNumber;

	private List<String> hideProperties = new ArrayList<>();
	private List<String> completedProperties = new ArrayList<>();

	private String selectedAddress;
 

	public boolean hasManualAddress() {
		return (hasText(this.manualHouseNumber) || hasText(this.manualAddress1) || hasText(this.manualAddress2)
				|| hasText(this.manualAddress3) || hasText(this.manualAddress4)) && !hasText(this.selectedAddress);
	}
}
