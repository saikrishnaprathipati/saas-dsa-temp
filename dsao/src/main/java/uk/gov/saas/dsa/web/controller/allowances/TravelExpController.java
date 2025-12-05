package uk.gov.saas.dsa.web.controller.allowances;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.service.allowances.TravelExpAllowancesService;
import uk.gov.saas.dsa.service.allowances.TravelProviderExistsException;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.travelExp.*;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO.TravelExpAllowanceVOBuilder;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.saas.dsa.domain.refdata.YesNoType.NO;
import static uk.gov.saas.dsa.domain.refdata.YesNoType.YES;
import static uk.gov.saas.dsa.model.TravelExpType.TAXI;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

/**
 * The Travel Expense Controller
 */
@Controller
public class TravelExpController {

	private static final String ELECTRIC = "electric";

	private static final String ORG_SPRINGFRAMEWORK_WEB_SERVLET_HANDLER_MAPPING_BEST_MATCHING_PATTERN = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

	private static final String CHANGE_TRAVEL_EXP_ITEM = "CHANGE_TRAVEL_EXP_ITEM";

	private static final String REMOVE_PROVIDER_BACK = "REMOVE_PROVIDER_BACK";

	private static final String ACTION_NAME = "ACTION_NAME";

	private static final String BACK_TO_TAXI = "BACK_TO_TAXI";
	private static final String CHANGE_TRAVEL_EXP_PATH = "changeTravelExp";
	private static final String BACK_TO_OWN_VEHICLE = "BACK_TO_OWN_VEHICLE";
	private static final String PROVIDERS_LIST_IN_SESSION = "inputList";
	private static final String RETURN_JOURNEYS = "returnJourneys";
	private static final String WEEKS = "weeks";
	private static final String START_LOC_POSTCODE = "startLocationPostcode";
	private static final String END_LOC_POSTCODE = "endLocationPostcode";
	private static final String VEHICLE_TYPE = "vehicleType";
	private static final String FUEL_COST = "fuelCost";
	private static final String MILES_PER_GALLON = "milesPerGallon";
	private static final String KWH_COST = "kwhCost";
	private static final String KWH_CAPACITY = "kwhCapacity";
	private static final String RANGE_OF_CAR = "rangeOfCar";
	private static final String TRAVELEXP_NON_APPROVED_CONTRACTOR_TWO_REQUIRED = "travelexp.non-approvedContractor.two.required";
	private static final String TRAVELEXP_TAXI_PROVIDER_REQUIRED = "travelexp.taxiProvider.required";
	private static final String REMOVE_TRAVEL_EXP_ITEM_ACTION = "REMOVE_TRAVEL_EXP_ITEM";
	private static final String CONFIRM_REMOVE_TAXI_PROVIDER_ACTION = "CONFIRM_REMOVE_PROVIDER";
	private static final String ADVISOR_TRAVEL_EXP_REMOVE_TAXI_PROVIDER_PAGE = "advisor/travelExp/removeTaxiProvider";
	private static final String ADVISOR_TRAVEL_EXP_REMOVE_PAGE = "advisor/travelExp/removeTravelExp";
	private static final String CONFIRM_REMOVE_TRAVEL_EXP_ACTION = "CONFIRM_REMOVE_TRAVEL_EXP";
	private static final String REMOVE_TAXI_PROVIDER_FORM_VO = "removeTaxiProviderFormVO";
	private static final String REMOVE_TRAVEL_EXP_FORM_VO = "removeTravelExpFormVO";
	private static final String REMOVE_TAXI_PROVIDER_PATH = "removeTaxiProvider";
	private static final String REMOVE_TRAVEL_EXP_PATH = "removeTravelExp";
	private static final String REMOVE_PROVIDER = "REMOVE_PROVIDER";
	private static final String TRAVELEXP_RECOMMENDED_PROVIDER_REQUIRED = "travelexp.recommendedProvider.required";
	private static final String TAXI_PROVIDER_RECOMMENDED_PROVIDER = "taxiProvider.recommendedProvider";
	private static final String TRAVELEXP_COST_REQUIRED = "travelexp.cost.required";
	private static final String TRAVELEXP_APPROVED_CONTRACTOR_REQUIRED = "travelexp.approvedContractor.required";
	private static final String TAXI_PROVIDER_APPROVED_CONTRACTOR = "taxiProvider.approvedContractor";
	private static final String TRAVELEXP_COST_INVALID = "travelexp.cost.invalid";
	private static final String TRAVELEXP_FUEL_COST_INVALID = "travelexp.fuelCost.invalid";
	private static final String TRAVELEXP_KWH_COST_INVALID = "travelexp.kwhCost.invalid";
	private static final String TAXI_PROVIDER_COST = "taxiProvider.cost";
	private static final String HIDE_ADD_PROVIDOR_PANEL = "HIDE_ADD_PROVIDOR_PANEL";
	private static final String ADD_TAXI_ACTION = "ADD_TAXI_ACTION";
	private static final String ADVISOR_TRAVEL_EXP_ADD_OWN_VEHICLE_PAGE = "advisor/travelExp/addOwnVehicle";
	private static final String ADVISOR_TRAVEL_EXP_ADD_LIFT_PAGE = "advisor/travelExp/addLift";
	private static final String ADVISOR_TRAVEL_EXP_ADD_TAXI_PAGE = "advisor/travelExp/addTaxi";
	private static final String ADVISOR_TRAVEL_EXP_TRAVEL_EXP_SUMMARY_PAGE = "advisor/travelExp/travelExpSummary";
	private static final String INIT = "INIT_";
	private static final String SAVE_LIFT_ACTION = "SAVE_LIFT_ACTION";
	private static final String SAVE_TAXI_ACTION = "SAVE_TAXI_ACTION";
	private static final String SAVE_OWN_VEHICLE_ACTION = "SAVE_OWN_VEHICLE_ACTION";
	private static final String INIT_LIFT = "INIT_LIFT";
	private static final String INIT_TAXI = "INIT_TAXI";
	private static final String INIT_OWN_VEHICLE = "INIT_OWN_VEHICLE";
	private static final String INIT_TRAVEL_EXP_FROM_TE_SELECTION_ACTION = "INIT_TRAVEL_EXP_FROM_TE_SELECTION";
	private static final String SELECTED_TRAVEL_EXP_TYPES = "SELECTED_TRAVEL_EXP_TYPES";
	private static final String ADD_TRAVEL_EXP_FORM_VO = "addTravelExpFormVO";
	private static final String SELECT_TRAVEL_EXP_FROM_NMPH_ACTION = "SELECT_TRAVEL_EXP_FROM_NMPH_SUMMARY";
	private static final String SELECT_TRAVEL_EXP_FROM_TA_SUMMARY_ACTION = "SELECT_TRAVEL_EXP_FROM_TA_SUMMARY";
	private static final String TRAVEL_EXP_SELECTION_FORM_VO = "travelExpSelectionFormVO";
	private static final String ADVISOR_TRAVEL_EXP_SELECTION_PAGE = "advisor/travelExp/travelExpensesSelection";
	private static final String TRAVEL_EXP_FIELD_INVALID = "travelexp.%s.invalid";
	private static final String TRAVEL_EXP_FIELD_REQUIRED = "travelexp.%s.required";
	private final Logger logger = LogManager.getLogger(this.getClass());

