package uk.gov.saas.dsa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = DSAApplication.class)
@ActiveProfiles({ DSAOApplicationTests.TEST_PROFILE })
public class DSAOApplicationTests {
	public static final String TEST_PROFILE = "localdev";

	@Test
	void contextLoads() {
	}

}
