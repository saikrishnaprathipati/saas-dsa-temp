package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;

/**
 * Advisor Details Lookup Service
 */
@Service
public class AdvisorLookupService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DsaAdvisorRepository dsaAdvisorRepository;
	private final DsaAdvisorAuthRepository dsaAdvisorAuthRepository;

	@Autowired
	public AdvisorLookupService(DsaAdvisorRepository dsaAdvisorRepository,
			DsaAdvisorAuthRepository dsaAdvisorAuthRepository) {
		this.dsaAdvisorRepository = dsaAdvisorRepository;
		this.dsaAdvisorAuthRepository = dsaAdvisorAuthRepository;
	}

	public DsaAdvisor findByEmail(String emailId) {
		logger.info("Find advisor details for emailId: {}", emailId);
		DsaAdvisor advisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);
		logger.info("Advisor details: {}", advisor);
		return advisor;
	}

	public String findAdvisorActivationStatusByEmail(String emailId) {
		logger.info("Advisor auth Details using emailId :{}", emailId);
		DsaAdvisorAuthDetails advisor = dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId);

		if (null != advisor) {
			return advisor.getActivationStatus();
		}
		return null;
	}
}