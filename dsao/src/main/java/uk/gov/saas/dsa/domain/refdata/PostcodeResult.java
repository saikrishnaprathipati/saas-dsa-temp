package uk.gov.saas.dsa.domain.refdata;

import java.util.List;

public class PostcodeResult {
	
	private String postCode;
	//area data
	private String postTown;
	private String county;
	//road data
	private String subStreet;
	private String street;
	private String subLocality;
	private String locality;

	private String organisationName;
	//premise data
	private List<String> premises;

	private String codeAddress;

	public List<String> getPremises() {
		return premises;
	}

	public void setPremises(List<String> premises) {
		this.premises = premises;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getPostTown() {
		return postTown;
	}

	public void setPostTown(String postTown) {
		this.postTown = postTown;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getSubStreet() {
		return subStreet;
	}

	public void setSubStreet(String subStreet) {
		this.subStreet = subStreet;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getSubLocality() {
		return subLocality;
	}

	public void setSubLocality(String subLocality) {
		this.subLocality = subLocality;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getCodeAddress() {
		return codeAddress;
	}

	public void setCodeAddress(String codeAddress) {
		this.codeAddress = codeAddress;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PostCodeResult{");
		sb.append("postCode='").append(postCode).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

