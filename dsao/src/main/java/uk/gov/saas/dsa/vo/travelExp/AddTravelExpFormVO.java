package uk.gov.saas.dsa.vo.travelExp;

import lombok.Data;
import uk.gov.saas.dsa.model.TravelExpType;

import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
public class AddTravelExpFormVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private long id;

	// using to while persisting entry in to the DB.
	private TravelExpType travelExpType;

	private String startLocationPostcode;

	private String endLocationPostcode;

	private String returnJourneys;

	private Integer weeks;

	@Valid
	private TaxiProviderFormVO taxiProvider;

	private List<TaxiProviderFormVO> taxiProviderList;

	private String vehicleType;

	private String fuelCost;
	private String milesPerGallon;

	private String kwhCost;
	private String kwhCapacity;
	private String rangeOfCar;

	private Set<String> orderedFields = new LinkedHashSet<>();

	// A temp variable to validate the providers panel.
	private String panelValidation;

	public LinkedHashSet<String> getOrderedFields() {
		orderedFields.add("startLocationPostcode");
		orderedFields.add("endLocationPostcode");
		orderedFields.add("returnJourneys");
		orderedFields.add("weeks");
		orderedFields.add("vehicleType");
		orderedFields.add("fuelCost");
		orderedFields.add("milesPerGallon");
		orderedFields.add("kwhCost");
		orderedFields.add("kwhCapacity");
		orderedFields.add("rangeOfCar");
		orderedFields.add("panelValidation");
		orderedFields.add("taxiProvider.recommendedProvider");
		orderedFields.add("taxiProvider.approvedContractor");
		orderedFields.add("taxiProvider.cost");

		return (LinkedHashSet<String>) orderedFields;
	}

}
