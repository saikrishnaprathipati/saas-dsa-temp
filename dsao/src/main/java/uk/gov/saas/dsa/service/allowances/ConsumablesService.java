package uk.gov.saas.dsa.service.allowances;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationStudConsumables;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationStudConsumablesRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemChangeFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

/**
 * Consumables Service
 */
@Service
public class ConsumablesService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAApplicationStudConsumablesRepository consumablesRepository;
	private ApplicationService applicationService;

	public ConsumablesService(ApplicationService applicationService,
			DSAApplicationStudConsumablesRepository consumablesRepository) {
		this.consumablesRepository = consumablesRepository;
		this.applicationService = applicationService;
	}

	/**
	 * To add the consumable items
	 * 
	 * @param dsaApplicationNumber the DSA Application number
	 * @param studReferenceNmber
	 * @param consumableTypes
	 * @throws IllegalAccessException
	 */
	public void addConsumables(long dsaApplicationNumber, long studReferenceNmber,
			List<ConsumableItemFormVO> consumableTypes) throws IllegalAccessException {
		logger.info("Saving dsa application number: {} with consumabels: {}", dsaApplicationNumber, consumableTypes);
		List<DSAApplicationStudConsumables> allConsumablesInDB = (List<DSAApplicationStudConsumables>) getConsumablesByApplicationNumber(
				dsaApplicationNumber);

		ArrayList<DSAApplicationStudConsumables> consumables = new ArrayList<DSAApplicationStudConsumables>();
		for (ConsumableItemFormVO consumableTypeVO : consumableTypes) {
			ConsumableItem consumableItem = consumableTypeVO.getConsumableItem();
			DSAApplicationStudConsumables existingItem = getExistingItem(allConsumablesInDB, consumableItem);
			if (existingItem != null) {
				// Process to UPDATE existing element
				existingItem.setOtehrItemDescription(consumableTypeVO.getDescription());
				existingItem.setCost(BigDecimal.valueOf(Double.valueOf(consumableTypeVO.getCost())));
				existingItem.setLastUpdatedBy(LoggedinUserUtil.getUserId());
				existingItem.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
				consumables.add(existingItem);
			} else {
				// Process to Save new elements
				DSAApplicationStudConsumables consumable = new DSAApplicationStudConsumables();
				consumable.setStudentReferenceNumber(studReferenceNmber);
				consumable.setDsaApplicationNumber(dsaApplicationNumber);
				consumable.setConsumabelItem(consumableTypeVO.getConsumableItem());

				consumable.setOtehrItemDescription(consumableTypeVO.getDescription());

				consumable.setCost(BigDecimal.valueOf(Double.valueOf(consumableTypeVO.getCost())));

				consumable.setCreatedBy(LoggedinUserUtil.getUserId());
				consumable.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
				consumable.setLastUpdatedBy(consumable.getCreatedBy());
				consumable.setLastUpdatedDate(consumable.getCreatedDate());
				consumables.add(consumable);
			}

		}
		consumablesRepository.saveAll(consumables);

		updateSection(dsaApplicationNumber, Section.ALLOWANCES, SectionStatus.COMPLETED);
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE,
				SectionStatus.NOT_STARTED);

		logger.info("consumables added succesfully ");
	}

	/**
	 * To update the Allowances status
	 * 
	 * @param dsaApplicationNumber DSA Application Number
	 * @throws IllegalAccessException
	 */
	public void updateSection(long dsaApplicationNumber, Section section, SectionStatus status)
			throws IllegalAccessException {
		logger.info("updateSectionStatus for dsaApplicationNumber");

		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, section, status);
		 
	}

	/**
	 * To get the consumable items
	 * 
	 * @param dsaApplicationNumber
	 * @return List of ConsumableTypeVO
	 */
	public List<ConsumableTypeVO> getAllConsumableItems(long dsaApplicationNumber) {

		List<DSAApplicationStudConsumables> studConsumables = (List<DSAApplicationStudConsumables>) getConsumablesByApplicationNumber(
				dsaApplicationNumber);

		return AllowancesServiceUtil.getConsumables(studConsumables);

	}

	/**
	 * Get a consumable item from the DB
	 * 
	 * @param itemId the consumable item Id
	 * @return ConsumableTypeVO
	 * @throws IllegalAccessException
	 */
	public ConsumableTypeVO getConsumableItem(long itemId) throws IllegalAccessException {
		ConsumableTypeVO consumableTypeVO = AllowancesServiceUtil.mapToConsumableTypeVO(getitemById(itemId));
		return consumableTypeVO;
	}

	private DSAApplicationStudConsumables getitemById(long itemId) throws IllegalAccessException {
		Optional<DSAApplicationStudConsumables> consumableItemInDb = consumablesRepository.findById(itemId);
		if (consumableItemInDb.isPresent()) {
			return consumableItemInDb.get();
		} else {
			throw new IllegalAccessException("No Consumable item found for id:" + itemId);
		}
	}

	/**
	 * Update consumable item
	 * 
	 * @param item the consumable item to update
	 * @return ConsumableTypeVO
	 * @throws IllegalAccessException
	 */
	public ConsumableTypeVO updateConsumableItem(ConsumableItemChangeFormVO item) throws IllegalAccessException {
		logger.info("Updating consumable item:{}", item);
		ConsumableTypeVO consumableTypeVO = null;

		DSAApplicationStudConsumables consumableItem = getitemById(item.getId());
		consumableItem.setCost(BigDecimal.valueOf(Double.valueOf(item.getCost())));

		consumableItem.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		consumableItem.setLastUpdatedBy(LoggedinUserUtil.getUserId());
		DSAApplicationStudConsumables consumabelItem = consumablesRepository.save(consumableItem);
		consumableTypeVO = AllowancesServiceUtil.mapToConsumableTypeVO(consumabelItem);
		logger.info("consumables Updated succesfully " + consumableTypeVO);
		return consumableTypeVO;

	}

	/**
	 * Delete a consumable item
	 * 
	 * @param itemId the consumable item id to delete
	 */
	public void deleteItem(long itemId) {
		logger.info("Removing consumable item id" + itemId);
		try {

			consumablesRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.info("Item already removed id: " + itemId);
		}
		logger.info("Removing Successful");
	}

	private Iterable<DSAApplicationStudConsumables> getConsumablesByApplicationNumber(long dsaApplicationNumber) {
		Iterable<DSAApplicationStudConsumables> studConsumables = consumablesRepository
				.findByDsaApplicationNumber(dsaApplicationNumber);
		return studConsumables;
	}

	private DSAApplicationStudConsumables getExistingItem(List<DSAApplicationStudConsumables> consumables,
			ConsumableItem consumableItem) {

		Optional<DSAApplicationStudConsumables> itemInDB = consumables.stream()
				.filter(item -> item.getConsumabelItem().equals(consumableItem)).findFirst();

		return itemInDB.isPresent() ? itemInDB.get() : null;
	}

}
