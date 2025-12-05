package uk.gov.saas.dsa.service.award;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.equipmentsTotal;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatValue;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.getConsumablesTotal;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.nmphTotal;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DSAAwardPDF;
import uk.gov.saas.dsa.domain.DSAQuotePDF;
import uk.gov.saas.dsa.domain.readonly.DSAAwardAccommData;
import uk.gov.saas.dsa.domain.readonly.DSAAwardItemisedData;
import uk.gov.saas.dsa.domain.readonly.DSAAwardNMPHData;
import uk.gov.saas.dsa.domain.readonly.DSAAwardNotice;
import uk.gov.saas.dsa.domain.readonly.DSAAwardTravelData;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.AccommodationType;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.model.DSAAwardProcessedStatus;
import uk.gov.saas.dsa.persistence.DSAAwardPDFPDFRepository;
import uk.gov.saas.dsa.persistence.DsaQuotePDFRepository;
import uk.gov.saas.dsa.persistence.readonly.DSAAwardNoticeRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.service.StudentDetailsService;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.DashboardFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.award.DSAAwardDetailsVO;
import uk.gov.saas.dsa.vo.award.DSAAwardVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAwardAllowanceVO;

@Service
public class DSAAwardService {
	private static final String QUOTE = "Quote";
	private static final String LINE_ITEM = "Line Item";
	private static final String CONSUMABLES = "Consumables";
	private static final String EQUIPMENTS = "Equipment, Software and Accessories";
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final StudentDetailsService studentDetailsService;
	private final CourseDetailsService courseDetailsService;
	private final ApplicationService applicationService;
	private DSAAwardPDFPDFRepository awardPDFRepository;
	private DSAAwardNoticeRepository dsaAwardNoticeRepository;
	private DsaQuotePDFRepository dsaQuotePDFRepository;

	public DSAAwardService(ConfigDataService configDataService, StudentDetailsService studentDetailsService,
			CourseDetailsService courseDetailsService, DSAAwardPDFPDFRepository awardPDFRepository,
			DSAAwardNoticeRepository dsaAwardNoticeRepository, DsaQuotePDFRepository dsaQuotePDFRepository,
			ApplicationService applicationService) {
		this.studentDetailsService = studentDetailsService;
		this.courseDetailsService = courseDetailsService;
		this.awardPDFRepository = awardPDFRepository;
		this.dsaAwardNoticeRepository = dsaAwardNoticeRepository;
		this.dsaQuotePDFRepository = dsaQuotePDFRepository;
		this.applicationService = applicationService;
	}

	public Blob getAwardPDF(long studentReferenceNumber, int sessionCode) {

		DSAAwardPDF applicationPDF = awardPDFRepository
				.findByStudentReferenceNumberAndSessionCode(studentReferenceNumber, sessionCode);
		return applicationPDF.getAwardPDF();
	}

	public Blob getQuote(long quoteId) throws IllegalAccessException {

		DSAQuotePDF quotePDF = dsaQuotePDFRepository.findQuoteByQuoteId(quoteId);
		if (quotePDF != null) {
			return quotePDF.getQuote();
		} else {
			throw new IllegalAccessException("No Quote found for id: " + quoteId);
		}
	}

	public DSAAwardVO getAwardDataFromSteps(DashboardFormVO dashboardFormVO, long studentReferenceNumber) {

		DSAApplicationsMade dsaApplication = applicationService.findByDsaApplicationNumberAndStudentReferenceNumber(
				dashboardFormVO.getDsaApplicationNumber(), studentReferenceNumber);

		DSAAwardVO dsaAwardVO = new DSAAwardVO();
		DSAAwardNotice awardNotice = getAwardNotice(studentReferenceNumber, dsaApplication.getSessionCode());
		logger.info("awardNotice {}", awardNotice);

		if (awardNotice != null) {
			dsaAwardVO.setAwardedInPhaseOne(false);
			DSAAwardProcessedStatus fundStatus = getfundStatus(awardNotice.getAwardNoticeStatus().toUpperCase());
			dsaAwardVO.setFundStatus(fundStatus);
			dsaAwardVO.setCurrentSession(dashboardFormVO.getSessionCode());
			dsaAwardVO.setAwardDate(getAwardDate(awardNotice.getPublishedDate()));
		} else {
			dsaAwardVO.setAwardedInPhaseOne(true);
		}
		logger.info("DSAAwardVO {}", dsaAwardVO);
		return dsaAwardVO;
	}

