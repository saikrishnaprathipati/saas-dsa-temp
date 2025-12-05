package uk.gov.saas.dsa.web.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.service.*;
import uk.gov.saas.dsa.service.allowances.*;
import uk.gov.saas.dsa.vo.ApplicationSectiponStatusVO;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemChangeFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.AddEquipmentFormVO;
import uk.gov.saas.dsa.vo.equipment.AddEquipmentPaymentFormVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.AddNMPHFormVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;
import uk.gov.saas.dsa.web.controller.AdditionalInfoController;
import uk.gov.saas.dsa.web.controller.allowances.EquipmentController;
import uk.gov.saas.dsa.web.controller.allowances.NMPHController;
import uk.gov.saas.dsa.web.controller.declaration.AdvisorDeclarationsController;
import uk.gov.saas.dsa.web.controller.declaration.AwardAccessController;
import uk.gov.saas.dsa.web.controller.declaration.BankDetailsController;
import uk.gov.saas.dsa.web.controller.declaration.StudentDeclarationsController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static uk.gov.saas.dsa.domain.refdata.YesNoType.NO;
import static uk.gov.saas.dsa.domain.refdata.YesNoType.YES;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.matches;

/**
 * Consumables Helper class
 */
public class AllowancesHelper {
	private static final String ACCOMMODATION_COUNT = "accommodationCount";
	public static final String ACCOMMODATIONS = "accommodations";
	public static final String ACCOMMODATIONS_TOTAL = "accommodationsTotal";
	private static final String SHOW_PAYMENT_FOR_SECTION = "SHOW_PAYMENT_FOR_SECTION";
	public static final String POST_CODE_REGEX = "^$|^((([A-Za-z]{1,2}[0-9]{1,2})|([A-Za-z]{1,2}[0-9][A-Za-z])))([ ]?[0-9][A-Za-z]{1,2})$";
	private static final String TRAVEL_EXP_ITEMS = "travelExpItems";
	private static final int NMPH_CAP = 20520;
	private static final int CONSUMABLE_CAP = 1725;
	private static final int EQUIPMENT_CAP = 5160;
	private static final double ZERO = 0;
	private static final int ONE = 1;
	private static final int TWO = 2;
	private static final Set<Character> SPECIAL_CHARACTERS = Set.of('\\', '/', ':', '*', '?', '<', '>', ' ', '|', '"', '\'');
	private static final String REGEX_PERIOD = "[.]+";
	public static final String PAGE_NUMBERS = "pageNumbers";

	public static final String CONSUMABLES_TOTAL = "consumablesTotal";
	public static final String CONSUMABLES_COUNT = "consumablesCount";
	public static final String CONSUMABLE_ITEMS = "consumableItems";
	public static final String HIDE_ADD_ANOTHER = "hideAddAnother";
	public static final String CONSUMABLES_TOTAL_EXCEEDED = "consumablesTotalExceeded";

	public static final String NMPH_TOTAL = "nmphTotal";
	public static final String NMPH_COUNT = "nmphCount";
	public static final String NMPH_ITEMS = "nmphItems";
	public static final String NMPH_TOTAL_EXCEEDED = "nmphTotalExceeded";

	public static final String EQUIPMENT_TOTAL = "equipmentTotal";
	public static final String QUOTE_TOTAL = "quoteTotal";
	public static final String GRAND_TOTAL = "grandTotal";
	public static final String QUOTE_COUNT = "quoteCount";
	public static final String SHOW_ADD_EQUIPMENT_LINK = "showAddEquipmentLink";
	public static final String SHOW_ADD_QUOTE_LINK = "showAddQuoteLink";
	public static final String EQUIPMENT_COUNT = "equipmentCount";
	public static final String EQUIPMENT_ITEMS = "equipmentItems";
	public static final String EQUIPMENT_TOTAL_EXCEEDED = "equipmentTotalExceeded";
	public static final String EQUIPMENT_PAGE = "equipmentPage";
	public static final String EQUIPMENT_CURRENT_PAGE_NUMBER = "equipmentCurrentPageNumber";

	private static final Logger logger = LogManager.getLogger(FindStudentHelper.class);

	public static void setConsumablesSummaryData(Model model, ConsumablesService consumablesService,
												 ConfigDataService configDataService, long dsaApplicationNumber) {
		logger.info("Setting the consumable summary data");
		List<ConsumableTypeVO> consumableItems = consumablesService.getAllConsumableItems(dsaApplicationNumber);

		Double total = getConsumablesTotal(consumableItems);
		model.addAttribute(CONSUMABLES_TOTAL, currencyLocalisation(total));
		model.addAttribute(CONSUMABLES_COUNT, consumableItems.size());
		model.addAttribute(CONSUMABLE_ITEMS, consumableItems);
		model.addAttribute(HIDE_ADD_ANOTHER, consumableItems.size() == ConsumableItem.values().length);
		Double consumableCap = getConsumableCap(configDataService);
		model.addAttribute(DSAConstants.CONSUMABLES_CAP, AllowancesHelper.currencyLocalisation(consumableCap));

		model.addAttribute(CONSUMABLES_TOTAL_EXCEEDED, (total.compareTo(consumableCap) > 0));
	}

