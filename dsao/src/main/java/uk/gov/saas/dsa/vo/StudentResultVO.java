package uk.gov.saas.dsa.vo;

import lombok.Data;
import uk.gov.saas.dsa.model.FundingEligibilityStatus;
import uk.gov.saas.dsa.web.helper.FindStudentHelper;

@Data
public class StudentResultVO {
	private String suid;
	private String firstName;
	private String lastName;
	private long studentReferenceNumber;
	private String dob;
	private String emailAddress;
	private String applicationUpdated;
	private String applicationStatus;
	private FundingEligibilityStatus fundingEligibilityStatus;
	private StudentCourseYearVO studentCourseYear;
	private String accountNumber;
	private String sortCode;

	public static String convertNumberToWord(int number) {
		return FindStudentHelper.convertNumberToWord(number);
	}
}