	public static final String SHOW_SKIP_TRAVEL_EXP = "showSkipTravelExp";

	private final TravelExpAllowancesService travelExpService;
	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;

	public TravelExpController(TravelExpAllowancesService travelExpService, FindStudentService findStudentService,
							   ApplicationService applicationService) {
		this.travelExpService = travelExpService;
		this.findStudentService = findStudentService;
		this.applicationService = applicationService;

	}

	@PostMapping(SELECT_TRAVEL_EXPENSE_PATH)
	public String selectTravelExpense(Model model, @RequestParam(value = ACTION) String action,
									  HttpServletRequest request,
									  @Valid @ModelAttribute(name = TRAVEL_EXP_SELECTION_FORM_VO) TravelExpSelectionFormVO travelExpSelectionFormVO,
									  BindingResult bindingResult, HttpSession httpsession) throws Exception {
		logger.info("in selectTravelExpense call action: {}, request: {}", action, travelExpSelectionFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}

		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = travelExpSelectionFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = travelExpSelectionFormVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		switch (action.toUpperCase()) {
			case SKIP_ACTION:
				view = AllowancesHelper.showAccommodationTypeSelection(request);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case BACK_ACTION:
				view = AllowancesHelper.showNMPHSummary(request);
				break;
			case INIT_TRAVEL_EXP_FROM_TE_SELECTION_ACTION:
				if (hasErrors(bindingResult)) {
					setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
					setSkipTravelExpLink(model, dsaApplicationNumber);
					view = ADVISOR_TRAVEL_EXP_SELECTION_PAGE;
				} else {
					view = getTravelExpScreen(request, httpsession, travelExpSelectionFormVO);
				}
				break;
			case SELECT_TRAVEL_EXP_FROM_TA_SUMMARY_ACTION:
			case "SKIP_NMPH":
			case BACK_TO_SELECT_TRAVEL_EXP:
				view = initialiseTravelExpSelectionPage(model, dsaApplicationNumber, studentReferenceNumber);
				break;
			case SELECT_TRAVEL_EXP_FROM_NMPH_ACTION:

				boolean hasTravelAllowances = !getAllTravelExpItems(travelExpSelectionFormVO.getDsaApplicationNumber())
						.isEmpty();
				if (hasTravelAllowances) {
					view = showTravelExpSummary(request);
				} else {
					view = initialiseTravelExpSelectionPage(model, dsaApplicationNumber, studentReferenceNumber);
				}
				break;
		}

		logger.info("View or redirection to {}", view);
		return view;
	}

	@PostMapping(path = {ADD_TRAVEL_EXPENSE_PATH, CHANGE_TRAVEL_EXP_PATH})
	public String addTravelExp(Model model,
							   @Valid @ModelAttribute(name = ADD_TRAVEL_EXP_FORM_VO) AddTravelExpFormVO addTravelExpFormVO,
							   BindingResult bindingResult, @RequestParam(value = ACTION) String action, HttpServletRequest request,
							   HttpSession httpSession) throws Exception {
		logger.info("Add travel expenses {}", action);
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		TravelExpType travelExpTypeParam = getWorkingScreen(httpSession);

		logger.info("in addTravelExp action: {}, Form: {}", action, addTravelExpFormVO);
		logger.info("travelExpType {}", travelExpTypeParam);

		AllowancesHelper.hasMandatoryValues(model, addTravelExpFormVO.getDsaApplicationNumber(),
				addTravelExpFormVO.getStudentReferenceNumber());
		model.addAttribute(ADD_TRAVEL_EXP_FORM_VO, addTravelExpFormVO);
		String view = ERROR_PAGE;
		if (action.equalsIgnoreCase(CHANGE_TRAVEL_EXP_ITEM) || action.equalsIgnoreCase("CHANGE_FROM_SUMMARY")) {
			TravelExpType travelExpType = addTravelExpFormVO.getTravelExpType();
			setTravelExpensesTypesIntoSession(httpSession, Collections.singletonList(travelExpType));
			travelExpTypeParam = travelExpType;
		}
		if (action.equalsIgnoreCase("CHANGE_FROM_SUMMARY")) {
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, DSAConstants.ALLOWANCES_SUMMARY_ACTION);
		}

		String attribute = (String) request
				.getAttribute(ORG_SPRINGFRAMEWORK_WEB_SERVLET_HANDLER_MAPPING_BEST_MATCHING_PATTERN);
		if (attribute.contains(CHANGE_TRAVEL_EXP_PATH)) {
			model.addAttribute(ACTION_NAME, CHANGE_TRAVEL_EXP_PATH);
		} else {
			model.addAttribute(ACTION_NAME, ADD_TRAVEL_EXPENSE_PATH);
		}

		if (attribute.contains(CHANGE_TRAVEL_EXP_PATH) && (action.equalsIgnoreCase(BACK_TO_OWN_VEHICLE)
				|| action.equalsIgnoreCase(BACK_TO_SELECT_TRAVEL_EXP) || action.equalsIgnoreCase(BACK_TO_TAXI))) {
			return showTravelExpSummary(request);
		}
		if (action.equalsIgnoreCase(DSAConstants.ALLOWANCES_SUMMARY_ACTION)) {
			return AllowancesHelper.showAllowancesSummary(request);
		}
		if (action.equalsIgnoreCase(BACK_TO_OWN_VEHICLE)) {
			travelExpTypeParam = updateSessionValues(httpSession, TravelExpType.OWN_VEHICLE);
			action = INIT_OWN_VEHICLE;
		}
		if (action.equalsIgnoreCase(BACK_TO_TAXI)) {
			travelExpTypeParam = updateSessionValues(httpSession, TravelExpType.TAXI);
			action = INIT_TAXI;
		}