	public static Double getConsumablesTotal(List<ConsumableTypeVO> consumableItems) {
		return consumableItems.stream().map(ConsumableTypeVO::getCost).mapToDouble(BigDecimal::doubleValue).sum();
	}

	public static void setEquipmentSummaryData(Model model, EquipmentService equipmentService,
											   ConfigDataService configDataService, QuoteUploadService quoteUploadService, long dsaApplicationNumber,
											   int currentPage, int pageSize) {
		logger.info("Setting the Equipment summary data {}", dsaApplicationNumber);
		List<EquipmentAllowanceVO> equipmentItems = equipmentService.getAllEquipmentAllowances(dsaApplicationNumber);
		Double equipmentTotal = equipmentsTotal(equipmentItems);

		List<QuoteResultVO> quotes = quoteUploadService.fetchAllQuotesForStudentApplication(dsaApplicationNumber);
		model.addAttribute("quotes", quotes);

		Double quoteTotal = quotesTotal(quotes);
		model.addAttribute(QUOTE_TOTAL, currencyLocalisation(quoteTotal));

		Double grandTotal = Double.sum(quoteTotal, equipmentTotal);
		model.addAttribute(GRAND_TOTAL, currencyLocalisation(grandTotal));

		model.addAttribute(EQUIPMENT_TOTAL, currencyLocalisation(equipmentTotal));
		model.addAttribute(EQUIPMENT_COUNT, equipmentItems.size());
		model.addAttribute(EQUIPMENT_ITEMS, equipmentItems);
		Double equipmentCap = getEquipmentCap(configDataService);
		model.addAttribute(DSAConstants.EQUIPMENT_CAP, AllowancesHelper.currencyLocalisation(equipmentCap));
		model.addAttribute(EQUIPMENT_TOTAL_EXCEEDED, (grandTotal.compareTo(equipmentCap) > 0));
		model.addAttribute(SHOW_ADD_EQUIPMENT_LINK, EQUIPMENT_ITEMS_LIMIT > equipmentItems.size() ? "Y" : "N");
		logger.info("Setting the Equipment summary data {}", equipmentItems);

		// Pagination mayhem
		Page<EquipmentAllowanceVO> equipmentPage = equipmentService
				.findPaginated(PageRequest.of(currentPage - 1, pageSize), equipmentItems);
		model.addAttribute(EQUIPMENT_PAGE, equipmentPage);
		model.addAttribute(EQUIPMENT_CURRENT_PAGE_NUMBER, currentPage);

		if (equipmentPage != null) {
			int totalPages = equipmentPage.getTotalPages();
			if (totalPages > 0) {
				List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

				// Mark -1 the pageNumbers that will show as an Ellipsis
				pageNumbers.forEach((pageNumber) -> {
					if (!(pageNumber == 1 || pageNumber == currentPage - 1 || pageNumber == currentPage + 1
							|| pageNumber == currentPage || pageNumber == equipmentPage.getTotalPages())) {
						pageNumbers.set(pageNumber - 1, -1);
					}
				});

				// i.e. from [1,-1,-1,-1,5,6,-1,8] to [1,-1,5,6,-1,8]
				List<Integer> finalPageNumbers = IntStream.range(0, pageNumbers.size())
						.filter(i -> ((i < pageNumbers.size() - 1 && !pageNumbers.get(i).equals(pageNumbers.get(i + 1)))
								|| i == pageNumbers.size() - 1))
						.mapToObj(pageNumbers::get).collect(Collectors.toList());

				model.addAttribute(PAGE_NUMBERS, finalPageNumbers);
			}
		}
	}

	public static void setQuoteSummaryData(Model model, QuoteUploadService quoteUploadService,
										   QuoteDetailsFormVO quoteDetailsFormVO, int currentPage, int pageSize, ConfigDataService configDataService) {
		logger.info("Setting the Quote summary data {}", quoteDetailsFormVO);

		List<QuoteResultVO> quotes = quoteUploadService
				.fetchAllQuotesForStudentApplication(quoteDetailsFormVO.getDsaApplicationNumber());
		model.addAttribute("quoteDetailsFormVO", quoteDetailsFormVO);
		model.addAttribute("quotes", quotes);

		Double quoteTotal = quotesTotal(quotes);
		model.addAttribute(QUOTE_TOTAL, currencyLocalisation(quoteTotal));

		String value = (String) model.getAttribute(EQUIPMENT_TOTAL);
		Double equipmentTotal = currencyDeLocalisation(value);

		Double grandTotal = Double.sum(quoteTotal, equipmentTotal);
		model.addAttribute(GRAND_TOTAL, currencyLocalisation(grandTotal));
		model.addAttribute(QUOTE_COUNT, quotes.size());
		model.addAttribute(STUDENT_FIRST_NAME, quoteDetailsFormVO.getFirstName());
		model.addAttribute(SHOW_ADD_QUOTE_LINK, QUOTE_ITEMS_LIMIT > quotes.size() ? "Y" : "N");

		Double equipmentCap = getEquipmentCap(configDataService);
		model.addAttribute(DSAConstants.EQUIPMENT_CAP, currencyLocalisation(equipmentCap));
		model.addAttribute(EQUIPMENT_TOTAL_EXCEEDED, (grandTotal.compareTo(equipmentCap) > 0));
	}

