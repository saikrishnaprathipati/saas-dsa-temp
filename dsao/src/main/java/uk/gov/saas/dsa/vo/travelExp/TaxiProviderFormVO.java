package uk.gov.saas.dsa.vo.travelExp;

import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = { "recommendedProvider", "approvedContractor", "cost" })
public class TaxiProviderFormVO {

	long id;

	@Size(max = 50, message = "{travelexp.recommendedProvider.maxLength}")
	private String recommendedProvider;

	private String approvedContractor;

	private String cost;

	private String costStr;
	
	boolean validated;

	private String remove;
}
