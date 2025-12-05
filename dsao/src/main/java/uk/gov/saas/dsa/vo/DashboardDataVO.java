package uk.gov.saas.dsa.vo;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * Dashboard data VO
 */
@Data
public class DashboardDataVO {
	@NotBlank
	private String studentReferenceNumber;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	@NotBlank
	private String academicYear;

	private long dsaApplicationNumber;
}
