package uk.gov.saas.dsa.model;

import lombok.Getter;

@Getter
public enum RoleDescription {

	DISABILITYADVISOR("DISABILITY ADVISOR",
			"As a disability advisor you will be able find a student, "
					+ "view their DSA history and complete the disabilities and "
					+ "allowances section of the DSA application on behalf of a student."),
	DISABILITYASSESSOR("DISABILITY ASSESSOR",
			"As a disability assessor you will be able find a student, "
					+ "view their DSA history and complete the disabilities and "
					+ "allowances section of the DSA application on behalf of a student.");

	private String roleName;
	private String roleDescription;

	RoleDescription(String roleName, String roleDescription) {
		this.roleName = roleName;
		this.roleDescription = roleDescription;
	}
}