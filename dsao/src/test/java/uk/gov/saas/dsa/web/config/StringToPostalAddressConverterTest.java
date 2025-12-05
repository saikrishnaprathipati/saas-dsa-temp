package uk.gov.saas.dsa.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.saas.dsa.domain.refdata.PostalAddress;

class StringToPostalAddressConverterTest {
	private StringToPostalAddressConverter subject;

	@BeforeEach
	void setUp() {
		subject = new StringToPostalAddressConverter();
	}

	@Test
	void shouldGetTheCorrectSelectedAddressFromTheString() {
		PostalAddress address = new PostalAddress("hno", "eh54 6th", "address line 1", "address line 3");
		PostalAddress convert = subject.convert(
				"houseNumber=hno,postCode=eh54 6th,address1=address line 1,address2=address line 3,address3=address line 3,address4=address4");
		assertEquals(address, convert);
	}

}
