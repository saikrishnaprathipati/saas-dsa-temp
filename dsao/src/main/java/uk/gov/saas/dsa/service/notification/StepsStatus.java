package uk.gov.saas.dsa.service.notification;

import lombok.Getter;

@Getter
public enum StepsStatus {
	
//  1	Offline Application
//	2	Received
//	3	Awarded
//	4	Pended with HEI/Student
//	5	Pended with SAAS
//	6	Rejected
//	7	Application Withdrawn
	
	APPLICATION_WITHDRAWN("APPLICATION WITHDRAWN", "Withdrawn"),
	PENDED_WITH_HEI_STUDENT("PENDED WITH HEI/STUDENT", "Pending"), AWARDED("AWARDED", "AWARDED"),
	REJECTED("REJECTED", "Not Awarded"), STEPS_RECEIVED("RECEIVED", "Received"),
	PENDED_WITH_SAAS("PENDED WITH SAAS", "In Progress");

	private String stepsStatus;
	private String description;

	private StepsStatus(String code, String description) {
		this.stepsStatus = code;
		this.description = description;
	}

}