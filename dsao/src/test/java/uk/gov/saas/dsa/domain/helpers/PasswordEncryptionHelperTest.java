package uk.gov.saas.dsa.domain.helpers;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

public class PasswordEncryptionHelperTest {
	
   
	@Test
	void encryptAndDecryptPassword() throws Exception {
        String plainText = "My password";
    	byte[] salt = PasswordEncryptionHelper.getSalt();
        SecretKey secretKey = PasswordEncryptionHelper.getSecretKey(plainText, salt);

        String cipherText = PasswordEncryptionHelper.encrypt(secretKey, plainText);
        String decryptedText = PasswordEncryptionHelper.decrypt(secretKey, cipherText);
        
		Assertions.assertEquals(plainText, decryptedText);
		
        //encode and save to DB as string
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        //decode and use as key
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        Assertions.assertEquals(plainText, PasswordEncryptionHelper.decrypt(originalKey, cipherText));
    }
}