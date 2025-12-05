package uk.gov.saas.dsa.vo;

import lombok.Data;

@Data
public class CreateAccountFormVO {

	private String emailAddress;
	private Boolean resendEmailLink;
	private Boolean isEmailAlreadyActive;
}
