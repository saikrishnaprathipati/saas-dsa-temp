package uk.gov.saas.dsa.service.allowances;

import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.entityToNMPHVO;
import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.getNMPHAllowances;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationStudNMPH;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationStudNMPHRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

/**
 * NMPH Allowances Service
 */
@Service
public class NMPHAllowancesService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAApplicationStudNMPHRepository nmphRepository;
	private final ApplicationService applicationService;

	public NMPHAllowancesService(DSAApplicationStudNMPHRepository nmphRepository,
			ApplicationService applicationService) {
		this.nmphRepository = nmphRepository;
		this.applicationService = applicationService;
	}

	/**
	 * Get allowances by dsa application number
	 * 
	 * @param dsaApplicationNumber
	 * @return
	 */
	public List<NMPHAllowanceVO> getAllNMPHAllowances(long dsaApplicationNumber) {
		logger.info("getAllNMPHAllowances for:{}", dsaApplicationNumber);
		List<DSAApplicationStudNMPH> list = nmphRepository.findByDsaApplicationNumber(dsaApplicationNumber);
		List<NMPHAllowanceVO> voList = getNMPHAllowances(list);
		return voList;
	}

	/**
	 * Add nmph
	 * 
	 * @param item
	 * @throws IllegalAccessException 
	 */
	public void addNMPHAllowance(NMPHAllowanceVO item) throws IllegalAccessException {
		logger.info("Add nmph :{}", item);
		DSAApplicationStudNMPH entity = new DSAApplicationStudNMPH();
		voToEntity(item, entity);

		entity.setCreatedBy(LoggedinUserUtil.getUserId());
		entity.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		entity.setLastUpdatedBy(entity.getCreatedBy());
		entity.setLastUpdatedDate(entity.getCreatedDate());

		DSAApplicationStudNMPH savedEntity = nmphRepository.save(entity);

		if(savedEntity != null) {
			ServiceUtil.updateSectionStatus(applicationService, savedEntity.getDsaApplicationNumber(), Section.ALLOWANCES,
					SectionStatus.COMPLETED);
			ServiceUtil.updateSectionStatus(applicationService, savedEntity.getDsaApplicationNumber(),
					Section.NEEDS_ASSESSMENT_FEE, SectionStatus.NOT_STARTED);
		}
	}

	/**
	 * Get NMPH details by id
	 * 
	 * @param itemId
	 * @return NMPHAllowanceVO
	 * @throws IllegalAccessException
	 */
	public NMPHAllowanceVO getNMPHItem(long itemId) throws IllegalAccessException {
		logger.info("Get nmph by id :{}", itemId);
		return entityToNMPHVO(getItemById(itemId));
	}

	/**
	 * Change the NMPH
	 * 
	 * @param requestVO
	 * @throws IllegalAccessException
	 */
	public void changeNMPHItem(NMPHAllowanceVO requestVO) throws IllegalAccessException {
		logger.info("change NMPH Item  :{}", requestVO);
		DSAApplicationStudNMPH entity = getItemById(requestVO.getId());
		NMPHAllowanceVO vo = entityToNMPHVO(entity);

		if (!vo.equals(requestVO)) {
			voToEntity(requestVO, entity);
			entity.setLastUpdatedBy(LoggedinUserUtil.getUserId());
			entity.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
			nmphRepository.save(entity);
			logger.info("NMPH VO item updated succesfully:{}", requestVO);
		} else {
			logger.info("NMPH VO ITEM DOES NOT CHANGED NOT UPDATING IT:{}", requestVO);
		}

	}

	/**
	 * Delete item
	 * 
	 * @param itemId
	 */
	public void deleteItem(Long itemId) {
		logger.info("Delte NMPH by Id:{}", itemId);
		try {
			nmphRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id: " + itemId);
		}
	}

	private DSAApplicationStudNMPH getItemById(long itemId) throws IllegalAccessException {
		Optional<DSAApplicationStudNMPH> entityData = nmphRepository.findById(itemId);
		if (entityData.isPresent()) {
			return entityData.get();
		} else {
			throw new IllegalAccessException("No NMPH item found for id:" + itemId);
		}
	}

	private void voToEntity(NMPHAllowanceVO item, DSAApplicationStudNMPH entity) {
		entity.setDsaApplicationNumber(item.getDsaApplicationNumber());
		entity.setStudentReferenceNumber(item.getStudentReferenceNumber());
		entity.setTypeOfSupport(item.getTypeOfSupport());
		entity.setRecommendedProvider(item.getRecommendedProvider());
		entity.setHourlyRate(item.getHourlyRate());
		entity.setHoursPerWeek(item.getHours());
		entity.setWeeks(item.getWeeks());
		entity.setCost(item.getCost());
	}

}
