package uk.gov.saas.dsa.model;

import lombok.Data;

/**
 * To Get the use application saved status
 * 
 * @author Siva Chimpiri
 *
 */
@Data
public class CurrentPageStatus {
	/**
	 * To hold user about you section
	 */
	private DashboardSection aboutYou;

	/**
	 * To hold user residence section
	 */
	private DashboardSection residencyDetails;

	/**
	 * To hold user course section
	 */
	private DashboardSection yourCourse;

	/**
	 * To hold user disability section
	 */
	private DashboardSection yourDisability;

}
