package uk.gov.saas.dsa.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import uk.gov.saas.dsa.domain.refdata.PostalAddress;
/**
 * this is used to convert the selected address from the UI to Object
 * @author Siva Chimpiri
 *
 */
@Component
public class StringToPostalAddressConverter implements Converter<String, PostalAddress> {
	private final Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public PostalAddress convert(String postalAddressString) {
		logger.info("postal address string received {}", postalAddressString);
		String[] data = postalAddressString.split(",");
		String houseNumber = splitString(data[0]);
		String postCode = splitString(data[1]);
		String address1 = splitString(data[2]);
		String address3 = splitString(data[3]);

		return new PostalAddress(houseNumber, postCode, address1, address3);
	}

	private String splitString(String str) {
		String[] data = str.split("=");
		return data[1];
	}
}