	public static Double equipmentsTotal(List<EquipmentAllowanceVO> equipmentItems) {
		return equipmentItems.stream().map(EquipmentAllowanceVO::getCost).mapToDouble(BigDecimal::doubleValue).sum();
	}

	public static Double quotesTotal(List<QuoteResultVO> quoteItems) {
		return quoteItems.stream().map(QuoteResultVO::getCost).mapToDouble(BigDecimal::doubleValue).sum();
	}

	public static void setNMPHSummaryData(Model model, NMPHAllowancesService nmphService,
										  ConfigDataService configDataService, long dsaApplicationNumber) {
		logger.info("Setting the NMPH summary data");
		List<NMPHAllowanceVO> nmphItems = nmphService.getAllNMPHAllowances(dsaApplicationNumber);

		Double total = nmphTotal(nmphItems);
		model.addAttribute(NMPH_TOTAL, currencyLocalisation(total));
		model.addAttribute(NMPH_COUNT, nmphItems.size());
		model.addAttribute(NMPH_ITEMS, nmphItems);
		Double nmphCap = getNMPHCap(configDataService);
		model.addAttribute(DSAConstants.NMPH_CAP, AllowancesHelper.currencyLocalisation(nmphCap));

		model.addAttribute(NMPH_TOTAL_EXCEEDED, (total.compareTo(nmphCap) > 0));
	}

	public static Double nmphTotal(List<NMPHAllowanceVO> nmphItems) {
		return nmphItems.stream().map(NMPHAllowanceVO::getCost).mapToDouble(BigDecimal::doubleValue).sum();
	}

	public static void setTravelExpSummaryData(Model model, TravelExpAllowancesService travelExpService,
											   long dsaApplicationNumber) {
		logger.info("Setting Travel Expenses summary data");
		List<TravelExpAllowanceVO> travelExpenses = travelExpService.getTravelExpAllowances(dsaApplicationNumber);

		model.addAttribute(TRAVEL_EXP_ITEMS, travelExpenses);
		boolean hasAllTypes = new HashSet<>(
				travelExpenses.stream().map(TravelExpAllowanceVO::getTravelExpType).collect(Collectors.toList()))
				.containsAll(Arrays.asList(TravelExpType.values()));
		model.addAttribute("HIDE_ADD_TRAVEL_EXP_LINK", hasAllTypes);
	}

	public static void setAccommodationSummaryData(Model model, AccommodationService accommodationService,
												   long dsaApplicationNumber) {
		List<AccommodationVO> accommodations = accommodationService.getAccommodations(dsaApplicationNumber);
		model.addAttribute(ACCOMMODATIONS_TOTAL, accommodationTotalCost(accommodations));
		model.addAttribute(ACCOMMODATION_COUNT, accommodations.size());
		model.addAttribute(ACCOMMODATIONS, accommodations);

	}

	public static String accommodationTotalCost(List<AccommodationVO> accommodations) {
		return currencyLocalisation(accommodationTotal(accommodations));
	}

	private static Double accommodationTotal(List<AccommodationVO> accommodations) {
		return accommodations.stream().map(AllowancesHelper::calculateAccommodationTotal)
				.mapToDouble(BigDecimal::doubleValue).sum();

	}

	private static BigDecimal calculateAccommodationTotal(AccommodationVO accommodation) {
		BigDecimal total = accommodation.getEnhancedCost().subtract(accommodation.getStandardCost())
				.multiply(BigDecimal.valueOf(accommodation.getWeeks()));
		return total;
	}

	public static Double getConsumableCap(ConfigDataService configDataService) {
		// TODO we need to enable this after adding the config data for
		// DSA_CONSUMABLES_CAP in the steps
		// ConfigData configData =
		// configDataService.findByItemName("DSA_CONSUMABLES_CAP");
		return (double) CONSUMABLE_CAP;

	}

	public static Double getNMPHCap(ConfigDataService configDataService) {
		// TODO we need to enable this after adding the config data for
		// DSA_CONSUMABLES_CAP in the steps
		// ConfigData configData =
		// configDataService.findByItemName("DSA_CONSUMABLES_CAP");
		return (double) NMPH_CAP;

	}

