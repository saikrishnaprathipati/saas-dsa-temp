package uk.gov.saas.dsa.domain.refdata;

import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostalAddressMapper {

	private PostalAddressMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static Map<String, Object> loadAddresses(PostcodeResult pcResult) {

		List<PostalAddress> pcResults = new ArrayList<>();
		List<String> premises = pcResult.getPremises();

		if (premises.isEmpty()) {
			pcResults.add(createAddress(pcResult));
		} else {
			for (String line : premises) {
				pcResults.add(createAddress(line, pcResult));
			}
		}

		LinkedHashMap<String, Object> addressMap = new LinkedHashMap<>();
		for (PostalAddress addressObj : pcResults) {
			addressMap.put(addressObj.toString(), addressObj);
		}

		return addressMap;

	}

	private static PostalAddress createAddress(PostcodeResult pcr) {

		String[] entries = pcr.getCodeAddress().split(";");
		return createAddress(entries[0], pcr);
	}

	private static PostalAddress createAddress(String nameNumber, PostcodeResult pcr) {
		PostalAddress pa = new PostalAddress();

		if (nameNumber.contains(";")) {
			pa.setHouseNumber(formatNameNumber(nameNumber));
		} else {
			pa.setHouseNumber(nameNumber);
		}

		pa.setPostCode(pcr.getPostCode());
		pa.setAddress1(pcr.getStreet());
		pa.setAddress2(hasText(pcr.getLocality()) ? pcr.getLocality() : "");
		pa.setAddress3(pcr.getPostTown());
		pa.setAddress4(pcr.getCounty());
		String displayLine = pa.getHouseNumber() + ", " + pa.getAddress1() + ", "
				+ ("".equals(pa.getAddress2()) ? "" : pa.getAddress2() + ", ") + pa.getAddress3() + ", " + pa.getAddress4();
		pa.setDisplayAddressLine(displayLine);

		return pa;
	}

	private static String formatNameNumber(String nameNumber) {
		String[] parts = nameNumber.split(";");

		StringBuilder formattedNameNumber = new StringBuilder();
		for (String part : parts) {
			formattedNameNumber.append(part).append(" ");
		}

		return formattedNameNumber.toString().trim();
	}
}
