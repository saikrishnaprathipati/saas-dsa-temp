package uk.gov.saas.dsa.model;

public enum STEPSAwardStatusCode {

//	N	New
//	C	Assessed
//	T	Pending
//	W	Withdrawn
//	A	Non-Attendance
//	R	Rejected
	N("New"), C("Assessed"), T("Pending"), W("Withdrawn"), A("Non-Attendance"), R("Rejected");

	String description;

	STEPSAwardStatusCode(String description) {

		this.description = description;
	}
}
