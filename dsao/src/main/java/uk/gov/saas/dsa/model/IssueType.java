package uk.gov.saas.dsa.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
public enum IssueType {

	INCORRECT_INFO("INCORRECT_INFO", "Incorrect information",
			"Something on my application is incorrect and I want my Disability Advisor to make a change.", "make any relevant changes"),
	DECLARATION_ISSUE("DECLARATION_ISSUE", "Student declaration",
			"I cannot complete the student declaration because I do not understand the information on the page, or I do not feel comfortable agreeing to the statements.", "help them understand the student declaration"),
	REQUEST_WITHDRAW("REQUEST_WITHDRAW", "Request to withdraw",
			"I want to withdraw my application for DSA because I no longer need it or I am deferring or withdrawing from my course.", "confirm that they wish to withdraw their application"),
	OTHER("OTHER", "Something else", "The reason I cannot complete my application is not listed above.", "confirm what the issue is");

	private final String code;
	private final String name;
	private final String description;
	private final String inlineMailText;

	IssueType(String code, String name, String description, String inlineMailText) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.inlineMailText = inlineMailText;
	}

	public static Optional<IssueType> getIssueTypeByValue(String value) {
		return Arrays.stream(IssueType.values())
				.filter(issueType -> Objects.equals(issueType.code, value))
				.findFirst();
	}
}
