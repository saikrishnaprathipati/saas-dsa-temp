package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.model.ActivationStatusType;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdvisorLookupServiceTest {

	@MockitoBean
	private DsaAdvisorRepository dsaAdvisorRepository;
	@MockitoBean
	private DsaAdvisorAuthRepository dsaAdvisorAuthRepository;

	private AdvisorLookupService advisorLookupService;

	@BeforeEach
	public void setUp() {
		advisorLookupService = new AdvisorLookupService(dsaAdvisorRepository, dsaAdvisorAuthRepository);
	}

	@Test
	public void testFindByEmail() {
		String email = "test@gcu.co.uk";
		DsaAdvisor dsaAdvisor = new DsaAdvisor();
		when(dsaAdvisorRepository.findByEmailIgnoreCase(email)).thenReturn(dsaAdvisor);

		DsaAdvisor result = advisorLookupService.findByEmail(email);
		assertNotNull(result);
	}

	@Test
	public void test_findAdvisorActivationStatusByEmail() {
		String email = "test@gcu.co.uk";
		DsaAdvisorAuthDetails dsaAdvisor = new DsaAdvisorAuthDetails();
		dsaAdvisor.setActivationStatus(ActivationStatusType.ACTIVATION_REQUESTED.name());
		when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(email)).thenReturn(dsaAdvisor);

		String response = advisorLookupService.findAdvisorActivationStatusByEmail(email);
		assertEquals(ResponseCode.ACTIVATION_REQUESTED.name(), response);
	}
}