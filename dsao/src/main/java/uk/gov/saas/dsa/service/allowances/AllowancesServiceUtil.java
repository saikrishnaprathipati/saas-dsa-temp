package uk.gov.saas.dsa.service.allowances;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatValue;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.populateCost;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.saas.dsa.domain.DSAApplicationStudAccommodation;
import uk.gov.saas.dsa.domain.DSAApplicationStudConsumables;
import uk.gov.saas.dsa.domain.DSAApplicationStudEquipment;
import uk.gov.saas.dsa.domain.DSAApplicationStudNMPH;
import uk.gov.saas.dsa.domain.DSAApplicationTravelExp;
import uk.gov.saas.dsa.domain.DSAApplicationTravelProvider;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.travelExp.TaxiProviderVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;
import uk.gov.saas.dsa.web.helper.FindStudentHelper;

public class AllowancesServiceUtil {
	private static final Logger logger = LogManager.getLogger(FindStudentHelper.class);

	public static List<ConsumableTypeVO> getConsumables(List<DSAApplicationStudConsumables> studConsumables) {
		List<ConsumableTypeVO> list = new ArrayList<>();
		if (studConsumables != null) {
			List<ConsumableItem> sortedItems = studConsumables.stream()
					.map(DSAApplicationStudConsumables::getConsumabelItem)
					.sorted(Comparator.comparing(ConsumableItem::getOrder)).collect(Collectors.toList());
			for (ConsumableItem consumable : sortedItems) {
				DSAApplicationStudConsumables dsaApplicationStudConsumables = studConsumables.stream()
						.filter(t -> t.getConsumabelItem().equals(consumable)).findFirst().get();
				ConsumableTypeVO consumableTypeVO = mapToConsumableTypeVO(dsaApplicationStudConsumables);
				list.add(consumableTypeVO);
			}
		}
		return list;
	}

	public static ConsumableTypeVO mapToConsumableTypeVO(DSAApplicationStudConsumables consumable) {
		ConsumableTypeVO consumableTypeVO = new ConsumableTypeVO();
		consumableTypeVO.setId(consumable.getId());

		consumableTypeVO.setCost(formatValue(consumable.getCost(), 2));
		consumableTypeVO.setCostString(currencyLocalisation(consumable.getCost().doubleValue()));
		consumableTypeVO.setDsaApplicationNumber(consumable.getDsaApplicationNumber());
		consumableTypeVO.setStudentReferenceNumber(consumable.getStudentReferenceNumber());
		consumableTypeVO.setConsumableItem(consumable.getConsumabelItem());
		consumableTypeVO.setOtehrItemText(consumable.getOtehrItemDescription());
		return consumableTypeVO;
	}

	public static List<NMPHAllowanceVO> getNMPHAllowances(List<DSAApplicationStudNMPH> list) {
		List<NMPHAllowanceVO> nmphList = new ArrayList<>();
		if (list != null) {
			nmphList = list.stream()
					.sorted(Collections.reverseOrder(Comparator.comparing(DSAApplicationStudNMPH::getLastUpdatedDate)))
					.map(AllowancesServiceUtil::entityToNMPHVO).collect(Collectors.toList());
		}
		return nmphList;
	}

	public static NMPHAllowanceVO entityToNMPHVO(DSAApplicationStudNMPH nmph) {
		NMPHAllowanceVO item = NMPHAllowanceVO.builder().id(nmph.getId())
				.dsaApplicationNumber(nmph.getDsaApplicationNumber())
				.studentReferenceNumber(nmph.getStudentReferenceNumber()).typeOfSupport(nmph.getTypeOfSupport())
				.recommendedProvider(nmph.getRecommendedProvider()).hours(nmph.getHoursPerWeek()).weeks(nmph.getWeeks())
				.hourlyRateStr(currencyLocalisation(nmph.getHourlyRate().doubleValue()))
				.hourlyRate(populateCost(nmph.getHourlyRate()))
				.costStr(currencyLocalisation(nmph.getCost().doubleValue()))

				.cost(populateCost(nmph.getCost())).build();
		logger.info("NMPH item -- {}", item);
		return item;
	}

	public static List<EquipmentAllowanceVO> getEquipments(List<DSAApplicationStudEquipment> list) {
		logger.info("Equipments in DB -- {}", list);
		List<EquipmentAllowanceVO> equipments = new ArrayList<>();
		if (list != null) {
			equipments = list.stream()
					.sorted(Collections
							.reverseOrder(Comparator.comparing(DSAApplicationStudEquipment::getLastUpdatedDate)))
					.map(AllowancesServiceUtil::entityToEquipmentVO).collect(Collectors.toList());

			logger.info("Sorted list in DB -- {}", equipments);
		}
		return equipments;
	}