	public DSAAwardDetailsVO getAwardDetails(long studentReferenceNumber, DashboardFormVO dashboardFormVO)
			throws Exception {
		DSAAwardDetailsVO dsaAwardDetailsVO = new DSAAwardDetailsVO();

		DSAAwardNotice awardNotice = getAwardNotice(studentReferenceNumber, dashboardFormVO.getSessionCode());
		logger.info("awardNotice {}", awardNotice);
		if (awardNotice != null) {

			DSAAwardProcessedStatus fundStatus = getfundStatus(awardNotice.getAwardNoticeStatus().toUpperCase());

			dsaAwardDetailsVO.fundStatus(fundStatus).dsaApplicationNumber(awardNotice.getDsaApplicationId())
					.studentReferenceNumber(studentReferenceNumber).currentSession(awardNotice.getSessionCode())
					.accomTotal(awardNotice.getAccomTotal()).awardDate(getAwardDate(awardNotice.getPublishedDate()));

			setStudentDetails(dsaAwardDetailsVO, dashboardFormVO);
			dsaAwardDetailsVO.academicYear(dashboardFormVO.getAcademicYear());
			setConsumablesData(dsaAwardDetailsVO, awardNotice.getItemisedDataElements());

			setEquipmentsData(dsaAwardDetailsVO, awardNotice.getItemisedDataElements());
			setTravelExpData(dsaAwardDetailsVO, awardNotice.getTravelDataElements());

			setNMPHData(awardNotice.getNmphDataElements(), dsaAwardDetailsVO);

			setAccommodationData(dsaAwardDetailsVO, awardNotice.getAccommodations());
		}
		return dsaAwardDetailsVO;
	}

	private void setAccommodationData(DSAAwardDetailsVO dsaAwardDetailsVO, List<DSAAwardAccommData> accommodations) {

		List<AccommodationVO> list = new ArrayList<AccommodationVO>();
		accommodations.forEach(item -> {

			AccommodationVO vo = new AccommodationVO();
			AccommodationType[] values = AccommodationType.values();

			Optional<AccommodationType> typeItem = Arrays.asList(values).stream()
					.filter(t -> t.getStepsDescription().equalsIgnoreCase(item.getAccommType())).findFirst();
			if (typeItem.isPresent()) {
				AccommodationType accommodationType = typeItem.get();
				vo.setAccommodationType(accommodationType);
			} else {
				throw new IllegalArgumentException(
						String.format("Invalid type returned from STEPS %s where as DSA allowed types are %s ",
								item.getAccommType(), values));
			}
			vo.setWeeks(item.getWeeks());
			vo.setEnhancedCostStr(currencyLocalisation(item.getEnhancedCost().doubleValue()));
			vo.setStandardCostStr(currencyLocalisation(item.getStandardCost().doubleValue()));
			list.add(vo);

		});
		dsaAwardDetailsVO.accommodations(list);
	}

	private void setEquipmentsData(DSAAwardDetailsVO dsaAwardDetailsVO,
			List<DSAAwardItemisedData> itemisedDataElements) {
		List<DSAAwardItemisedData> stepsEquipments = filteritemisedData(itemisedDataElements, EQUIPMENTS);
		List<EquipmentAllowanceVO> equipments = new ArrayList<EquipmentAllowanceVO>();
		for (DSAAwardItemisedData item : stepsEquipments) {

			EquipmentAllowanceVO vo = EquipmentAllowanceVO.builder().cost(formatValue(item.getProductCost(), 2))
					.costStr(currencyLocalisation(item.getProductCost().doubleValue()))
					.dsaApplicationNumber(dsaAwardDetailsVO.dsaApplicationNumber())
					.studentReferenceNumber(dsaAwardDetailsVO.studentReferenceNumber())
					.productName(item.getProductName()).description(item.getProductDescription())
					.itemType(item.getItemType()).paytoHEI(item.getPayToHEI()).build();
			equipments.add(vo);
		}
		List<EquipmentAllowanceVO> quotes = getQuotesData(equipments);

		List<EquipmentAllowanceVO> lineItems = filterEquipments(LINE_ITEM, equipments);

		dsaAwardDetailsVO.equipments(equipments);
		dsaAwardDetailsVO.lineItemEquipments(lineItems);
		dsaAwardDetailsVO.quoteEquipments(quotes);

		Double total = equipmentsTotal(equipments);
		String equipmentsTotal = currencyLocalisation(total);
		dsaAwardDetailsVO.equipmentsTotal(equipmentsTotal);
		setPaymentForHEI(equipments, dsaAwardDetailsVO);
	}

