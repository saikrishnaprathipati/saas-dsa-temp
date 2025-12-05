package uk.gov.saas.dsa.service.allowances;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.DSAApplicationTravelExp;
import uk.gov.saas.dsa.domain.DSAApplicationTravelProvider;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.persistence.DSAApplicationStudTravelExpRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudTravelProviderRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.travelExp.TaxiProviderVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.entityToTravelExpVO;
import static uk.gov.saas.dsa.service.allowances.AllowancesServiceUtil.getTravelExpenses;

/**
 * Travel EXP Allowances Service
 */
@Service
public class TravelExpAllowancesService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DSAApplicationStudTravelExpRepository travelExpRepository;
	private final DSAApplicationStudTravelProviderRepository travelProviderRepository;
	private final ApplicationService applicationService;

	public TravelExpAllowancesService(DSAApplicationStudTravelExpRepository travelExpRepository,
									  DSAApplicationStudTravelProviderRepository travelProviderRepository,
									  ApplicationService applicationService) {
		this.travelExpRepository = travelExpRepository;
		this.travelProviderRepository = travelProviderRepository;
		this.applicationService = applicationService;
	}

	/**
	 * Get allowances by dsa application number
	 */
	public List<TravelExpAllowanceVO> getTravelExpAllowances(long dsaApplicationNumber) {
		logger.info("Getting TravelExpAllowances for: {}", dsaApplicationNumber);
		List<DSAApplicationTravelExp> list = getExpenses(dsaApplicationNumber);
		return getTravelExpenses(list);
	}

	/**
	 * Add TravelExp
	 */
	public TravelExpAllowanceVO addTravelExpAllowance(TravelExpAllowanceVO requestVO)
			throws IllegalAccessException {
		logger.info("Add TravelExp :{}", requestVO);
		DSAApplicationTravelExp entity;
		DSAApplicationTravelExp existingItem = getExistingItem(requestVO);
		if (existingItem == null) {
			entity = new DSAApplicationTravelExp();
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

		DSAApplicationTravelExp savedEntity = travelExpRepository.save(entity);
		logger.info("Add TravelExp added succesfully {}", savedEntity);
		TravelExpAllowanceVO travelExpAllowanceVO = entityToTravelExpVO(savedEntity);
		logger.info("Saved VO {}", travelExpAllowanceVO);
		ServiceUtil.updateSectionStatus(applicationService, savedEntity.getDsaApplicationNumber(), Section.ALLOWANCES,
				SectionStatus.COMPLETED);
		ServiceUtil.updateSectionStatus(applicationService, savedEntity.getDsaApplicationNumber(),
				Section.NEEDS_ASSESSMENT_FEE, SectionStatus.NOT_STARTED);
		return travelExpAllowanceVO;
	}

	public void deleteTravelProvider(Long itemId) {
		logger.info("Delete Travel provider by Id:{}", itemId);
		try {
			travelProviderRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id : {}", itemId);
		}
	}

	/**
	 * Delete item
	 */
	public void deleteTravelExp(Long itemId) {
		logger.info("Delete TravelExp by Id:{}", itemId);
		try {
			travelExpRepository.deleteById(itemId);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id: {}", itemId);
		}
	}

	private void voToEntity(TravelExpAllowanceVO item, DSAApplicationTravelExp entity) {
		entity.setDsaApplicationNumber(item.getDsaApplicationNumber());
		entity.setReturnJourneys(item.getReturnJourneys());
		entity.setWeeks(item.getWeeks());

		entity.setStartLocationPostcode(item.getStartLocationPostcode().toUpperCase());
		entity.setEndLocationPostcode(item.getEndLocationPostcode().toUpperCase());

		entity.setTravelExpType(item.getTravelExpType());
		entity.setStudentReferenceNumber(item.getStudentReferenceNumber());

		// Divide between travel expense type
		if (TravelExpType.TAXI.equals(item.getTravelExpType())) {
			List<DSAApplicationTravelProvider> travelProviders = travelProviders(entity, item.getTaxiProvidersList());
			entity.setTravelProviders(travelProviders);
		} else {
			String vehicleType = item.getVehicleType();
			entity.setVehicleType(vehicleType);

			if (!vehicleType.equalsIgnoreCase("electric")) {
				entity.setFuelCost(item.getFuelCost());
				entity.setMilesPerGallon(item.getMilesPerGallon());
				entity.setKwhCost(BigDecimal.ZERO);
				entity.setKwhCapacity(0);
				entity.setRangeOfCar(0);
			} else {
				entity.setKwhCost(item.getKwhCost());
				entity.setKwhCapacity(item.getKwhCapacity());
				entity.setRangeOfCar(item.getRangeOfCar());
				entity.setFuelCost(BigDecimal.ZERO);
				entity.setMilesPerGallon(0);
			}
		}
	}

	private List<DSAApplicationTravelProvider> travelProviders(DSAApplicationTravelExp entity,
															   List<TaxiProviderVO> voRequest) {
		List<DSAApplicationTravelProvider> existingEntries = entity.getTravelProviders() == null
				? new ArrayList<>()
				: entity.getTravelProviders();

		List<DSAApplicationTravelProvider> freshEntries = voRequest.stream().filter(t -> t.getId() == 0).map(item -> {

			DSAApplicationTravelProvider provider = new DSAApplicationTravelProvider();

			provider.setProviderName(item.getRecommendedProvider());
			provider.setIsApprovedContract(item.getApprovedContractor());
			provider.setCost(item.getCost());
			provider.setCreatedBy(entity.getCreatedBy());
			provider.setCreatedDate(entity.getCreatedDate());
			provider.setLastUpdatedBy(entity.getLastUpdatedBy());
			provider.setLastUpdatedDate(entity.getLastUpdatedDate());

			provider.setTravelExp(entity);
			return provider;
		}).collect(Collectors.toList());
		existingEntries.addAll(freshEntries);

		logger.info("existingEntries :{}", existingEntries);
		logger.info("freshEntries to save:{}", freshEntries);
		return existingEntries;
	}

	private List<DSAApplicationTravelExp> getExpenses(long dsaApplicationNumber) {
		return travelExpRepository.findByDsaApplicationNumber(dsaApplicationNumber);
	}

	private DSAApplicationTravelExp getExistingItem(TravelExpAllowanceVO item) {
		DSAApplicationTravelExp expense = null;
		Optional<DSAApplicationTravelExp> element = getExpenses(item.getDsaApplicationNumber()).stream()
				.filter(t -> t.getTravelExpType().equals(item.getTravelExpType())).findFirst();
		if (element.isPresent()) {
			expense = element.get();
		}
		return expense;
	}
}