	public static Double getEquipmentCap(ConfigDataService configDataService) {
		// TODO we need to enable this after adding the config data for
		// EQUIPMENT_CAP in the steps
		// ConfigData configData =
		// configDataService.findByItemName("DSA_EQUIPMENT_CAP");
		return (double) EQUIPMENT_CAP;

	}

	public static ConsumableItemChangeFormVO mapToFormData(ConsumableTypeVO consumableTypeVO) {
		ConsumableItemChangeFormVO consumableItemChangeFormVO = new ConsumableItemChangeFormVO();
		consumableItemChangeFormVO.setConsumableItem(consumableTypeVO.getConsumableItem());
		consumableItemChangeFormVO.setCost(consumableTypeVO.getCost().toString());
		if (consumableItemChangeFormVO.getConsumableItem().equals(ConsumableItem.OTHER)) {
			consumableItemChangeFormVO.setDescription(consumableTypeVO.getOtehrItemText());
		}
		consumableItemChangeFormVO.setId(consumableTypeVO.getId());
		return consumableItemChangeFormVO;
	}

	public static boolean hasMandatoryValues(Model model, long dsaApplicationNumber, long studentReferenceNumber)
			throws IllegalAccessException {
		boolean hasMandatoryValues = (dsaApplicationNumber > 0 && studentReferenceNumber > 0);
		if (hasMandatoryValues) {
			model.addAttribute(DSA_APPLICATION_NUMBER, dsaApplicationNumber);
			model.addAttribute(STUDENT_REFERENCE_NUMBER, studentReferenceNumber);
		} else {
			String errorMessage = String.format(
					"Missing ID's in the request: DSA number is {%d}, and STUD Ref number is {%d}",
					dsaApplicationNumber, studentReferenceNumber);
			model.addAttribute(ERROR_MESSAGE, errorMessage);
			throw new IllegalAccessException(errorMessage);
		}
		return true;
	}

	public static Model addErrorMessage(Model model, String action, HttpServletRequest request) {
		return model.addAttribute(ERROR_MESSAGE,
				String.format("Calling the {%s} service with Unknown action: {%s}", request.getContextPath(), action));
	}

	public static String sanitizeCost(BindingResult bindingResult, MessageSource messageSource, ConsumableItem item,
									  String costStr, String pattern, String field, String errorCode) {
		return validateNumber(bindingResult, item.getItemName(), costStr, field, errorCode, NumberType.COST_XXXXX_YY,
				pattern);
	}

	public static String validateNumber(BindingResult bindingResult, String displayName, String value, String field,
										String errorCode, NumberType type, String pattern) {
		boolean isInvalidDecimalPoint = isInvalidDecimalPoint(value);

		Double doubleVal = ZERO;
		boolean containsLetter = value.matches(".*[a-zA-Z].*");
		if (!containsLetter) {
			try {
				doubleVal = Double.parseDouble(value.trim());
				logger.info("input value: {} converted to double Val {}", value, doubleVal);
				Double minVal = type.getMin();
				Double maxVal = type.getMax();

				boolean isScientificNotion = isScientificNotation(String.valueOf(doubleVal));
				boolean isNotInTheRange = doubleVal < minVal || doubleVal > maxVal;
				boolean isNotValidFormat = isInvalidDecimalPoint
						|| isPatternNotMatched(type, pattern, value, doubleVal, isScientificNotion);
				if (isNotValidFormat) {
					rejectWithInvalidValue(bindingResult, displayName, field, value, errorCode);
				} else if (isNotInTheRange) {
					rejectWithInvalidValueRange(bindingResult, displayName, value, field, minVal, maxVal,
							errorCode + RANGE);
				}
			} catch (Exception e) {
				rejectWithInvalidValue(bindingResult, displayName, field, value, errorCode);
			}
		} else {
			rejectWithInvalidValue(bindingResult, displayName, field, value, errorCode);
		}
		return formatCost(type, doubleVal);
	}

	private static boolean isPatternNotMatched(NumberType type, String pattern, String value, Double doubleVal,
											   boolean isScientificNotion) {
		boolean isMatched;
		if (Arrays.asList(NumberType.COST_X_YY, NumberType.COST_XXX_YY, NumberType.COST_XXXXX_YY).contains(type)
				&& !isScientificNotion) {
			isMatched = matches(Pattern.compile(pattern), doubleVal.toString());
		} else {
			isMatched = matches(Pattern.compile(pattern), value);
		}
		logger.info("type: {}, string value {}, doubleVal {} , pattern:{},  isMatched: {}", type, value, doubleVal,
				pattern, isMatched);
		return !isMatched;
	}

	public static void rejectWithInvalidValue(BindingResult bindingResult, String displayName, String field,
											  String value, String errorCode) {
		bindingResult.rejectValue(field, errorCode, new Object[]{displayName}, "");
		logger.info("Error added for - filed, {}, value {}, errorCode {}", field, value, errorCode);
	}

