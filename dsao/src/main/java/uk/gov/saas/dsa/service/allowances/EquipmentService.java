package uk.gov.saas.dsa.service.allowances;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationStudEquipment;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationStudEquipmentRepository;
import uk.gov.saas.dsa.persistence.readonly.DSALrgEquipmentPaymentInstRepository;
import uk.gov.saas.dsa.persistence.readonly.StudCourseYearRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

/**
 * Equipment Allowances Service
 */
@Service
public class EquipmentService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DSAApplicationStudEquipmentRepository equipmentRepository;
	private final ApplicationService applicationService;

	public EquipmentService(DSAApplicationStudEquipmentRepository equipmentRepository,
			ApplicationService applicationService, StudCourseYearRepository studCourseYearRepository,
			DSALrgEquipmentPaymentInstRepository dsaLrgEquipmentPaymentInstRepository) {
		this.equipmentRepository = equipmentRepository;
		this.applicationService = applicationService;
	}

	public List<EquipmentAllowanceVO> getAllEquipmentAllowances(long dsaApplicationNumber) {
		logger.info("getAllEquipmentAllowances for:{}", dsaApplicationNumber);
		List<DSAApplicationStudEquipment> list = equipmentRepository.findByDsaApplicationNumber(dsaApplicationNumber);

		return AllowancesServiceUtil.getEquipments(list);
	}

	public Page<EquipmentAllowanceVO> findPaginated(Pageable pageable, List<EquipmentAllowanceVO> equipment) {
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = pageSize * currentPage;

		List<EquipmentAllowanceVO> list;

		if (equipment.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, equipment.size());
			list = equipment.subList(startItem, toIndex);
		}

		return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), equipment.size());
	}

	public void addEquipmentAllowance(EquipmentAllowanceVO item) throws IllegalAccessException {
		logger.info("Add equipment :{}", item);
		DSAApplicationStudEquipment entity = new DSAApplicationStudEquipment();
		voToEntity(item, entity);

		entity.setCreatedBy(LoggedinUserUtil.getUserId());
		entity.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		entity.setLastUpdatedBy(entity.getCreatedBy());
		entity.setLastUpdatedDate(entity.getCreatedDate());

		equipmentRepository.save(entity);
		ServiceUtil.updateSectionStatus(applicationService, item.getDsaApplicationNumber(), Section.ALLOWANCES,
				SectionStatus.COMPLETED);
		ServiceUtil.updateSectionStatus(applicationService, item.getDsaApplicationNumber(),
				Section.NEEDS_ASSESSMENT_FEE, SectionStatus.NOT_STARTED);
	}

	public EquipmentAllowanceVO getEquipmentItem(long itemId) throws IllegalAccessException {
		logger.info("Get equipment by id :{}", itemId);
		return AllowancesServiceUtil.entityToEquipmentVO(getItemById(itemId));
	}

	public void changeEquipment(EquipmentAllowanceVO requestVO) throws IllegalAccessException {
		logger.info("change Equipment Item  :{}", requestVO);
		DSAApplicationStudEquipment entity = getItemById(requestVO.getId());
		EquipmentAllowanceVO vo = AllowancesServiceUtil.entityToEquipmentVO(entity);

		if (!vo.equals(requestVO)) {
			voToEntity(requestVO, entity);
			entity.setLastUpdatedBy(LoggedinUserUtil.getUserId());
			entity.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
			equipmentRepository.save(entity);
			logger.info("Equipment VO item updated successfully:{}", requestVO);
		} else {
			logger.info("Equipment VO ITEM DID NOT CHANGE, NO UPDATE :{}", requestVO);
		}
	}

	/**
	 * Delete item
	 *
	 * @param itemId The id of the item
	 */
	public void deleteItem(Long itemId) {
		logger.info("Delete Equipment by Id:{}", itemId);
		try {
			equipmentRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id: " + itemId);
		}
	}

	private DSAApplicationStudEquipment getItemById(long itemId) throws IllegalAccessException {
		Optional<DSAApplicationStudEquipment> entityData = equipmentRepository.findById(itemId);
		if (entityData.isPresent()) {
			return entityData.get();
		} else {
			throw new IllegalAccessException("No Equipment item found for id:" + itemId);
		}
	}

	private void voToEntity(EquipmentAllowanceVO item, DSAApplicationStudEquipment entity) {
		entity.setDsaApplicationNumber(item.getDsaApplicationNumber());
		entity.setStudentReferenceNumber(item.getStudentReferenceNumber());
		entity.setProductName(item.getProductName());
		entity.setDescription(item.getDescription());
		entity.setCost(item.getCost());
	}

}
