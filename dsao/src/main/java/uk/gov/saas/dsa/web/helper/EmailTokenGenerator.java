package uk.gov.saas.dsa.web.helper;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;
import java.util.UUID;

public class EmailTokenGenerator {

	private static final int SUBTRACT_FIFTY_CHARACTERS = 50;
	private static final int FETCH_THE_THIRTY_TWO_CHARACTERS_BIT = 32;
	public static String generateRegistrationCode() {
		String uuid = UUID.randomUUID().toString();
		String sha3Hex = DigestUtils.sha256Hex(uuid);

		int randomNum = new Random().nextInt(sha3Hex.length() - SUBTRACT_FIFTY_CHARACTERS);
		return sha3Hex.substring(randomNum, randomNum + FETCH_THE_THIRTY_TWO_CHARACTERS_BIT);
	}
}