	private static void rejectWithInvalidValueRange(BindingResult bindingResult, String displayName, String value,
													String field, Double minVal, Double maxVal, String errorCode) {
		bindingResult.rejectValue(field, errorCode,

				new Object[]{localisedNumber(minVal), localisedNumber(maxVal), displayName}, "");
		logger.info("Error added for - filed, {}, value {}, errorCode {}, minVal {}, maxVal {}", field, value,
				errorCode, minVal, maxVal);

	}

	private static boolean isInvalidDecimalPoint(String value) {
		boolean invalidDecimalPoint = false;
		String[] split = value.split(REGEX_PERIOD);
		if (split.length == TWO) {
			String decimalPlaces = split[ONE];
			if (decimalPlaces.length() > TWO) {
				invalidDecimalPoint = true;
			}
		}
		return invalidDecimalPoint;
	}

	private static boolean isScientificNotation(String numberString) {
		try {
			new BigDecimal(numberString);
		} catch (NumberFormatException e) {
			return false;
		}

		return numberString.toUpperCase().contains("E")
				&& (numberString.charAt(1) == '.' || numberString.charAt(2) == '.');
	}

	private static String localisedNumber(Double value) {

		return numberLocalisation(value);
	}

	public static String currencyLocalisation(Double value) {

		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.UK);
		return nf.format(value);
	}

	public static Double currencyDeLocalisation(String value) {

		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.UK);
		try {
			return nf.parse(value).doubleValue();
		} catch (ParseException e) {
			logger.error(e);
		}
		return 0.0;
	}

	public static BigDecimal populateCost(BigDecimal cost) {
		return BigDecimal.valueOf(cost.doubleValue());
	}

	private static String numberLocalisation(Double value) {

		NumberFormat nf = NumberFormat.getInstance(Locale.UK);
		return nf.format(value);
	}

	public static String redirectToView(HttpServletRequest request, String uri) {
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + uri;
	}

	public static StudentResultVO setStudentDetailsInTheModel(Model model, long studentReferenceNumber,
															  FindStudentService findStudentService) throws Exception {
		StudentResultVO studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber);
		model.addAttribute(STUDENT_FIRST_NAME, studentResultVO.getFirstName());
		return studentResultVO;
	}

	public static NMPHAllowanceVO mapToNMPHAllowanceVO(AddNMPHFormVO formVO) {
		return NMPHAllowanceVO.builder().id(formVO.getId()).dsaApplicationNumber(formVO.getDsaApplicationNumber())
				.studentReferenceNumber(formVO.getStudentReferenceNumber()).typeOfSupport(formVO.getTypeOfSupport())
				.recommendedProvider(formVO.getRecommendedProvider())
				.hourlyRate(BigDecimal.valueOf(Double.parseDouble(formVO.getHourlyRate())))
				.hours(Integer.valueOf(formVO.getHours())).weeks(formVO.getWeeks())
				.cost(stringToBigDecimal(formVO.getCost())).build();
	}

	public static AddNMPHFormVO mapToNMPHAllowanceFormVo(NMPHAllowanceVO nmphVO) {
		AddNMPHFormVO formData = new AddNMPHFormVO();
		formData.setStudentReferenceNumber(nmphVO.getStudentReferenceNumber());
		formData.setDsaApplicationNumber(nmphVO.getDsaApplicationNumber());
		formData.setId(nmphVO.getId());
		formData.setTypeOfSupport(nmphVO.getTypeOfSupport());
		formData.setRecommendedProvider(nmphVO.getRecommendedProvider());
		formData.setHourlyRate(formatValue(nmphVO.getHourlyRate(), 2).toString());
		formData.setHours(nmphVO.getHours().toString());
		formData.setWeeks(nmphVO.getWeeks());
		formData.setCost(formatValue(nmphVO.getCost(), 2).toString());
		return formData;
	}

	public static EquipmentAllowanceVO mapToEquipmentAllowanceVO(AddEquipmentFormVO formVO) {
		return EquipmentAllowanceVO.builder().id(formVO.getId()).dsaApplicationNumber(formVO.getDsaApplicationNumber())
				.studentReferenceNumber(formVO.getStudentReferenceNumber()).productName(formVO.getProductName())
				.description(formVO.getDescription()).cost(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())))
				.build();
	}

	public static AddEquipmentFormVO mapToEquipmentAllowanceFormVO(EquipmentAllowanceVO equipmentVO) {
		AddEquipmentFormVO formData = new AddEquipmentFormVO();
		formData.setStudentReferenceNumber(equipmentVO.getStudentReferenceNumber());
		formData.setDsaApplicationNumber(equipmentVO.getDsaApplicationNumber());
		formData.setId(equipmentVO.getId());
		formData.setProductName(equipmentVO.getProductName());
		formData.setDescription(equipmentVO.getDescription());
		formData.setCost(formatValue(equipmentVO.getCost(), 2).toString());
		return formData;
	}

	public static String bigDecimalToString(BigDecimal val) {
		String cost = null;
		if (val != null) {
			cost = AllowancesHelper.formatValue(val, 2).toString();
		}
		return cost;

	}

	public static BigDecimal formatValue(BigDecimal dec, int precision) {
		String format = String.format(DSAConstants.COST_FORMAT, dec);

		return new BigDecimal(format);
	}

	public static boolean optionHasCorrectValue(String removeItem) {
		return StringUtils.hasText(removeItem) && ((YES.name().equals(removeItem)) || (NO.name().equals(removeItem)));
	}

	public static boolean chooseQuoteHasCorrectValue(String chooseQuote) {
		return StringUtils.hasText(chooseQuote)
				&& ((YES.name().equals(chooseQuote)) || (NO.name().equals(chooseQuote)));
	}

	public static String formatCost(NumberType type, Double value) {
		boolean isCost = Arrays.asList(NumberType.COST_XXXXX_YY, NumberType.COST_XXX_YY, NumberType.COST_X_YY)
				.contains(type);

		String valueStr = isCost ? String.format(DSAConstants.COST_FORMAT, value) : String.valueOf(value.intValue());
		logger.info("Double value {}, formatted value {}", value, valueStr);
		return valueStr;
	}

	public static void validatePostcode(BindingResult bindingResult, String postcode, String fieldName,
										String message) {
		if (!matches(Pattern.compile(POST_CODE_REGEX), postcode)) {
			bindingResult.rejectValue(fieldName, message);
		}
	}

	public static BigDecimal stringToBigDecimal(String costString) {
		return BigDecimal.valueOf(Double.parseDouble(costString));
	}

	public static String showConsumableSummary(HttpServletRequest request) {

		return AllowancesHelper.redirectToView(request, CONSUMABLES_SUMMARY_PATH);

	}

	public static String showAccommodationAllowanceDetails(HttpServletRequest request) {

		return AllowancesHelper.redirectToView(request, "accommodationAllowance");

	}

	public static String showAccommodationsSummary(HttpServletRequest request) {

		return AllowancesHelper.redirectToView(request, "accommodationSummary");

	}

	public static String showAccommodationTypeSelection(HttpServletRequest request) {

		return AllowancesHelper.redirectToView(request, "selectAccommodation");

	}

	public static String showCourseDetailsPage(HttpServletRequest request) {

		return redirectToView(request, "courseDetails");

	}

	public static String showDashboardPage(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.APPLICATION_DASHBOARD_PATH);

	}

	public static String showNMPHSummary(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.NMPH_SUMMARY_PATH);

	}

	public static String showAllowancesSummary(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.ALLOWANCES_SUMMARY_PATH);

	}

	public static String showAssessmentFeeSummary(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.ASSESSMENT_FEE_SUMMARY_PATH);

	}

	public static String initTravelExp(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.SELECT_TRAVEL_EXPENSE_PATH);

	}

	public static String initDisabilities(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.DISABILITY_DETAILS_PATH);

	}

	public static String disabilitiesSummary(HttpServletRequest request) {

		return redirectToView(request, DISABILITY_DETAILS_SUMMARY_PATH);

	}

	public static String showTravelExpSummary(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.TRAVEL_EXP_SUMMARY_PATH);
	}

	public static String showAddTravelExp(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.ADD_TRAVEL_EXPENSE_PATH);

	}

	public static String initEquipment(HttpServletRequest request) {

		return redirectToView(request, EquipmentController.ADD_EQUIPMENT_PATH);

	}

	public static String initChooseQuote(HttpServletRequest request) {

		return redirectToView(request, EquipmentController.CHOOSE_QUOTE_PATH);

	}

	public static String showEquipmentSummary(HttpServletRequest request) {

		return redirectToView(request, EquipmentController.EQUIPMENT_SUMMARY_PATH);

	}

	public static String initAddNMPH(HttpServletRequest request) {

		return redirectToView(request, NMPHController.ADD_NMPH_ALLOWANCE_PATH);

	}

	public static String showConsumablesInitialPage(HttpServletRequest request) {

		return redirectToView(request, DSAConstants.INIT_CONSUMABLES_PATH);

	}

	public static String showDeclrationsInitialPage(HttpServletRequest request) {

		return redirectToView(request, AdvisorDeclarationsController.DECLARATION_DETAILS_URI);

	}

	public static String showAdditionalInfoPage(HttpServletRequest request) {

		return redirectToView(request, AdditionalInfoController.ADD_ADDITIONAL_INFO_URI);

	}

	
	public static boolean noErrors(BindingResult bindingResult) {

		return !bindingResult.hasErrors();
	}

	public static boolean hasErrors(BindingResult bindingResult) {

		return bindingResult.hasErrors();
	}

	public static String showNeedsAssessmentFeeInitialPage(HttpServletRequest request) {

		return redirectToView(request, "addAssessmentFee");

	}

	public static String showStudentDeclarationPage(HttpServletRequest request) {

		return redirectToView(request, StudentDeclarationsController.STUDENT_DECLARATIONS_URI);

	}

	public static String showChooseBankAccountPage(HttpServletRequest request) {

		return redirectToView(request, StudentDeclarationsController.CHOOSE_BANK_ACCOUNT_URI);

	}

	public static String showAddBankAccountPage(HttpServletRequest request) {

		return redirectToView(request, BankDetailsController.ADD_BANK_ACCOUNT_URI);

	}

	public static String showBankAccountSummaryPage(HttpServletRequest request) {

		return redirectToView(request, BankDetailsController.BANK_ACCOUNT_SUMMARY_URI);

	}

	public static String showAwardAccessPage(HttpServletRequest request) {

		return redirectToView(request, AwardAccessController.AWARD_ACCESS);

	}

	public static void setAssessmentFeeSummaryData(Model model, AssessmentFeeService assessmentFeeService,
												   long dsaApplicationNumber) {
		logger.info("Setting the AssessmentFee summary data");

		List<AssessmentFeeVO> items = assessmentFeeService.getAssessmentItems(dsaApplicationNumber);

		Double total = items.stream().map(AssessmentFeeVO::getCost).mapToDouble(BigDecimal::doubleValue).sum();

		model.addAttribute("assessmentFeeTotal", currencyLocalisation(total));
		model.addAttribute("assessmentFeeItems", items);
	}
	
	public static boolean isAdvisorDeclarationsCompleted(long dsaApplicationNumber, ApplicationService applicationService) {

		ApplicationSectiponStatusVO sectionStatus = applicationService.getApplicationSectionStatus(dsaApplicationNumber,
				Section.ADVISOR_DECLARATION);

		return sectionStatus.getSectionStatus().equals(SectionStatus.COMPLETED);
	}

	public static String formatSortcode(String sortCode) {

		if (StringUtils.hasText(sortCode) && sortCode.length() == 6) {
			String firstTwoNumbers = sortCode.substring(0, 2);
			String secondTwoNumbers = sortCode.substring(2, 4);
			String thirdTwoNumbers = sortCode.substring(4, 6);
			sortCode = firstTwoNumbers + "-" + secondTwoNumbers + "-" + thirdTwoNumbers;
		}
		return sortCode;
	}

	public static String formatAccountNumber(String accountNumber) {
		if (StringUtils.hasText(accountNumber)) {

			String lastCharacters = accountNumber.substring(4);
			return "****" + lastCharacters;
		}
		return accountNumber;
	}

	public static Map<String, String> populateDisabilitiesData(DisabilitiesService disabilitiesService,
															   long dsaApplicationNumber) {
		List<DisabilityTypeVO> types = getApplicationDisabilityTypes(disabilitiesService, dsaApplicationNumber);

		Map<String, String> disabilitiesMap = new LinkedHashMap<>();

		for (DisabilityTypeVO disabilityTypeVO : types) {

			disabilitiesMap.put(disabilityTypeVO.getDisabilityTypeDesc(), disabilityTypeVO.getDisabilityTypeHintText());
			if (disabilityTypeVO.getDisabilityCode().equals(DisabilityTypeVO.DISABILITY_NOT_LISTED)) {
				disabilitiesMap.put(disabilityTypeVO.getDisabilityTypeDesc(),
						disabilityTypeVO.getDisabilityNotlistedText());

			}
		}
		return disabilitiesMap;
	}

	public static List<DisabilityTypeVO> getApplicationDisabilityTypes(DisabilitiesService disabilitiesService,

																	   Long dsaApplicationNumber) {
		return disabilitiesService.populateApplicationDisabilities(dsaApplicationNumber);

	}

	public static String toCapitaliseWord(String input) {
		String process = input.toLowerCase();
		return Arrays.stream(process.split("\\s+")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
				.collect(Collectors.joining(" "));
	}

	public static void setAllowanceAndDeclarationCompletionStatusIntheModel(Model model,
																			ApplicationService applicationService, long dsaApplicationNumber, long studentReferenceNumber) {
		ApplicationResponse applicationResponse;
		try {
			applicationResponse = applicationService.findApplication(dsaApplicationNumber, studentReferenceNumber);
			model.addAttribute("allowancesNotCompleted", !applicationResponse.isAllAllowancesCompleted());
			model.addAttribute("ADVISOR_DECLARTION_NOT_COMPLETED",
					!applicationResponse.isAdvisorDeclarationCompleted());
			model.addAttribute("STUDENT_DECLARTION_NOT_COMPLETED",
					!applicationResponse.isAdvisorDeclarationCompleted());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static StudentResultVO getStudentResultWithSuid(final FindStudentService findStudentService,
														   final long studentReferenceNumber, Integer sessionCode) throws IllegalAccessException {

		StudentResultVO studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber,
				sessionCode);
		final StudentPersonalDetails studentPersonalDetails = findStudentService
				.findStudentPersonDetailsStudByRefNumber(studentReferenceNumber);
		studentResultVO.setSuid(studentPersonalDetails.getUserId());
		logger.info("Student Result With Suid {}", studentResultVO);
		return studentResultVO;
	}

	public static long getStudentRefFromSession(HttpSession httpsession) {
		Long studentReferenceNumber = (Long) httpsession.getAttribute(STUDENT_REFERENCE_NUMBER);
		logger.info(" Stud ref in the session {}", studentReferenceNumber);
//		Long studentReferenceNumber = null;
//		if (!StringUtils.hasText(stud_ref_str)) {
//			logger.error("No Stud ref in the session");
//		} else {
//			studentReferenceNumber = Long.valueOf(stud_ref_str);
//			logger.info("No Stud ref in the session {}", studentReferenceNumber);
//		}
		return studentReferenceNumber;
	}

	public static String getSuidFromSession(HttpSession httpsession) {
		String suid = (String) httpsession.getAttribute(SUID);
		logger.info(" suid in the session {}", suid);
		return suid;
	}

	public static void setStudentRefInSession(HttpSession httpSession, long studentRefNo, String suid) {
		httpSession.setAttribute(STUDENT_REFERENCE_NUMBER, studentRefNo);
		httpSession.setAttribute(SUID, suid);
	}

	public static QuoteDetailsFormVO mapQuoteOptionFormVO(Model model, long dsaApplicationNumber,
														  StudentResultVO studentResultVO, int sessionCode) {

		QuoteDetailsFormVO quoteDetailsFormVO = new QuoteDetailsFormVO();
		quoteDetailsFormVO.setAcademicYear(studentResultVO.getStudentCourseYear().getAcademicYearFull());
		quoteDetailsFormVO.setSessionCode(sessionCode);
		quoteDetailsFormVO.setDsaApplicationNumber(dsaApplicationNumber);
		quoteDetailsFormVO.setStudentReferenceNumber(studentResultVO.getStudentReferenceNumber());
		quoteDetailsFormVO
				.setAdvisorId(Objects.requireNonNull(model.getAttribute(DSAConstants.LOGGEDIN_USER_TYPE)).toString());
		quoteDetailsFormVO.setFirstName(studentResultVO.getFirstName());
		quoteDetailsFormVO.setLastName(studentResultVO.getLastName());
		return quoteDetailsFormVO;
	}

	public static void setPreviousYearEligibility(HttpSession httpSession, boolean previousYearEligibility) {
		httpSession.setAttribute(PREVIOUS_YEAR_ELIGIBILITY, previousYearEligibility);
	}

	public static void setEquipmentPaymentForData(Model model, EquipmentPaymentService equipmentPaymentService,
												  ConfigDataService configDataService, long dsaApplicationNumber, long studRefNo) {

		DSAApplicationsMade dsaApplication = equipmentPaymentService
				.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber, studRefNo);

		boolean showPaymentInstitution = showPaymentInstitution(equipmentPaymentService,
				dsaApplication.getSessionCode(), studRefNo);
		boolean hasEquipments = hasEquipments(equipmentPaymentService, dsaApplicationNumber);
		if (hasEquipments) {
			if (showPaymentInstitution) {
				AddEquipmentPaymentFormVO addEquipmentPaymentFormVO = new AddEquipmentPaymentFormVO();
				model.addAttribute(SHOW_PAYMENT_FOR_SECTION, true);
				addEquipmentPaymentFormVO
						.setPaymentForItem(equipmentPaymentService.getPaymentType(dsaApplicationNumber).name());
				addEquipmentPaymentFormVO.setDsaApplicationNumber(dsaApplicationNumber);
				addEquipmentPaymentFormVO.setStudentReferenceNumber(studRefNo);
				model.addAttribute("addEquipmentPaymentFormVO", addEquipmentPaymentFormVO);
			} else {
				model.addAttribute(SHOW_PAYMENT_FOR_SECTION, false);
				equipmentPaymentService.deletepaymentType(dsaApplicationNumber);
			}
		} else {
			model.addAttribute(SHOW_PAYMENT_FOR_SECTION, false);
			equipmentPaymentService.deletepaymentType(dsaApplicationNumber);
		}
	}

	public static String sanetizeSpecialCharacters(String input) {
		String output = input.chars()
				.mapToObj(c -> SPECIAL_CHARACTERS.contains((char) c) ? "_" : String.valueOf((char) c))
				.collect(Collectors.joining());
		return output;
	}

	private static boolean showPaymentInstitution(EquipmentPaymentService equipmentService, int sessionCode,
												  long studRefNo) {
		return equipmentService.isPaymentInstitution(studRefNo, sessionCode);
	}

	private static boolean hasEquipments(EquipmentPaymentService equipmentPaymentService, long dsaApplicationNumber) {

		return equipmentPaymentService.hasEquipments(dsaApplicationNumber);
	}

}
