package uk.gov.saas.dsa.domain.helpers;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PasswordEncryptionHelper {

	private static final Logger logger = LogManager.getLogger(PasswordEncryptionHelper.class);
	private static final String SHA_SECURE_RANDOM_GEN = "SHA1PRNG";
	private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
	private static final String SECRET_KEY_TYPE = "AES";
	private static final int INITIALIZATION_VECTOR_LENGTH = 16;
	private static final int ITERATION_COUNT = 65536;
	private static final int KEY_LENGTH = 256;

	private static byte[] getRandomInitVector() {
		byte[] initVector = new byte[INITIALIZATION_VECTOR_LENGTH];

		new SecureRandom().nextBytes(initVector);
		return initVector;
	}

	public static String encrypt(SecretKey secretKey, String value) {
		byte[] initVector = getRandomInitVector();

		Cipher cipher;
		try {
			cipher = initCipher(secretKey, Cipher.ENCRYPT_MODE, initVector);
			byte[] cipherText = cipher.doFinal(value.getBytes());
			byte[] cipherTextWithInitVector = ByteBuffer.allocate(initVector.length + cipherText.length).put(initVector)
					.put(cipherText).array();

			return Base64.getEncoder().encodeToString(cipherTextWithInitVector);

		} catch (Exception e) {
			logger.error(String.format("A problem occured while encrypting password '%s'", value), e);
		}
		return StringUtils.EMPTY;
	}

	private static Cipher initCipher(SecretKey secretKey, int mode, byte[] initVector) throws Exception {
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(mode, secretKey, new IvParameterSpec(initVector));
		return cipher;
	}

	public static String decrypt(SecretKey secretKey, String value) {
		byte[] decoded = Base64.getDecoder().decode(value);
		byte[] initVector = Arrays.copyOfRange(decoded, 0, INITIALIZATION_VECTOR_LENGTH);
		byte[] cipherText = Arrays.copyOfRange(decoded, INITIALIZATION_VECTOR_LENGTH, decoded.length);

		Cipher cipher;
		try {
			cipher = initCipher(secretKey, Cipher.DECRYPT_MODE, initVector);
			byte[] original = cipher.doFinal(cipherText);
			return new String(original);
		} catch (Exception e) {
		}
		return StringUtils.EMPTY;
	}

	public static byte[] getSalt() {
		SecureRandom sr;
		try {
			sr = SecureRandom.getInstance(SHA_SECURE_RANDOM_GEN);
			byte[] salt = new byte[16];
			sr.nextBytes(salt);
			return salt;
		} catch (NoSuchAlgorithmException e) {
			logger.error(String.format("A problem occured while decryptying password"), e);
		}
		return null;
	}

	public static SecretKey getSecretKey(String password, byte[] salt) {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
			return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SECRET_KEY_TYPE);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error(String.format("A problem occured while encrypting password '%s'", password), e);
		}
		return null;
	}

	public static boolean checkPassword(String secretKey, String plainPassword, String encryptedPassword) {

		byte[] decodedKey = Base64.getDecoder().decode(secretKey);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		String decryptedPassword = decrypt(originalKey, encryptedPassword);
		logger.info(" DSAO Sign in page {} and {}",plainPassword, decryptedPassword );
		return decryptedPassword.equalsIgnoreCase(plainPassword);
    }
}