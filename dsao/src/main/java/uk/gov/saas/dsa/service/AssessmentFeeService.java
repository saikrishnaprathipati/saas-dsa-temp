package uk.gov.saas.dsa.service;

import java.sql.Timestamp;
import java.time.LocalDateTime; 
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationAssessmentFee;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationAssessmentFeeRepository;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

/**
 * Assessment fee Service
 */
@Service
public class AssessmentFeeService {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAApplicationAssessmentFeeRepository applicationAssessmentFeeRepository;
	private ApplicationService applicationService;

	public AssessmentFeeService(DSAApplicationAssessmentFeeRepository applicationAssessmentFeeRepository,
			ApplicationService applicationService) {
		this.applicationAssessmentFeeRepository = applicationAssessmentFeeRepository;
		this.applicationService = applicationService;
	}

	public void addAssessmentFee(AssessmentFeeVO requestVO) throws IllegalAccessException {
		logger.info("Add assessment fee :{}", requestVO);
		DSAApplicationAssessmentFee entity = new DSAApplicationAssessmentFee();
		List<DSAApplicationAssessmentFee> allItems = getAllItems(requestVO.getDsaApplicationNumber());
		DSAApplicationAssessmentFee existingItem = null;
		if (allItems != null) {
			Optional<DSAApplicationAssessmentFee> findFirst = allItems.stream()
					.filter(t -> t.getId() == requestVO.getId()).findFirst();
			existingItem = findFirst.isPresent() ? findFirst.get() : null;
		}

		if (existingItem == null) {
			entity = new DSAApplicationAssessmentFee();
			entity.setCreatedBy(LoggedinUserUtil.getUserId());
			entity.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
			entity.setLastUpdatedBy(entity.getCreatedBy());
			entity.setLastUpdatedDate(entity.getCreatedDate());
			voToEntity(requestVO, entity);
		} else {
			entity = existingItem;
			voToEntity(requestVO, entity);
			entity.setLastUpdatedBy(LoggedinUserUtil.getUserId());
			entity.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		}

		applicationAssessmentFeeRepository.save(entity);
		if (allItems.size() == 0) {
			updateSectionStatus(requestVO.getDsaApplicationNumber());
		}
	}

	public List<AssessmentFeeVO> getAssessmentItems(long dsaApplicationNumber) {
		logger.info("Get AssessmentFee by dsaApplicationNumber :{}", dsaApplicationNumber);
		return ServiceUtil.setAssessmentFeeItems(getAllItems(dsaApplicationNumber));

	}

	private List<DSAApplicationAssessmentFee> getAllItems(long dsaApplicationNumber) {
		final List<DSAApplicationAssessmentFee> list = applicationAssessmentFeeRepository
				.findByDsaApplicationNumber(dsaApplicationNumber);
		return list;
	}

	public AssessmentFeeVO getAssessmentItem(long itemId) {
		logger.info("Get AssessmentFee by id :{}", itemId);
		return entityToVO(getItemById(itemId));
	}

	private DSAApplicationAssessmentFee getItemById(long itemId) {
		Optional<DSAApplicationAssessmentFee> entityData = applicationAssessmentFeeRepository.findById(itemId);
		return entityData.isPresent() ? entityData.get() : null;
	}

	private void voToEntity(AssessmentFeeVO item, DSAApplicationAssessmentFee entity) {
		entity.setDsaApplicationNumber(item.getDsaApplicationNumber());
		entity.setStudentReferenceNumber(item.getStudentReferenceNumber());
		entity.setAssessmentCentreName(item.getAssessmentFeeCentreName());
		entity.setAssessorName(item.getAssessorName());
		entity.setTotalHours(item.getTotalHours());
		entity.setCost(item.getCost());
	}

	private AssessmentFeeVO entityToVO(DSAApplicationAssessmentFee feeItme) {
 
		return ServiceUtil.setAssessmentFeeVO(feeItme);
	}

	private void updateSectionStatus(long dsaApplicationNumber) throws IllegalAccessException {
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE,
				SectionStatus.STARTED);

	}

	public void deleteItem(Long itemId) {
		logger.info("Delte AssessmentFee by Id:{}", itemId);
		try {
			applicationAssessmentFeeRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id: " + itemId);
		}
	}

}