	public static EquipmentAllowanceVO entityToEquipmentVO(DSAApplicationStudEquipment equipment) {
		EquipmentAllowanceVO item = EquipmentAllowanceVO.builder().id(equipment.getId())
				.dsaApplicationNumber(equipment.getDsaApplicationNumber())
				.studentReferenceNumber(equipment.getStudentReferenceNumber()).productName(equipment.getProductName())
				.description(equipment.getDescription())

				.costStr(currencyLocalisation(equipment.getCost().doubleValue()))
				.cost(BigDecimal.valueOf(equipment.getCost().doubleValue())).build();
		logger.info("Equipment -- {}", item);
		return item;
	}

	public static List<TravelExpAllowanceVO> getTravelExpenses(List<DSAApplicationTravelExp> list) {
		List<TravelExpAllowanceVO> voList = new ArrayList<>();
		if (list != null) {

			voList = list.stream().map(AllowancesServiceUtil::entityToTravelExpVO).sorted(Comparator.comparing(t -> {
				TravelExpType travelExpType = t.getTravelExpType();
				return travelExpType.getOrder();
			})).collect(Collectors.toList());
		}
		return voList;
	}

	public static TravelExpAllowanceVO entityToTravelExpVO(DSAApplicationTravelExp travelExpEntity) {

		return TravelExpAllowanceVO.builder().travelExpNo(travelExpEntity.getTravelExpNo())
				.id(travelExpEntity.getTravelExpNo()).dsaApplicationNumber(travelExpEntity.getDsaApplicationNumber())
				.studentReferenceNumber(travelExpEntity.getStudentReferenceNumber())
				.startLocationPostcode(travelExpEntity.getStartLocationPostcode().toUpperCase())
				.endLocationPostcode(travelExpEntity.getEndLocationPostcode().toUpperCase())
				.travelExpType(travelExpEntity.getTravelExpType()).returnJourneys(travelExpEntity.getReturnJourneys())
				.weeks(travelExpEntity.getWeeks()).vehicleType(travelExpEntity.getVehicleType())
				.fuelCost(travelExpEntity.getFuelCost())
				.fuelCostStr(travelExpEntity.getFuelCost() == null ? ""
						: currencyLocalisation(travelExpEntity.getFuelCost().doubleValue()))
				.milesPerGallon(travelExpEntity.getMilesPerGallon()).kwhCost(travelExpEntity.getKwhCost())
				.kwhCostStr(travelExpEntity.getKwhCost() == null ? ""
						: currencyLocalisation(travelExpEntity.getKwhCost().doubleValue()))
				.kwhCapacity(travelExpEntity.getKwhCapacity()).rangeOfCar(travelExpEntity.getRangeOfCar())
				.taxiProvidersList(getProviderData(travelExpEntity.getTravelProviders())).build();
	}

	public static List<TaxiProviderVO> getProviderData(List<DSAApplicationTravelProvider> providers) {

		List<TaxiProviderVO> list = new ArrayList<>();
		if (providers != null && !providers.isEmpty()) {
			list = providers.stream().map(AllowancesServiceUtil::entityToTravelProviderVO).collect(Collectors.toList());
		}
		return list;

	}

	public static TaxiProviderVO entityToTravelProviderVO(DSAApplicationTravelProvider provider) {
		return TaxiProviderVO.builder().id(provider.getId()).approvedContractor(provider.getIsApprovedContract())
				.recommendedProvider(provider.getProviderName())
				.costStr(currencyLocalisation(provider.getCost().doubleValue())).cost(populateCost(provider.getCost()))
				.build();
	}

	public static List<AccommodationVO> getAccommodations(List<DSAApplicationStudAccommodation> list) {
		List<AccommodationVO> accommodations = new ArrayList<>();
		if (list != null) {
			accommodations = list.stream()
					.sorted(Collections
							.reverseOrder(Comparator.comparing(DSAApplicationStudAccommodation::getLastUpdatedDate)))
					.filter(t -> t.getWeeks() != 0 && t.getEnhancedCost() != null && t.getStandardCost() != null)
					.map(AllowancesServiceUtil::toAccommodationVO).collect(Collectors.toList());
		}
		return accommodations;
	}

	public static AccommodationVO toAccommodationVO(DSAApplicationStudAccommodation t) {
		AccommodationVO vo = new AccommodationVO();
		vo.setId(t.getId());
		vo.setDsaApplicationNumber(t.getDsaApplicationNumber());
		vo.setStudentReferenceNumber(t.getStudentReferenceNumber());
		vo.setAccommodationType(t.getAccommodationType());
		if (t.getEnhancedCost() != null) {
			vo.setEnhancedCost(populateCost(t.getEnhancedCost()));
			vo.setEnhancedCostStr(currencyLocalisation(t.getEnhancedCost().doubleValue()));
		}
		if (t.getStandardCost() != null) {
			vo.setStandardCost(populateCost(t.getStandardCost()));
			vo.setStandardCostStr(currencyLocalisation(t.getStandardCost().doubleValue()));
		}
		vo.setWeeks(t.getWeeks());
		return vo;
	}
}