	private void setPaymentForHEI(List<EquipmentAllowanceVO> equipments, DSAAwardDetailsVO dsaAwardDetailsVO) {
		int allNullValuesCount = equipments.stream().filter(t -> (t.getPaytoHEI() == null)).collect(Collectors.toList())
				.size();
		if (equipments.size() != allNullValuesCount) {

			boolean payToHEI = equipments.stream()
					.map(t -> (t.getPaytoHEI() != null && t.getPaytoHEI().equalsIgnoreCase(YesNoType.YES.getDbValue())))
					.findFirst().get();
			YesNoType type = payToHEI ? YesNoType.YES : YesNoType.NO;
			dsaAwardDetailsVO.paymentToHEI(type.getDbValue());
		}
	}

	private List<EquipmentAllowanceVO> getQuotesData(List<EquipmentAllowanceVO> equipments) {
		List<EquipmentAllowanceVO> quotesInSteps = filterEquipments(QUOTE, equipments);

		List<EquipmentAllowanceVO> filteredQuotes = filterQuotesWithAmount(quotesInSteps);
		return filteredQuotes;
	}

	private List<EquipmentAllowanceVO> filterQuotesWithAmount(List<EquipmentAllowanceVO> quotesInSteps) {
		List<EquipmentAllowanceVO> filteredQuotes = new ArrayList<EquipmentAllowanceVO>();

		if (quotesInSteps.size() > 0) {
			List<DSAQuotePDF> quotesInDSA = dsaQuotePDFRepository
					.findQuotesByStudentRefNumber(quotesInSteps.get(0).getStudentReferenceNumber());
			for (EquipmentAllowanceVO vo : quotesInSteps) {

				Optional<DSAQuotePDF> supplierAndCost = quotesInDSA.stream().filter(t -> {
					String supplierInDSA = t.getSupplier();
					String supplierInSteps = vo.getProductName();

					double costInDSA = t.getQuoteCost().doubleValue();
					double costInSteps = vo.getCost().doubleValue();

					return supplierInDSA.equalsIgnoreCase(supplierInSteps) && costInDSA == costInSteps;
				}).findFirst();

				if (supplierAndCost.isPresent()) {
					DSAQuotePDF dsaQuotePDF = supplierAndCost.get();
					logger.info("supplier And Cost matched {}", vo);
					EquipmentAllowanceVO allowanceVO = EquipmentAllowanceVO.builder().cost(vo.getCost())
							.costStr(vo.getCostStr()).productName(vo.getProductName()).description(vo.getDescription())
							.itemType(vo.getItemType()).dsaApplicationNumber(vo.getDsaApplicationNumber())
							.studentReferenceNumber(vo.getStudentReferenceNumber()).dsaQuoteId(dsaQuotePDF.getQuoteId())
							.build();
					filteredQuotes.add(allowanceVO);
				} else {
					logger.error("supplier And Cost NOT matched {}", vo);
				}

			}
		}

		return filteredQuotes;
	}

	private List<EquipmentAllowanceVO> filterEquipments(String string, List<EquipmentAllowanceVO> equipments) {

		return equipments.stream().filter(t -> t.getItemType().equalsIgnoreCase(string)).collect(Collectors.toList());
	}

