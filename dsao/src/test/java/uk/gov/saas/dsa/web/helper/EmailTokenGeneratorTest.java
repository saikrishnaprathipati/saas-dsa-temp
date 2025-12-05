package uk.gov.saas.dsa.web.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.saas.dsa.domain.helpers.EncryptionHelper;

public class EmailTokenGeneratorTest {

	@Test
	public void testGenerateRegistrationCode() {
		String token = EmailTokenGenerator.generateRegistrationCode();
		Assertions.assertNotNull(token);
		//System.out.println(EncryptionHelper.decrypt("F31FDFCEBC37DE06804BEDE1911EF84F"));
	}
}
