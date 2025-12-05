package uk.gov.saas.dsa.model;

import lombok.Getter;

/**
 * TravelExp Type
 */
@Getter
public enum TravelExpType {

	OWN_VEHICLE(1, "Own vehicle", "ownVehicle"), TAXI(2, "Taxi", "taxi"), LIFT(3, "Lift", "lift");
	

	int order;
	private String description;
	private String pathName;

	TravelExpType(int order, String description, String pathName) {
		this.order = order;
		this.description = description;
		this.pathName = pathName;
	}

}