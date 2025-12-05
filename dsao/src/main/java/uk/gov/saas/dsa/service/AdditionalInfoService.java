package uk.gov.saas.dsa.service;

import static uk.gov.saas.dsa.model.Section.ADDITIONAL_INFO;
import static uk.gov.saas.dsa.model.SectionStatus.COMPLETED;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAAppAdditionalInformation;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAAdditionalInfoRepository;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

@Service
public class AdditionalInfoService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAAdditionalInfoRepository additionalInfoRepo;
	private ApplicationService applicationServicve;

	public AdditionalInfoService(ApplicationService service, DSAAdditionalInfoRepository repo) {
		this.additionalInfoRepo = repo;
		this.applicationServicve = service;
	}

	public DSAAppAdditionalInformation getAdditionalInfo(long dsaNo) {

		DSAAppAdditionalInformation info = additionalInfoRepo.findByDsaApplicationNumber(dsaNo);
		logger.info("Additional info for dsa id {}: {}", dsaNo, info);
		return info;
	}

	public DSAAppAdditionalInformation addAdditionalInfo(long dsaApplicationNumber, String text)
			throws IllegalAccessException {
		text = text != null ? text.trim() : text;
		DSAAppAdditionalInformation entity = getAdditionalInfo(dsaApplicationNumber);
		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
		String userId = LoggedinUserUtil.getUserId();
		if (entity != null) {
			entity.setInfoText(text);
			entity.setLastUpdatedDate(timestamp);
			entity.setLastUpdatedBy(userId);

		} else {
			entity = new DSAAppAdditionalInformation();
			entity.setDsaApplicationNumber(dsaApplicationNumber);
			entity.setInfoText(text);
			entity.setCreatedBy(userId);
			entity.setCreatedDate(timestamp);
			entity.setLastUpdatedBy(userId);
			entity.setLastUpdatedDate(timestamp);
		}
		additionalInfoRepo.save(entity);
		 
		ServiceUtil.updateSectionStatus(applicationServicve, dsaApplicationNumber, ADDITIONAL_INFO, COMPLETED);
	 
		return entity;
	}
}
