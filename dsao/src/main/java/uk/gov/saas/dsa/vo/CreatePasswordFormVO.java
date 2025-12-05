package uk.gov.saas.dsa.vo;

import lombok.Data;

@Data
public class CreatePasswordFormVO {

	private String password;
	private String confirmPassword;
	private String email;
	private String userId;
}