	private void setConsumablesData(DSAAwardDetailsVO dsaAwardDetailsVO,
			List<DSAAwardItemisedData> itemisedDataElements) {

		List<DSAAwardItemisedData> consumables = filteritemisedData(itemisedDataElements, CONSUMABLES);
		List<ConsumableTypeVO> consumableItems = new ArrayList<ConsumableTypeVO>();

		for (DSAAwardItemisedData stepsConsumable : consumables) {
			ConsumableTypeVO typeVO = new ConsumableTypeVO();
			String productName = stepsConsumable.getProductName();

			Optional<ConsumableItem> item = Arrays.asList(ConsumableItem.values()).stream()
					.filter(t -> t.getItemName().equalsIgnoreCase(productName)).findFirst();
			if (!item.isEmpty()) {
				if (item.get().equals(ConsumableItem.OTHER)) {
					typeVO.setOtehrItemText(stepsConsumable.getProductDescription());
				}

				typeVO.setConsumableItem(item.get());
				typeVO.setCost(stepsConsumable.getProductCost());
				consumableItems.add(typeVO);
			} else {
				// All mismatch ConsumableItems from the steps will be treated as OTHER
				// ConsumableItem.
				typeVO.setConsumableItem(ConsumableItem.OTHER);
				typeVO.setOtehrItemText(productName);
				typeVO.setCost(stepsConsumable.getProductCost());
				consumableItems.add(typeVO);
			}
		}

		Double consumablesTotal = getConsumablesTotal(consumableItems);
		if (consumablesTotal != null && consumablesTotal > 0) {
			String consumablesTotalStr = currencyLocalisation(consumablesTotal);
			dsaAwardDetailsVO.consumableItemsTotal(consumablesTotalStr);
			dsaAwardDetailsVO.consumableItems(consumableItems);
		}

	}

	private List<DSAAwardItemisedData> filteritemisedData(List<DSAAwardItemisedData> itemisedDataElements,
			String type) {
		List<DSAAwardItemisedData> list = new ArrayList<DSAAwardItemisedData>();
		if (itemisedDataElements != null && !itemisedDataElements.isEmpty()) {
			list = itemisedDataElements.stream().filter(t -> t.getType().equalsIgnoreCase(type))
					.collect(Collectors.toList());

		}
		return list;
	}

	private void setTravelExpData(DSAAwardDetailsVO dsaAwardDetailsVO, List<DSAAwardTravelData> travelDataElements) {
		logger.info("travelDataElements {}", travelDataElements);
		if (travelDataElements != null && !travelDataElements.isEmpty()) {

			BigDecimal totalCost = BigDecimal.valueOf(0);
			List<TravelExpAwardAllowanceVO> travelAllowances = new ArrayList<TravelExpAwardAllowanceVO>();
			for (DSAAwardTravelData t : travelDataElements) {
				double doubleValue = t.getTravelCost().doubleValue();
				String travelExpTotal = currencyLocalisation(doubleValue);
				String travelType = t.getTravelType();
				TravelExpAwardAllowanceVO travelAllowanceVO = TravelExpAwardAllowanceVO.builder()
						.costStr(travelExpTotal).weeks(t.getTravelWeeks()).returnJourneys(t.getTravelJourneys())
						.transportType(travelType).startLocation(t.getStartPostcode()).endLocation(t.getEndPostcode())
						.maxAmount(t.getMaxAmount().doubleValue())
						.maxAmountStr(currencyLocalisation(t.getMaxAmount().doubleValue())).cost(doubleValue).build();
				totalCost = totalCost.add(t.getMaxAmount());
				travelAllowances.add(travelAllowanceVO);
			}
			if (totalCost != null && totalCost.doubleValue() > 0) {
				dsaAwardDetailsVO.travelExpItems(travelAllowances);
				String travelExpTotal = currencyLocalisation(totalCost.doubleValue());
				dsaAwardDetailsVO.travelExpTotal(travelExpTotal);
				dsaAwardDetailsVO.travelExpPartTotal(currencyLocalisation(totalCost.doubleValue() / 2));
				dsaAwardDetailsVO.travelExpItems(travelAllowances);

				double otherTravelSum = travelAllowances.stream()
						.filter(t -> t.getTransportType().equalsIgnoreCase("Other (DSA Travel)"))
						.mapToDouble(value -> value.getMaxAmount()).sum();
				double taxiTravelSum = travelAllowances.stream()
						.filter(t -> t.getTransportType().equalsIgnoreCase("taxi"))
						.mapToDouble(value -> value.getMaxAmount()).sum();
				if (otherTravelSum > 0) {
					String otherExpPart = currencyLocalisation(otherTravelSum / 2);
					dsaAwardDetailsVO.otherTravelExpText(String.format(
							"If you are claiming for own car or lift, we will pay this in 2 instalments. The first payment %s will be paid into your bank account in the next 3 to 5 working days (if your course has already started), otherwise you will receive the payment 2 weeks of your course start date. The second payment %s will be made at the beginning of January after your course starts.",
							otherExpPart, otherExpPart));
				}
				if (taxiTravelSum > 0) {
					dsaAwardDetailsVO.taxiTravelExpText(
							"If using taxis, we will pay this directly to the taxi provider when we receive an invoice, or we will pay you directly within 21 days of you sending us taxi receipts using the Document Uploader in your SAAS Account.");
				}

			}
		}
	}

