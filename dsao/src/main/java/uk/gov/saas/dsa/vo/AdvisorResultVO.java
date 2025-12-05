package uk.gov.saas.dsa.vo;

import lombok.Data;

@Data
public class AdvisorResultVO {

	private Long advisorId;
	private String firstName;
	private String lastName;
	private String email;
	private String institution;
	private String teamEmail;
	private String institutionCode;
	private String roleName;
	private String roleDescription;
	private String userId;
	private String response;
	private Boolean canAdvisorApply;
}


