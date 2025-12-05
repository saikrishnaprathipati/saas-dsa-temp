package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.DsaStudentAuthDetails;
import uk.gov.saas.dsa.persistence.DsaStudentAuthDetailsRepository;

/**
 * Advisor Details Lookup Service
 */
@Service
public class StudentLookupService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DsaStudentAuthDetailsRepository dsaStudentAuthDetailsRepository;

	@Autowired
	public StudentLookupService(DsaStudentAuthDetailsRepository dsaStudentAuthDetailsRepository) {
		this.dsaStudentAuthDetailsRepository = dsaStudentAuthDetailsRepository;
	}

	public DsaStudentAuthDetails findStudentBySuid(String suid) {
		logger.info("Student Details suid :{}", suid);
		return dsaStudentAuthDetailsRepository.findBySuid(suid);
	}

	public DsaStudentAuthDetails findStudentByStudRefNumber(Long studRefNumber) {
		logger.info("Student auth Details studRefNumber :{}", studRefNumber);
		return dsaStudentAuthDetailsRepository.findByStudRefNumber(studRefNumber);
	}
}