	private void setStudentDetails(DSAAwardDetailsVO dsaAwardDetailsVO, DashboardFormVO dashboardFormVO)
			throws Exception {

		StudentResultVO studentResultVO = studentDetailsService.findStudentByStudRefAndSessionCode(
				dsaAwardDetailsVO.studentReferenceNumber(), dashboardFormVO.getSessionCode());
		CourseDetailsVO courseDetailsVO = courseDetailsService
				.findCourseDetailsFromDB(dsaAwardDetailsVO.studentReferenceNumber(), dashboardFormVO.getSessionCode());

		dsaAwardDetailsVO.studentFirstName(studentResultVO.getFirstName())
				.studentFullName(studentResultVO.getFirstName() + " " + studentResultVO.getLastName())
				.institution(courseDetailsVO.getInstitutionName()).academicYear(dashboardFormVO.getAcademicYear());
	}

	private void setNMPHData(List<DSAAwardNMPHData> nmphElements, DSAAwardDetailsVO dsaAwardDetailsVO) {
		List<NMPHAllowanceVO> nmphItems = new ArrayList<NMPHAllowanceVO>();
		List<DSAAwardNMPHData> list = nmphElements.stream().filter(
				t -> t.getProvider() != null && t.getHourleyRate() != null && t.getHours() >= 0 && t.getWeeks() >= 0)
				.collect(Collectors.toList());
		if (!list.isEmpty()) {
			list.forEach(dsaAllowance -> {

				BigDecimal nmphHourlyRate = dsaAllowance.getHourleyRate();
				int nmphHours = dsaAllowance.getHours();
				int nmphWeeks = dsaAllowance.getWeeks();

				BigDecimal costValue = nmphHourlyRate.multiply(BigDecimal.valueOf(nmphHours * nmphWeeks));
				BigDecimal formattedCost = formatValue(costValue, 2);

				NMPHAllowanceVO v = NMPHAllowanceVO.builder().cost(formattedCost)
						.costStr(currencyLocalisation(formattedCost.doubleValue())).hours(nmphHours).weeks(nmphWeeks)
						.recommendedProvider(dsaAllowance.getProvider()).typeOfSupport(dsaAllowance.getTypeOfSupport())
						.hourlyRate(formatValue(dsaAllowance.getHourleyRate(), 2))
						.hourlyRateStr(currencyLocalisation(dsaAllowance.getHourleyRate().doubleValue())).build();
				nmphItems.add(v);
			});

			Double total = nmphTotal(nmphItems);
			if (total != null && total > 0) {
				String nmphTotal = currencyLocalisation(total);
				dsaAwardDetailsVO.nmphTotal(nmphTotal);
				dsaAwardDetailsVO.nmphItems(nmphItems);
			}
		}
	}

	private DSAAwardProcessedStatus getfundStatus(String awardNoticeStatus) {
		DSAAwardProcessedStatus[] values = DSAAwardProcessedStatus.values();
		Optional<DSAAwardProcessedStatus> statusItem = Arrays.asList(values).stream()
				.filter(t -> t.getCode().toUpperCase().equals(awardNoticeStatus.toUpperCase())).findFirst();
		return statusItem.get();
	}

	private DSAAwardNotice getAwardNotice(long studentRefNo, int sessionCode) {
		return dsaAwardNoticeRepository.findByStudentReferenceNumberAndSessionCode(studentRefNo, sessionCode);
	}

	public String getAwardFundStatus(long studentRefNo, int sessionCode) {
		String status  = "UNKNOWN";
		DSAAwardNotice awardNotice = getAwardNotice(studentRefNo, sessionCode);
		if (awardNotice == null) {
			logger.error("no entry in DSA_AWARD_NOTICE for studentRefNo {} , sessionCode {}", studentRefNo, sessionCode);
			logger.error("Setting hardcoded status as UNKNOWN!");
		} else {
			status = getfundStatus(awardNotice.getAwardNoticeStatus().toUpperCase()).getDescription().toLowerCase();
		}
		return status;
	}

	private String getAwardDate(Date publishedDate) {
		String dateStr = null;
		if (publishedDate != null) {
			dateStr = ServiceUtil.formatDate(publishedDate);
		}
		return dateStr;
	}
}
