package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.persistence.DsaAdvisorLoginRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdvisorLoginServiceTest {
	@MockitoBean
	private DsaAdvisorRepository dsaAdvisorRepository;
	@MockitoBean
	private DsaAdvisorLoginRepository dsaAdvisorLoginRepository;
	private AdvisorLoginService advisorLoginService;

	@BeforeEach
	public void setUp() {
		advisorLoginService = new AdvisorLoginService(dsaAdvisorLoginRepository, dsaAdvisorRepository);
	}

	@Test
	public void testFindByEmail() {
		String email = "test@gcu.co.uk";
		DsaAdvisorLoginDetails dsaAdvisor = new DsaAdvisorLoginDetails();
		when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(email)).thenReturn(dsaAdvisor);

		DsaAdvisorLoginDetails result = advisorLoginService.findByEmail(email);
		assertNotNull(result);
	}

	@Test
	public void testFindAdvisorByEmail() {
		String email = "test@gcu.co.uk";
		DsaAdvisor dsaAdvisor = new DsaAdvisor();
		when(dsaAdvisorRepository.findByEmailIgnoreCase(email)).thenReturn(dsaAdvisor);

		DsaAdvisor result = advisorLoginService.findAdvisorByEmail(email);
		assertNotNull(result);
	}

	@Test
	public void testSaveFailedLoginDetails() {
		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
		dsaAdvisorLoginDetails.setUserId("123");
		DsaAdvisorLoginDetails result = advisorLoginService.saveFailedLoginDetails(1, dsaAdvisorLoginDetails);
		assertNull(result);
	}
}