		switch (Objects.requireNonNull(travelExpTypeParam)) {
			case OWN_VEHICLE:
				if (!action.equalsIgnoreCase("CHANGE_FROM_SUMMARY")) {
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, "BACK_TO_SELECT_TRAVEL_EXP");
				}
				view = ADVISOR_TRAVEL_EXP_ADD_OWN_VEHICLE_PAGE;
				view = processScreenData(model, addTravelExpFormVO, bindingResult, action, travelExpTypeParam, request,
						httpSession, view);
				break;
			case LIFT:
				if (!action.equalsIgnoreCase("CHANGE_FROM_SUMMARY")) {
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, "BACK_TO_TAXI");
				}
				view = ADVISOR_TRAVEL_EXP_ADD_LIFT_PAGE;
				view = processScreenData(model, addTravelExpFormVO, bindingResult, action, travelExpTypeParam, request,
						httpSession, view);
				break;
			case TAXI:
				if (!action.equalsIgnoreCase("CHANGE_FROM_SUMMARY")) {
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, "BACK_TO_OWN_VEHICLE");
				}
				view = ADVISOR_TRAVEL_EXP_ADD_TAXI_PAGE;
				model.addAttribute(HIDE_ADD_PROVIDOR_PANEL, false);
				view = processScreenData(model, addTravelExpFormVO, bindingResult, action, travelExpTypeParam, request,
						httpSession, view);
				break;
			default:
				addErrorMessage(model, action, request);
				break;
		}

		logger.info("View or redirecting to {}", view);
		return view;

	}

	private TravelExpType updateSessionValues(HttpSession httpsession, TravelExpType type) {
		logger.info("TravelExpType to add to the session {}", type);
		List<TravelExpType> expensesFromTheSession = getSelectedTravelExpensesFromTheSession(httpsession);
		if (expensesFromTheSession != null && expensesFromTheSession.size() == 1) {
			TravelExpType travelExpType = expensesFromTheSession.get(0);
			if (!travelExpType.equals(type)) {
				ArrayList<TravelExpType> list = new ArrayList<>();
				list.add(travelExpType);
				list.add(type);
				expensesFromTheSession = list;
			}

		} else {
			expensesFromTheSession.add(type);
		}

		setTravelExpensesTypesIntoSession(httpsession, expensesFromTheSession);

		return type;
	}

	@PostMapping(path = {REMOVE_TRAVEL_EXP_PATH})
	public String removeTravelExp(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
								  @ModelAttribute(name = REMOVE_TRAVEL_EXP_FORM_VO) RemoveTravelExpFormVO removeItemFormVO,
								  BindingResult bindingResult) throws IllegalAccessException {
		logger.info("in removeTravelExp action: {}, Form: {}", action, removeItemFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());
		String view = ERROR_PAGE;
		model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, BACK_ACTION);
		switch (action.toUpperCase()) {
			case BACK_ACTION:
				view = showTravelExpSummary(request);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case "REMOVE_FROM_SUMMARY":
				model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, DSAConstants.ALLOWANCES_SUMMARY_ACTION);
				model.addAttribute(REMOVE_TRAVEL_EXP_FORM_VO, removeItemFormVO);
				view = ADVISOR_TRAVEL_EXP_REMOVE_PAGE;
				break;
			case DSAConstants.ALLOWANCES_SUMMARY_ACTION:
				view = AllowancesHelper.showAllowancesSummary(request);
				break;
			case REMOVE_TRAVEL_EXP_ITEM_ACTION:
				model.addAttribute(REMOVE_TRAVEL_EXP_FORM_VO, removeItemFormVO);
				view = ADVISOR_TRAVEL_EXP_REMOVE_PAGE;
				break;
			case CONFIRM_REMOVE_TRAVEL_EXP_ACTION:
				String removeItem = removeItemFormVO.getRemoveItem();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						long id = removeItemFormVO.getId();
						travelExpService.deleteTravelExp(id);
						ServiceUtil.updateSectionStatus(applicationService, removeItemFormVO.getDsaApplicationNumber(),
								Section.ALLOWANCES, SectionStatus.STARTED);
					}
					view = showTravelExpSummary(request);

				} else {
					bindingResult.rejectValue(REMOVE_ITEM, DSAConstants.ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(REMOVE_TRAVEL_EXP_FORM_VO, removeItemFormVO);
					view = ADVISOR_TRAVEL_EXP_REMOVE_PAGE;
				}
				break;
			default:
				addErrorMessage(model, action, request);
				break;
		}

		return view;
	}

	@PostMapping(path = {REMOVE_TAXI_PROVIDER_PATH})
	public String removeTravelProvider(Model model, HttpServletRequest request,
									   @RequestParam(value = ACTION) String action,
									   @ModelAttribute(name = REMOVE_TAXI_PROVIDER_FORM_VO) RemoveTaxiProviderFormVO removeItemFormVO,
									   BindingResult bindingResult, HttpSession httpSession) throws IllegalAccessException {
		logger.info("in removeTravelProvider call action: {}, request: {}", action, removeItemFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());

		String view = ERROR_PAGE;
		switch (action.toUpperCase()) {
			case "REMOVE_PROVIDER":
				model.addAttribute(REMOVE_TAXI_PROVIDER_FORM_VO, removeItemFormVO);
				view = ADVISOR_TRAVEL_EXP_REMOVE_TAXI_PROVIDER_PAGE;
				break;
			case REMOVE_PROVIDER_BACK:
				view = showAddTravelExp(request);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case CONFIRM_REMOVE_TAXI_PROVIDER_ACTION:
				String removeItem = removeItemFormVO.getRemoveItem();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						TaxiProviderFormVO itemToRemove = removeItemFormVO.getTaxiProviderToRemove();
						if (itemToRemove.getId() > 0) {
							travelExpService.deleteTravelProvider(itemToRemove.getId());
						}
						List<TaxiProviderFormVO> taxiProvidersBeforeRemove = getProvidersListFromTheSession(httpSession);

						logger.info("taxiProvidersBeforeRemove: {}", taxiProvidersBeforeRemove);
						List<TaxiProviderFormVO> taxiProvidersAfterRemove = taxiProvidersBeforeRemove.stream()
								.filter(t -> !t.equals(itemToRemove)).collect(Collectors.toList());
						logger.info("taxiProvidersAfterRemove: {}", taxiProvidersAfterRemove);
						removeItemFormVO.setTaxiProviderList(taxiProvidersAfterRemove);
						setProvidersInTheSession(httpSession, taxiProvidersAfterRemove);

						view = showAddTravelExp(request);
					} else {
						view = showAddTravelExp(request);
					}
				} else {
					bindingResult.rejectValue(REMOVE_ITEM, DSAConstants.ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(REMOVE_TAXI_PROVIDER_FORM_VO, removeItemFormVO);
					view = ADVISOR_TRAVEL_EXP_REMOVE_TAXI_PROVIDER_PAGE;
				}
				break;
			default:
				addErrorMessage(model, action, request);
				break;
		}

		return view;
	}

	@PostMapping(TRAVEL_EXP_SUMMARY_PATH)
	public String travelExpSummary(Model model,
								   @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {
		logger.info("Travel exp summary Path: {}, form data: {}", NMPH_SUMMARY_PATH, keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
		setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);

		setTravelExpSummaryData(model, travelExpService, keyDataVO.getDsaApplicationNumber());
		String view = ADVISOR_TRAVEL_EXP_TRAVEL_EXP_SUMMARY_PAGE;

		logger.info("View or redirect to {}", view);
		return view;

	}

	private String processScreenData(Model model, AddTravelExpFormVO addTravelExpFormVO, BindingResult bindingResult,
									 String action, TravelExpType travelExpTypeParam, HttpServletRequest request, HttpSession httpSession,
									 String view) throws IllegalAccessException {
		logger.info("processScreenData view  {}", view);
		String upperCaseAction = action.toUpperCase();
		if (!action.contains(travelExpTypeParam.name()) && !Arrays
				.asList(DASHBOARD_ACTION, BACK_TO_SELECT_TRAVEL_EXP, REMOVE_PROVIDER).contains(upperCaseAction)) {
			upperCaseAction = INIT + travelExpTypeParam.name();
		}
		switch (upperCaseAction) {
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case BACK_TO_SELECT_TRAVEL_EXP:
				view = redirectToView(request, SELECT_TRAVEL_EXPENSE_PATH);
				break;
			case INIT_OWN_VEHICLE:
			case INIT_TAXI:
			case INIT_LIFT:
				initialiseAddPageWithData(model, travelExpTypeParam, addTravelExpFormVO, action, httpSession);
				break;
			case SAVE_OWN_VEHICLE_ACTION:
			case SAVE_LIFT_ACTION:
				validateJourneyFormData(addTravelExpFormVO, bindingResult);
				validateVehicleInputs(bindingResult, addTravelExpFormVO);
				if (noErrors(bindingResult)) {
					view = saveTaxiExp(addTravelExpFormVO, travelExpTypeParam, request, httpSession);
				}
				break;
			case REMOVE_PROVIDER:
				logger.info("Removing provider:{}", addTravelExpFormVO.getTaxiProvider());
				view = redirectToView(request, REMOVE_TAXI_PROVIDER_PATH);
				break;
			case SAVE_TAXI_ACTION:
				validateJourneyFormData(addTravelExpFormVO, bindingResult);

				List<TaxiProviderFormVO> list = getProvidersListFromTheSession(httpSession);
				if (!list.isEmpty()) {
					addTravelExpFormVO.setTaxiProviderList(list);
				}

				boolean oneApprovedProvider = hasProviders(YES, 1, list);
				boolean oneNon_ApprovedProvider = hasProviders(NO, 1, list);

				if (list.isEmpty()) {
					ValidationHelper.rejectFieldValue(bindingResult, "panelValidation", TRAVELEXP_TAXI_PROVIDER_REQUIRED);
				} else if ((oneApprovedProvider && oneNon_ApprovedProvider) || oneNon_ApprovedProvider) {
					ValidationHelper.rejectFieldValue(bindingResult, "panelValidation",
							TRAVELEXP_NON_APPROVED_CONTRACTOR_TWO_REQUIRED);
				}

				if (noErrors(bindingResult) && hasCorrectTravelProvides(list)) {
					addTravelExpFormVO.setTaxiProviderList(list);
					view = saveTaxiExp(addTravelExpFormVO, travelExpTypeParam, request, httpSession);
					setProvidersInTheSession(httpSession, null);
				}

				setHideAddProviderPanel(model, hasCorrectTravelProvides(list));
				break;
			case DSAConstants.ALLOWANCES_SUMMARY_ACTION:
				view = AllowancesHelper.showAllowancesSummary(request);
				break;
			case ADD_TAXI_ACTION:

				validateTaxiProviderFormData(bindingResult, addTravelExpFormVO);

				List<TaxiProviderFormVO> inputList = getExistingProviders(addTravelExpFormVO, httpSession);
				TaxiProviderFormVO taxiProvider = addTravelExpFormVO.getTaxiProvider();
				boolean noErrors = noErrors(bindingResult);
				boolean hasOneNonApproved = hasProviders(NO, 1, inputList);
				boolean isApproved = YES.getDisplayValue().equals(taxiProvider.getApprovedContractor());
				taxiProvider.setValidated(noErrors && !(isApproved && hasOneNonApproved)
						&& formDataProviderIsNotExistInDBValues(inputList, taxiProvider));
				inputList.add(taxiProvider);

				List<TaxiProviderFormVO> updatedList = filterValidatedProviders(inputList);
				if (hasOneNonApproved && isApproved) {

					ValidationHelper.rejectFieldValue(bindingResult, TAXI_PROVIDER_APPROVED_CONTRACTOR,
							TRAVELEXP_NON_APPROVED_CONTRACTOR_TWO_REQUIRED);
					noErrors = false;
					addTravelExpFormVO.setTaxiProviderList(updatedList);
				}

				noErrors = saveTravelProvider(addTravelExpFormVO, bindingResult, inputList, noErrors, httpSession);

				if (noErrors) {
					addTravelExpFormVO.setTaxiProvider(new TaxiProviderFormVO());
				}
				boolean hidePanel = hideTaxiProvidersPanel(noErrors, addTravelExpFormVO.getTaxiProviderList());
				setHideAddProviderPanel(model, hidePanel);

				break;
			default:
				addErrorMessage(model, action, request);
				break;
		}
		logger.info("in processScreenData - View or redirecting to {}", view);
		return view;
	}

	private boolean hasCorrectTravelProvides(List<TaxiProviderFormVO> list) {
		return ((hasProviders(YES, 1, list) && list.size() == 1) || (hasProviders(NO, 2, list) && list.size() == 2));
	}

	private boolean formDataProviderIsNotExistInDBValues(List<TaxiProviderFormVO> inputList,
														 TaxiProviderFormVO taxiProvider) {
		boolean isNotExist = inputList == null || inputList.isEmpty();
		if (!isNotExist) {
			// When refreshing the browser after saving the Provider entry, the form data is
			// not resetting
			// check with DB values
			isNotExist = inputList.stream().filter(t -> {
				String dbValue = t.getApprovedContractor() + t.getCost() + t.getRecommendedProvider();
				String newValue = taxiProvider.getApprovedContractor() + taxiProvider.getCost()
						+ taxiProvider.getRecommendedProvider();
				logger.info("dbValue  {}", dbValue);
				logger.info("newValue {}", newValue);
				boolean formDataAndDBDataIsSame = dbValue.equalsIgnoreCase(newValue);
				if (formDataAndDBDataIsSame) {
					taxiProvider.setId(t.getId());
				}
				return formDataAndDBDataIsSame;
			}).collect(Collectors.toList()).isEmpty();
		}
		return isNotExist;
	}

	private boolean saveTravelProvider(AddTravelExpFormVO addTravelExpFormVO, BindingResult bindingResult,
									   List<TaxiProviderFormVO> inputList, boolean noErrors, HttpSession httpSession) {
		if (noErrors) {
			List<TaxiProviderFormVO> listInSession = getProvidersListFromTheSession(httpSession);

			List<TaxiProviderFormVO> tempList = new ArrayList<>();
			tempList.addAll(inputList);
			tempList.addAll(listInSession);
			if (hasProviders(YES, 1, tempList) && hasProviders(NO, 1, tempList)) {
				noErrors = false;
				ValidationHelper.rejectFieldValue(bindingResult, TAXI_PROVIDER_APPROVED_CONTRACTOR,
						TRAVELEXP_NON_APPROVED_CONTRACTOR_TWO_REQUIRED);
			} else {
				listInSession = tempList.stream().distinct().collect(Collectors.toList());
			}

			setProvidersInTheSession(httpSession, listInSession);

			addTravelExpFormVO.setTaxiProviderList(listInSession);
		}
		return noErrors;
	}

	private void validateJourneyFormData(AddTravelExpFormVO addTravelExpFormVO, BindingResult bindingResult) {
		validateReturnJourney(bindingResult, addTravelExpFormVO);
		validatePostCodes(bindingResult, addTravelExpFormVO);
		validateWeeks(bindingResult, addTravelExpFormVO);
	}

	private void validateVehicleInputs(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		String vehicleType = addTravelExpFormVO.getVehicleType();
		validateVehicleType(bindingResult, vehicleType);
		validateNonElectricVehicleInputs(bindingResult, addTravelExpFormVO, vehicleType);
		validateElectricVehicleInputs(bindingResult, addTravelExpFormVO, vehicleType);
	}

	private void validateElectricVehicleInputs(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO,
											   String vehicleType) {
		if (vehicleType != null && vehicleType.equalsIgnoreCase(ELECTRIC)) {
			validateKWHCost(bindingResult, addTravelExpFormVO);
			validateKWHCapacity(bindingResult, addTravelExpFormVO);
			validateRange(bindingResult, addTravelExpFormVO);
		}
	}

	private void validateKWHCost(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(KWH_COST)) {

			String kwhCost = addTravelExpFormVO.getKwhCost();
			if (!StringUtils.hasLength(kwhCost)) {
				ValidationHelper.rejectFieldValue(bindingResult, KWH_COST, format(TRAVEL_EXP_FIELD_REQUIRED, KWH_COST));
			} else {
				addTravelExpFormVO.setKwhCost(validateNumber(bindingResult, KWH_COST, kwhCost, KWH_COST,
						TRAVELEXP_KWH_COST_INVALID, NumberType.COST_X_YY, COST_REGEX));
			}
		}
	}

	private void validateKWHCapacity(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(KWH_CAPACITY)) {

			String kwhCapacity = addTravelExpFormVO.getKwhCapacity();
			if (!StringUtils.hasLength(kwhCapacity)) {
				ValidationHelper.rejectFieldValue(bindingResult, KWH_CAPACITY,
						format(TRAVEL_EXP_FIELD_REQUIRED, KWH_CAPACITY));
			} else {
				addTravelExpFormVO.setKwhCapacity(AllowancesHelper.validateNumber(bindingResult, KWH_CAPACITY,
						kwhCapacity, KWH_CAPACITY, String.format(TRAVEL_EXP_FIELD_INVALID, KWH_CAPACITY),
						NumberType.NUMBER_XX, TWO_DIG_NO_REGEX));
			}
		}
	}

	private void validateRange(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(RANGE_OF_CAR)) {

			String rangeOfCar = addTravelExpFormVO.getRangeOfCar();
			if (!StringUtils.hasLength(rangeOfCar)) {
				ValidationHelper.rejectFieldValue(bindingResult, RANGE_OF_CAR,
						format(TRAVEL_EXP_FIELD_REQUIRED, RANGE_OF_CAR));
			} else {
				addTravelExpFormVO.setRangeOfCar(AllowancesHelper.validateNumber(bindingResult, RANGE_OF_CAR,
						rangeOfCar, RANGE_OF_CAR, String.format(TRAVEL_EXP_FIELD_INVALID, RANGE_OF_CAR),
						NumberType.NUMBER_XXX, THREE_DIG_NO_REGEX));
			}
		}
	}

	private void validateNonElectricVehicleInputs(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO,
												  String vehicleType) {
		if (vehicleType != null && !vehicleType.equalsIgnoreCase(ELECTRIC)) {
			validateFuelCost(bindingResult, addTravelExpFormVO);
			validateMPG(bindingResult, addTravelExpFormVO);
		}
	}

	private void validateFuelCost(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(FUEL_COST)) {
			String fuelCost = addTravelExpFormVO.getFuelCost();
			if (!StringUtils.hasText(fuelCost)) {
				ValidationHelper.rejectFieldValue(bindingResult, FUEL_COST,
						format(TRAVEL_EXP_FIELD_REQUIRED, FUEL_COST));
			} else {
				addTravelExpFormVO.setFuelCost(validateNumber(bindingResult, FUEL_COST, fuelCost, FUEL_COST,
						TRAVELEXP_FUEL_COST_INVALID, NumberType.COST_XXX_YY, COST_REGEX));
			}
		}
	}

	private void validateMPG(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(MILES_PER_GALLON)) {
			String milesPerGallon = addTravelExpFormVO.getMilesPerGallon();
			if (!StringUtils.hasText(milesPerGallon)) {
				ValidationHelper.rejectFieldValue(bindingResult, MILES_PER_GALLON,
						format(TRAVEL_EXP_FIELD_REQUIRED, MILES_PER_GALLON));
			} else {

				addTravelExpFormVO.setMilesPerGallon(AllowancesHelper.validateNumber(bindingResult, MILES_PER_GALLON,
						milesPerGallon, MILES_PER_GALLON, String.format(TRAVEL_EXP_FIELD_INVALID, MILES_PER_GALLON),
						NumberType.NUMBER_XX, TWO_DIG_NO_REGEX));
			}

		}
	}

	private void validateVehicleType(BindingResult bindingResult, String vehicleType) {
		if (!bindingResult.hasFieldErrors(VEHICLE_TYPE)) {
			if (vehicleType == null) {
				ValidationHelper.rejectFieldValue(bindingResult, VEHICLE_TYPE,
						format(TRAVEL_EXP_FIELD_REQUIRED, VEHICLE_TYPE));
			}
		}
	}

	private void validateWeeks(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(WEEKS)) {
			if (addTravelExpFormVO.getWeeks() == null || addTravelExpFormVO.getWeeks() < 1) {
				ValidationHelper.rejectFieldValue(bindingResult, WEEKS, format(TRAVEL_EXP_FIELD_REQUIRED, WEEKS));
			}
		}
	}

	private void validatePostCodes(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		validateStartLocPostcode(bindingResult, addTravelExpFormVO);
		validateEndLocPostcode(bindingResult, addTravelExpFormVO);
	}

	private void validateEndLocPostcode(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(END_LOC_POSTCODE)) {
			String endLocationPostcode = addTravelExpFormVO.getEndLocationPostcode();
			if (!StringUtils.hasText(endLocationPostcode)) {
				ValidationHelper.rejectFieldValue(bindingResult, END_LOC_POSTCODE,
						format(TRAVEL_EXP_FIELD_REQUIRED, END_LOC_POSTCODE));
			} else {
				validatePostcode(bindingResult, endLocationPostcode, END_LOC_POSTCODE,
						format(TRAVEL_EXP_FIELD_INVALID, END_LOC_POSTCODE));
			}
			addTravelExpFormVO.setEndLocationPostcode(endLocationPostcode.toUpperCase());
		}
	}

	private void validateStartLocPostcode(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(START_LOC_POSTCODE)) {
			String startLocationPostcode = addTravelExpFormVO.getStartLocationPostcode();
			if (!StringUtils.hasText(startLocationPostcode)) {
				ValidationHelper.rejectFieldValue(bindingResult, START_LOC_POSTCODE,
						format(TRAVEL_EXP_FIELD_REQUIRED, START_LOC_POSTCODE));
			} else {
				validatePostcode(bindingResult, startLocationPostcode, START_LOC_POSTCODE,
						format(TRAVEL_EXP_FIELD_INVALID, START_LOC_POSTCODE));
			}
			addTravelExpFormVO.setStartLocationPostcode(startLocationPostcode.toUpperCase());
		}
	}

	private void validateReturnJourney(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {
		if (!bindingResult.hasFieldErrors(RETURN_JOURNEYS)) {
			if (!StringUtils.hasText(addTravelExpFormVO.getReturnJourneys())) {
				ValidationHelper.rejectFieldValue(bindingResult, RETURN_JOURNEYS,
						format(TRAVEL_EXP_FIELD_REQUIRED, RETURN_JOURNEYS));
			} else {
				addTravelExpFormVO.setReturnJourneys(
						validateNumber(bindingResult, RETURN_JOURNEYS, addTravelExpFormVO.getReturnJourneys(),
								RETURN_JOURNEYS, format(TRAVEL_EXP_FIELD_INVALID, RETURN_JOURNEYS),
								NumberType.RETURN_JOURNEYS, DSAConstants.JOURNEY_REGEX));
			}
		}
	}

	private String saveTaxiExp(AddTravelExpFormVO addTravelExpFormVO, TravelExpType travelExpTypeParam,
							   HttpServletRequest request, HttpSession httpsession) throws IllegalAccessException {
		try {
			saveTravelExpense(addTravelExpFormVO, travelExpTypeParam);
		} catch (TravelProviderExistsException e) {
			e.printStackTrace();
		}

		List<TravelExpType> typesInSession = processSessionData(travelExpTypeParam, httpsession);
		return processNextView(request, typesInSession);
	}

	private List<TaxiProviderFormVO> getExistingProviders(AddTravelExpFormVO addTravelExpFormVO,
														  HttpSession httpSession) {

		List<TaxiProviderFormVO> taxiProviderList = addTravelExpFormVO.getTaxiProviderList();
		if (taxiProviderList == null || taxiProviderList.isEmpty()) {
			List<TravelExpAllowanceVO> travelExpItemsInDB = getAllTravelExpItems(
					addTravelExpFormVO.getDsaApplicationNumber());
			Optional<TravelExpAllowanceVO> value = travelExpItemsInDB.stream()
					.filter(t -> t.getTravelExpType().equals(TravelExpType.TAXI)).findFirst();
			if (value.isPresent()) {
				List<TaxiProviderVO> existingList = value.get().getTaxiProvidersList();
				taxiProviderList = getTaxiProviderList(existingList);

			} else {
				taxiProviderList = new ArrayList<>();
			}
		}
		List<TaxiProviderFormVO> providers = filterValidatedProviders(taxiProviderList);

		List<TaxiProviderFormVO> listInSession = getUniqueProviders(httpSession, providers);

		setProvidersInTheSession(httpSession, listInSession);

		addTravelExpFormVO.setTaxiProviderList(listInSession);

		return providers;
	}

	private List<TaxiProviderFormVO> getUniqueProviders(HttpSession httpSession, List<TaxiProviderFormVO> providers) {
		List<TaxiProviderFormVO> tempList = new ArrayList<>();
		List<TaxiProviderFormVO> listInSession = getProvidersListFromTheSession(httpSession);
		tempList.addAll(providers);
		tempList.addAll(listInSession);
		listInSession = tempList.stream().distinct().collect(Collectors.toList());
		return listInSession;
	}

	private void setProvidersInTheSession(HttpSession httpSession, List<TaxiProviderFormVO> listInSession) {
		httpSession.setAttribute(PROVIDERS_LIST_IN_SESSION, listInSession);
	}

	private boolean hideTaxiProvidersPanel(boolean noErrors, List<TaxiProviderFormVO> updatedList) {
		return noErrors && hasCorrectTravelProvides(updatedList);
	}

	private void setHideAddProviderPanel(Model model, boolean hidePanel) {
		model.addAttribute(HIDE_ADD_PROVIDOR_PANEL, hidePanel);
	}

	private List<TravelExpType> processSessionData(TravelExpType travelExpTypeParam, HttpSession httpsession) {
		List<TravelExpType> typesInSession = getSelectedTravelExpensesFromTheSession(httpsession);
		logger.info("travelExpTypes in the session before remove: {}, {}", travelExpTypeParam, typesInSession);
		if (!typesInSession.isEmpty()) {
			if (typesInSession.size() == 1) {
				TravelExpType travelExpType = typesInSession.get(0);
				if (travelExpTypeParam.equals(travelExpType)) {
					typesInSession = new ArrayList<>();
				}
			} else {
				typesInSession.remove(TravelExpType.valueOf(travelExpTypeParam.name()));
			}

			logger.info("travelExpTypes in the session after remove: {}, {}", travelExpTypeParam, typesInSession);
			setTravelExpensesTypesIntoSession(httpsession, typesInSession);
		}
		return getSelectedTravelExpensesFromTheSession(httpsession);
	}

	private void saveTravelExpense(AddTravelExpFormVO addTravelExpFormVO, TravelExpType travelExpTypeParam)
			throws TravelProviderExistsException, IllegalAccessException {
		addTravelExpFormVO.setTravelExpType(travelExpTypeParam);
		TravelExpAllowanceVO savedVO = travelExpService.addTravelExpAllowance(formToVO(addTravelExpFormVO));
		voToForm(savedVO);
	}

	private void initialiseAddPageWithData(Model model, TravelExpType travelExpTypeParam,
										   AddTravelExpFormVO addTravelExpFormVO, String originalAction, HttpSession httpSession) {

		List<TravelExpAllowanceVO> travelExpItemsInDB = getAllTravelExpItems(
				addTravelExpFormVO.getDsaApplicationNumber());
		if (!travelExpItemsInDB.isEmpty()) {
			Optional<TravelExpAllowanceVO> item = travelExpItemsInDB.stream()
					.filter(t -> t.getTravelExpType().equals(travelExpTypeParam)).findFirst();
			if (item.isPresent()) {
				TravelExpAllowanceVO travelExpAllowanceVO = item.get();
				initialisePageWithExistingData(model, travelExpAllowanceVO, httpSession);
			} else {
				initialisePageWithFreshFormData(model, originalAction, addTravelExpFormVO, httpSession);
			}
		} else {
			initialisePageWithFreshFormData(model, originalAction, addTravelExpFormVO, httpSession);
		}
	}

	private void initialisePageWithExistingData(Model model, TravelExpAllowanceVO itemVO, HttpSession httpSession) {
		logger.info("initialising screen with existing data {}", itemVO);
		AddTravelExpFormVO formData = voToForm(itemVO);
		List<TaxiProviderFormVO> listInDB = formData.getTaxiProviderList();

		List<TaxiProviderFormVO> listInSession = getUniqueProviders(httpSession, listInDB);

		formData.setTaxiProviderList(listInSession);
		setProvidersInTheSession(httpSession, listInSession);
		model.addAttribute(ADD_TRAVEL_EXP_FORM_VO, formData);

		setHideAddProviderPanel(model,
				hideTaxiProvidersPanel(formData.getTaxiProviderList().size() <= 2, listInSession));
	}

	private void initialisePageWithFreshFormData(Model model, String originalAction, AddTravelExpFormVO existingFormVO,
												 HttpSession httpSession) {
		logger.info("initialising screen with fresh data");
		AddTravelExpFormVO newFormVO = new AddTravelExpFormVO();
		if (originalAction.equals(CONFIRM_REMOVE_TAXI_PROVIDER_ACTION)) {
			newFormVO = existingFormVO;

			List<TaxiProviderFormVO> listInSession = getProvidersListFromTheSession(httpSession);
			newFormVO.setTaxiProviderList(listInSession);
		}
		model.addAttribute(ADD_TRAVEL_EXP_FORM_VO, newFormVO);
		setHideAddProviderPanel(model, hideTaxiProvidersPanel(true, newFormVO.getTaxiProviderList()));
	}

	private List<TaxiProviderFormVO> getProvidersListFromTheSession(HttpSession httpSession) {
		@SuppressWarnings("unchecked")
		List<TaxiProviderFormVO> listInSession = (List<TaxiProviderFormVO>) httpSession
				.getAttribute(PROVIDERS_LIST_IN_SESSION);
		if (listInSession == null) {
			listInSession = new ArrayList<>();
		}
		return listInSession;
	}

	private List<TravelExpType> getSelectedTravelExpensesFromTheSession(HttpSession httpSession) {

		@SuppressWarnings("unchecked")
		List<TravelExpType> travelExpTypes = (List<TravelExpType>) httpSession.getAttribute(SELECTED_TRAVEL_EXP_TYPES);
		if (travelExpTypes == null) {
			travelExpTypes = new ArrayList<>();
		}
		logger.info("travelExpTypes in the session {}", travelExpTypes);
		return travelExpTypes;
	}

	private String processNextView(HttpServletRequest request, List<TravelExpType> types) {
		String view;
		if (!types.isEmpty()) {
			view = showAddTravelExp(request);
		} else {
			view = AllowancesHelper.showTravelExpSummary(request);
		}
		logger.info("Showing next view {}", view);
		return view;
	}

	private void validateTaxiProviderFormData(BindingResult bindingResult, AddTravelExpFormVO addTravelExpFormVO) {

		if (!StringUtils.hasText(addTravelExpFormVO.getTaxiProvider().getRecommendedProvider())) {

			ValidationHelper.rejectFieldValue(bindingResult, TAXI_PROVIDER_RECOMMENDED_PROVIDER,
					TRAVELEXP_RECOMMENDED_PROVIDER_REQUIRED);

		}
		if (!bindingResult.hasFieldErrors(TAXI_PROVIDER_COST)) {
			if (!StringUtils.hasText(addTravelExpFormVO.getTaxiProvider().getCost())) {
				ValidationHelper.rejectFieldValue(bindingResult, TAXI_PROVIDER_COST, TRAVELEXP_COST_REQUIRED);
			} else {

				addTravelExpFormVO.getTaxiProvider()
						.setCost(validateNumber(bindingResult, COST, addTravelExpFormVO.getTaxiProvider().getCost(),
								TAXI_PROVIDER_COST, TRAVELEXP_COST_INVALID, NumberType.COST_XXXXX_YY, COST_REGEX));

				addTravelExpFormVO.getTaxiProvider().setCostStr(
						currencyLocalisation(Double.parseDouble(addTravelExpFormVO.getTaxiProvider().getCost())));
			}
		}
		if (!StringUtils.hasText(addTravelExpFormVO.getTaxiProvider().getApprovedContractor()) || !AllowancesHelper
				.optionHasCorrectValue(addTravelExpFormVO.getTaxiProvider().getApprovedContractor().toUpperCase())) {
			ValidationHelper.rejectFieldValue(bindingResult, TAXI_PROVIDER_APPROVED_CONTRACTOR,
					TRAVELEXP_APPROVED_CONTRACTOR_REQUIRED);
		}

	}

	private AddTravelExpFormVO voToForm(TravelExpAllowanceVO travelExp) {
		AddTravelExpFormVO form = new AddTravelExpFormVO();
		form.setId(travelExp.getTravelExpNo());
		form.setDsaApplicationNumber(travelExp.getDsaApplicationNumber());
		form.setStudentReferenceNumber(travelExp.getStudentReferenceNumber());
		form.setStartLocationPostcode(travelExp.getStartLocationPostcode());
		form.setEndLocationPostcode(travelExp.getEndLocationPostcode());
		form.setReturnJourneys(travelExp.getReturnJourneys().toString());
		form.setTravelExpType(travelExp.getTravelExpType());
		form.setWeeks(travelExp.getWeeks());
		form.setVehicleType(travelExp.getVehicleType());
		form.setFuelCost(
				travelExp.getFuelCost() != null ? (AllowancesHelper.formatValue(travelExp.getFuelCost(), 2).toString())
						: null);
		form.setMilesPerGallon(travelExp.getMilesPerGallon() != null ? travelExp.getMilesPerGallon().toString() : null);
		form.setKwhCost(
				travelExp.getKwhCost() != null ? AllowancesHelper.formatValue(travelExp.getKwhCost(), 2).toString()
						: null);

		form.setKwhCapacity(travelExp.getKwhCapacity() != null ? travelExp.getKwhCapacity().toString() : null);
		form.setRangeOfCar(travelExp.getRangeOfCar() != null ? travelExp.getRangeOfCar().toString() : null);

		List<TaxiProviderFormVO> taxiProviderList = getTaxiProviderList(travelExp.getTaxiProvidersList());

		form.setTaxiProviderList(taxiProviderList);

		return form;
	}

	private TravelExpAllowanceVO formToVO(AddTravelExpFormVO item) {
		TravelExpAllowanceVOBuilder builder = TravelExpAllowanceVO.builder()
				.dsaApplicationNumber(item.getDsaApplicationNumber())
				.studentReferenceNumber(item.getStudentReferenceNumber())
				.endLocationPostcode(item.getEndLocationPostcode().toUpperCase())
				.startLocationPostcode(item.getStartLocationPostcode().toUpperCase())
				.returnJourneys(Integer.valueOf(item.getReturnJourneys())).travelExpType(item.getTravelExpType())
				.weeks(item.getWeeks());
		if (item.getTravelExpType().equals(TAXI)) {
			builder.taxiProvidersList(populateTaxiProvidersList(item.getTaxiProviderList()));
		} else {
			String vehicleType = item.getVehicleType();
			builder.vehicleType(vehicleType);
			if (!vehicleType.equalsIgnoreCase(ELECTRIC)) {
				builder.fuelCost(stringToBigDecimal(item.getFuelCost()))
						.milesPerGallon(Integer.valueOf(item.getMilesPerGallon()));
			} else {
				builder.kwhCost(stringToBigDecimal(item.getKwhCost()))
						.kwhCapacity(Integer.valueOf(item.getKwhCapacity()))
						.rangeOfCar(Integer.valueOf(item.getRangeOfCar()));
			}
		}

		return builder.build();
	}

	private List<TaxiProviderFormVO> getTaxiProviderList(List<TaxiProviderVO> list) {

		return list.stream().map(t -> {
			TaxiProviderFormVO taxiProviderFormVO = new TaxiProviderFormVO();
			taxiProviderFormVO.setApprovedContractor(t.getApprovedContractor());
			taxiProviderFormVO.setCost(AllowancesHelper.formatValue(t.getCost(), 2).toString());
			taxiProviderFormVO.setCostStr(currencyLocalisation(Double.parseDouble(taxiProviderFormVO.getCost())));
			taxiProviderFormVO.setRecommendedProvider(t.getRecommendedProvider());
			taxiProviderFormVO.setValidated(true);
			taxiProviderFormVO.setId(t.getId());
			return taxiProviderFormVO;
		}).collect(Collectors.toList());
	}

	private List<TaxiProviderVO> populateTaxiProvidersList(List<TaxiProviderFormVO> taxiProviderList) {

		return taxiProviderList.stream()
				.map(item -> TaxiProviderVO.builder().recommendedProvider(item.getRecommendedProvider())
						.approvedContractor(item.getApprovedContractor()).id(item.getId())
						.cost(AllowancesHelper.stringToBigDecimal(item.getCost())).build())
				.collect(Collectors.toList());
	}

	private String initialiseTravelExpSelectionPage(Model model, long dsaApplicationNumber, long studentReferenceNumber)
			throws Exception {

		setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);

		setSkipTravelExpLink(model, dsaApplicationNumber);

		model.addAttribute(TRAVEL_EXP_SELECTION_FORM_VO, new TravelExpSelectionFormVO());

		return ADVISOR_TRAVEL_EXP_SELECTION_PAGE;
	}

	private void setSkipTravelExpLink(Model model, long dsaApplicationNumber) {
		boolean noTravelExpItems = getAllTravelExpItems(dsaApplicationNumber).isEmpty();
		model.addAttribute(SHOW_SKIP_TRAVEL_EXP, noTravelExpItems);
	}

	private List<TravelExpAllowanceVO> getAllTravelExpItems(long dsaApplicationNumber) {
		return travelExpService.getTravelExpAllowances(dsaApplicationNumber);
	}

	private String getTravelExpScreen(HttpServletRequest request, HttpSession httpsession,
									  TravelExpSelectionFormVO travelExpSelectionFormVO) {

		List<TravelExpType> expenseTypes = travelExpSelectionFormVO.getTravelExpTypes().stream()
				.map(TravelExpType::valueOf).collect(Collectors.toList());
		setTravelExpensesTypesIntoSession(httpsession, expenseTypes);

		return showAddTravelExp(request);
	}

	private void setTravelExpensesTypesIntoSession(HttpSession httpsession, List<TravelExpType> expenseTypes) {
		httpsession.setAttribute(SELECTED_TRAVEL_EXP_TYPES, expenseTypes);
	}

	private TravelExpType getWorkingScreen(HttpSession httpsession) {

		List<TravelExpType> types = getSelectedTravelExpensesFromTheSession(httpsession);
		Optional<TravelExpType> expType = types.stream().sorted(Comparator.comparing(TravelExpType::getOrder))
				.findFirst();
		return expType.orElse(null);
	}

	private boolean hasProviders(YesNoType type, int count, List<TaxiProviderFormVO> taxiProviders) {
		List<TaxiProviderFormVO> list = new ArrayList<>();
		if (taxiProviders != null) {
			list = taxiProviders.stream().filter(t -> type.getDisplayValue().equals(t.getApprovedContractor()))
					.collect(Collectors.toList());
			logger.info("List of  {}, {}", (YES.equals(type) ? "Approved" : "Non - Approved"), list);
		}
		return list.size() == count;

	}

	private List<TaxiProviderFormVO> filterValidatedProviders(List<TaxiProviderFormVO> listProviders) {
		return listProviders.stream().filter(TaxiProviderFormVO::isValidated).collect(Collectors.toList());
	}

}
