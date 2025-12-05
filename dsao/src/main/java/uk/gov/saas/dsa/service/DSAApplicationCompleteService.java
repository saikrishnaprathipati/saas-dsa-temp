package uk.gov.saas.dsa.service;

import java.sql.Date;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationComplete;
import uk.gov.saas.dsa.persistence.readonly.DSAApplicationCompleteRepository;

@Service
public class DSAApplicationCompleteService {
	private static final String D = "D";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAApplicationCompleteRepository dsaApplicationCompleteRepository;

	public DSAApplicationCompleteService(DSAApplicationCompleteRepository dsaApplicationCompleteRepository) {
		this.dsaApplicationCompleteRepository = dsaApplicationCompleteRepository;
	}

	public void saveCompleteWeAppData(long dsaAppno, long studRefNo, int sessionCode) {
		DSAApplicationComplete item = new DSAApplicationComplete();
		item.setDsaApplicationNumber(dsaAppno);
		item.setStudentReferenceNumber(studRefNo);
		item.setSessionCode(sessionCode);
		item.setApplicationType(D);
		item.setWebSubmitted(Date.valueOf(LocalDateTime.now().toLocalDate()));
		dsaApplicationCompleteRepository.save(item);
		logger.info("COMPLETE_WEB_APP_DSA saved succesfully {}", item);
	}

}
