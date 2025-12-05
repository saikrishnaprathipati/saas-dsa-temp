package uk.gov.saas.dsa.domain.helpers;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;



public class LegacyPasswordEncoder implements PasswordEncoder
{
	private static final  Logger logger = LogManager.getLogger(LegacyPasswordEncoder.class);

	/**
	 * Default constructor
	 */
	public LegacyPasswordEncoder(){
		// Do nothing here
	}

	static final String HEXES = "0123456789ABCDEF";

	/**
	 * The key that is going to be used by AES algorithm
	 */
	private static byte[] keyBytes =
	{
			38, 74, -22, -83, -128, -105, 34, -31, -5, 35, -36, 120, 74, 91, -1, -44
	};

	private static String encryption = "AES";

	public static String encrypt(String password)
	{
		try
		{
			if (password != null)
			{
				SecretKeySpec key = new SecretKeySpec(keyBytes, encryption);
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				byte[] encryptedBytes = cipher.doFinal(password.getBytes());
				return byteToHex(encryptedBytes);
			}
		} catch (Exception e)
		{
			logger.error("A problem occured while encrypting password", e);
		}
		return "";
	}

	public static String decrypt(String password)
	{
		try
		{
			if (password != null)
			{
				byte[] passwordBytes = hexToByte(password);
				SecretKeySpec key = new SecretKeySpec(keyBytes, encryption);
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] decryptedBytes = cipher.doFinal(passwordBytes);
				return new String(decryptedBytes);
			}
		} catch (Exception e)
		{
			logger.error(String.format("A problem occured while decrypting password '%s'", password), e);
		}

		return "";
	}

	public static boolean checkPassword(String plainPassword, String encryptedPassword)
	{
		String plainEncrptedPassword = encrypt(plainPassword);

		if (plainEncrptedPassword != null) {
			if (plainEncrptedPassword.equals(encryptedPassword)) {
				logger.info("Encryption Helper matches = true");
				return true;
			}
			logger.info("Encryption Helper matches = false");
		}
		return false;
	}

	public static String byteToHex(byte[] raw)
	{
		if (raw == null)
		{
			return "";
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw)
		{
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static byte[] hexToByte(String hexString)
	{

		if (hexString == null)
			return new byte[0];

		int len = hexString.length();
		byte[] ba = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			ba[i / 2] =
				(byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
		}
		return ba;
	}

	@Override
	public String encode(CharSequence charSequence) {
		return encrypt(charSequence.toString());
	}

	@Override
	public boolean matches(CharSequence charSequence, String s) {
		logger.info("Encryption Helper matches");
		return checkPassword(charSequence.toString(),s);
	}


}
