package uk.gov.saas.dsa.vo;

import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

public class LoginFormVO {

	private String emailAddress;
	private String password;
	@Setter
	private Boolean resendLoginLink;
	private Boolean isEmailAlreadyActive;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getIsEmailAlreadyActive() {
		return isEmailAlreadyActive;
	}

	public void setIsEmailAlreadyActive(Boolean isEmailAlreadyActive) {
		this.isEmailAlreadyActive = isEmailAlreadyActive;
	}

	public Boolean getResendLoginLink() {
		return resendLoginLink;
	}

	private Set<String> orderedFields = new LinkedHashSet<>();
	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("emailAddress");
		orderedFields.add("password");

		return (LinkedHashSet<String>) orderedFields;
	}
}
