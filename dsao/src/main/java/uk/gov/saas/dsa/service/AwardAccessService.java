package uk.gov.saas.dsa.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAAwardAccess;
import uk.gov.saas.dsa.persistence.DSAAwardAccessRepository;

/**
 * DSA Declarations Service
 */
@Service
public class AwardAccessService {
	private final DSAAwardAccessRepository awardAccessRepository;

	/**
	 * DeclarationsService constructor
	 * 
	 * @param declarationTypeRepository
	 * @param applicationService
	 */
	public AwardAccessService(DSAAwardAccessRepository declarationTypeRepository) {
		this.awardAccessRepository = declarationTypeRepository;
	}

	public void saveAwardAccess(long dsaNo, String canAccess) {
		DSAAwardAccess entity = getAwardAccess(dsaNo);
		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
		if (entity != null) {
			entity.setAdvisorCanAccess(canAccess);
			entity.setLastUpdatedDate(timestamp);
		} else {
			entity = new DSAAwardAccess();
			entity.setDsaApplicationNumber(dsaNo);
			entity.setAdvisorCanAccess(canAccess);
			entity.setCreatedDate(timestamp);
			entity.setLastUpdatedDate(timestamp);
		}
		awardAccessRepository.save(entity);
	}

	public DSAAwardAccess getAwardAccess(long dsaNo) {
		DSAAwardAccess entity = awardAccessRepository.findByDsaApplicationNumber(dsaNo);
		return entity;
	}
}
