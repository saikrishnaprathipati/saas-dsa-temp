package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.persistence.DsaAdvisorLoginRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Advisor login details Service
 */
@Service
public class AdvisorLoginService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DsaAdvisorLoginRepository dsaAdvisorLoginRepository;
	private DsaAdvisorRepository dsaAdvisorRepository;

	@Autowired
	public AdvisorLoginService(DsaAdvisorLoginRepository dsaAdvisorLoginRepository, DsaAdvisorRepository dsaAdvisorRepository) {
		this.dsaAdvisorLoginRepository = dsaAdvisorLoginRepository;
		this.dsaAdvisorRepository = dsaAdvisorRepository;
	}

	public DsaAdvisorLoginDetails findByEmail(String emailId) {
		logger.info("Advisor login details using emailId :{}", emailId);
		return dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId);
	}
	
	public DsaAdvisorLoginDetails saveFailedLoginDetails(int failedPasswordCount, DsaAdvisorLoginDetails dsaAdvisorLoginDetails) {
		logger.info("Advisor save failed login details using emailId :{}", dsaAdvisorLoginDetails);
		dsaAdvisorLoginDetails.setFailedPasswordCount(failedPasswordCount);
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		return dsaAdvisorLoginRepository.save(dsaAdvisorLoginDetails);
	}
	
	public DsaAdvisor findAdvisorByEmail(String emailId) {
		logger.info("Advisor login details using emailId :{}", emailId);
		return dsaAdvisorRepository.findByEmailIgnoreCase(emailId);
	}

}
