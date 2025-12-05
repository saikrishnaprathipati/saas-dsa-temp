package uk.gov.saas.dsa.domain.refdata;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Component(value = "session")
@NoArgsConstructor
@EqualsAndHashCode
public class PostalAddress {

	private String houseNumber;
	private String postCode;
	private String address1;
	private String address2;
	private String address3;
	private String address4;
	private String displayAddressLine;

	public PostalAddress(String houseNumber, String postCode, String address1, String address3) {
		super();
		this.houseNumber = houseNumber;
		this.postCode = postCode;
		this.address1 = address1;
		this.address3 = address3;
	}

	@Override
	public String toString() {
		return ("houseNumber=" + this.houseNumber + "," + "postCode=" + this.postCode + "," + "address1=" + this.address1
				
				+ "," + "address3=" + this.address3).toUpperCase();
				
	}
}
