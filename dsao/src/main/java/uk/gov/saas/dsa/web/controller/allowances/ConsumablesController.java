package uk.gov.saas.dsa.web.controller.allowances;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.getConsumableCap;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.mapToFormData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.redirectToView;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.sanitizeCost;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setConsumablesSummaryData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setStudentDetailsInTheModel;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.matches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.service.allowances.ConsumablesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.consumables.AddConsumableFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemChangeFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemRemoveFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

/**
 * The consumables controller
 */
@Controller
public class ConsumablesController {

	public static final String SHOW_SKIP_CONSUMABLES = "showSkipConsumables";

	public static final String ADD_CONSUMABLES_PATH = "addConsumables";
	public static final String SELECT_CONSUMABLES_PATH = "selectConsumables";
	public static final String REMOVE_CONSUMABLE_PATH = "removeConsumable";
	public static final String CHANGE_CONSUMABLE_PATH = "changeConsumable";
	public static final String CHANGE_CONSUMABLE_ITEM_PATH = "changeConsumableItem";
	private static final String CONSUMABLE_ITEM_DATA = "consumableItemData";
	private static final String ADD_CONSUMABLE_FORM_VO = "addConsumableFormVO";
	private static final String CONSUMABLE_ITEM_CHANGE_FORM_VO = "consumableItemChangeFormVO";
	public static final String CONSUMABLE_ITEM_REMOVE_FORM_VO = "consumableItemRemoveFormVO";
	public static final String CONSUMABLES_TYPE_FORM_VO = "consumableTypeFormVO";
	private static final String COST = "cost";
	private static final String OTHER_DESCRIPTION = "otherDescription";
	private static final String CONSUMABLE_COST_INVALID = "consumable.cost.invalid";
	public static final String CONSUMABLE_COST_REQUIRED = "consumable.cost.required";
	private static final String OTHER_DESC_INVALID = "consumable.otherDescription.invalid";
	private static final String OTHER_DESC_REQUIRED = "consumable.otherDescription.required";
	private static final String CONSUMABLE_REMOVE_OPTION_REQUIRED = "consumable.remove.option.required";

	public static final String ADD_ACTION = "ADD";
	public static final String ADVISOR_CHANGE_CONSUMABLE_PAGE = "advisor/consumables/changeConsumable";
	public static final String REMOVE_CONSUMABLE_PAGE = "advisor/consumables/removeConsumable";
	public static final String ADVISOR_ADD_CONSUMABLES_PAGE = "advisor/consumables/addConsumables";
	public static final String ADVISOR_CONSUMABLES_SUMMARY_PAGE = "advisor/consumables/consumablesSummary";
	public static final String CONSUMABLES_SELCECTION_PAGE = "advisor/consumables/consumables";

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final ConfigDataService configDataService;
	private final ConsumablesService consumablesService;
	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;
	private final MessageSource messageSource;

	public ConsumablesController(ConsumablesService consumablesService, FindStudentService findStudentService,
			ConfigDataService configDataService, MessageSource messageSource, ApplicationService applicationService) {

		this.consumablesService = consumablesService;
		this.findStudentService = findStudentService;
		this.configDataService = configDataService;
		this.messageSource = messageSource;
		this.applicationService = applicationService;

	}

	/**
	 * Initialise consumables
	 * 
	 * @param model
	 * @param keyDataVO     keydatavo
	 * @param bindingResult BindingResult
	 * @return HTML
	 * @throws Exception
	 */
	@PostMapping(INIT_CONSUMABLES_PATH)
	public String initConsumables(Model model, @RequestParam(value = ACTION, required = true) String action,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			BindingResult bindingResult) throws Exception {
		logger.info("init consumables call {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {
			if (action.equals("ADD_FROM_ALL_SUMMARY")) {
				model.addAttribute("BACK_ACTION", "BACK_TO_ALLOWANCES_SUMMARY");
			} else 
			if (action.equals("ADD_FROM_CONSU_SUMMARY")) {
				model.addAttribute("BACK_ACTION", "BACK_TO_CONSUMABLE_SUMMARY");
			} else {
				model.addAttribute("BACK_ACTION", "BACK_TO_DISABILITY_SUMMARY");
			}
			setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);
			setConsumablesCapIntheModel(model);
			deriveSkipConsumableLink(model, dsaApplicationNumber);
			populateModelData(model, bindingResult, new ConsumableTypeFormVO());
			logger.info("init Consumabels call completed");
			view = CONSUMABLES_SELCECTION_PAGE;
		}

		return view;
	}

	/**
	 * select consumables
	 * 
	 * @param model              the model
	 * @param action             the action
	 * @param redirectAttributes redirect attributes
	 * @param request            ServletResquests
	 * @param consumablesFormVO  ConsumablesFormVO
	 * @param bindingResult      BindingResult
	 * @return html view
	 * @throws Exception
	 */
	@PostMapping(SELECT_CONSUMABLES_PATH)
	public String selectConsumables(Model model, @RequestParam(value = ACTION, required = true) String action,
			final RedirectAttributes redirectAttributes, HttpServletRequest request,
			@Valid @ModelAttribute(name = CONSUMABLES_TYPE_FORM_VO) ConsumableTypeFormVO consumablesFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("select consumables call {}", action);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		List<String> allowedActions = Arrays.asList(DASHBOARD_ACTION, BACK_ACTION, SAVE_AND_CONTINUE_ACTION,
				SKIP_ACTION, "BACK_TO_DISABILITY_SUMMARY", "BACK_TO_ALLOWANCES_SUMMARY", "BACK_TO_CONSUMABLE_SUMMARY");
		String view = ERROR_PAGE;
		logger.info("selectConsumables call action {}, form data {}", action, consumablesFormVO);
		if (allowedActions.contains(action.toUpperCase())) {
			model.addAttribute("BACK_ACTION", consumablesFormVO.getBackAction());
			if (action.toUpperCase().equals(DASHBOARD_ACTION)) {
				return AllowancesHelper.showDashboardPage(request);
			}
			if (action.toUpperCase().equals(SKIP_ACTION)) {
				// Marking the status as started as the user skipped the consumables
				consumablesService.updateSection(consumablesFormVO.getDsaApplicationNumber(), Section.ALLOWANCES,
						SectionStatus.STARTED);
				return AllowancesHelper.initChooseQuote(request);
			}

			setKeyValuesToModel(model, consumablesFormVO.getDsaApplicationNumber(),
					consumablesFormVO.getStudentReferenceNumber());
			if (action.toUpperCase().equalsIgnoreCase(SAVE_AND_CONTINUE_ACTION)) {
				bindingResult = validateOtherCheckBox(bindingResult, consumablesFormVO);
				if (bindingResult.hasErrors()) {
					populateModelData(model, bindingResult, consumablesFormVO);
					model.addAttribute(STUDENT_FIRST_NAME, consumablesFormVO.getStudentFirstName());
					setConsumablesCapIntheModel(model);
					deriveSkipConsumableLink(model, consumablesFormVO.getDsaApplicationNumber());
					return CONSUMABLES_SELCECTION_PAGE;
				} else {
					AddConsumableFormVO addConsumableFormVO = new AddConsumableFormVO();
					List<String> itemCodes = consumablesFormVO.getConsumableItemCodes();
					List<ConsumableItemFormVO> consItemFormList = new ArrayList<ConsumableItemFormVO>();
					for (String code : itemCodes) {
						ConsumableItemFormVO item = new ConsumableItemFormVO();
						item.setConsumableItem(ConsumableItem.valueOf(code));
						if (item.getConsumableItem().equals(ConsumableItem.OTHER)) {
							item.setDescription(consumablesFormVO.getOtherDescription());
						}
						consItemFormList.add(item);
					}

					addConsumableFormVO.setConsumableItems(consItemFormList);

					addConsumableFormVO.setDsaApplicationNumber(consumablesFormVO.getDsaApplicationNumber());
					addConsumableFormVO.setStudentReferenceNumber(consumablesFormVO.getStudentReferenceNumber());
					model.addAttribute(ADD_CONSUMABLE_FORM_VO, addConsumableFormVO);
					logger.info("addConsumableFormVO {}", addConsumableFormVO);
					view = ADVISOR_ADD_CONSUMABLES_PAGE;
				}
			}
			if (action.toUpperCase().equalsIgnoreCase(BACK_ACTION)) {
				boolean hasConsumables = !consumablesService
						.getAllConsumableItems(consumablesFormVO.getDsaApplicationNumber()).isEmpty();
				if (hasConsumables) {
					view = AllowancesHelper.showConsumableSummary(request);
				} else {
					view = redirectToView(request, DISABILITY_DETAILS_PATH);
				}
			}
			if (action.toUpperCase().equalsIgnoreCase("BACK_TO_DISABILITY_SUMMARY")) {
				view = AllowancesHelper.disabilitiesSummary(request);
			}
			if (action.equals("BACK_TO_ALLOWANCES_SUMMARY")) {
				view = AllowancesHelper.showAllowancesSummary(request);
			}
			if (action.equals("BACK_TO_CONSUMABLE_SUMMARY")) {
				view = AllowancesHelper.showConsumableSummary(request);
			}
		} else {
			addErrorMessage(model, action, request);
		}
		logger.info("Redirecting to view:{}", view);

		return view;
	}

	/**
	 * To add Consumables
	 * 
	 * @param model               the model
	 * @param action              user action
	 * @param request             HttpServletRequest
	 * @param redirectAttributes  RedirectAttributes
	 * @param addConsumableFormVO VOAddConsumableFormVO
	 * @param bindingResult       BindingResult
	 * @return html view
	 * @throws IllegalAccessException
	 */
	@PostMapping(ADD_CONSUMABLES_PATH)
	public String addConsuambles(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request, RedirectAttributes redirectAttributes,
			@Valid AddConsumableFormVO addConsumableFormVO, BindingResult bindingResult) throws IllegalAccessException {
		logger.info("adding consumables {}", addConsumableFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		long dsaApplicationNumber = addConsumableFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = addConsumableFormVO.getStudentReferenceNumber();
		List<String> allowedActions = Arrays.asList(CANCEL_ACTION, DASHBOARD_ACTION, ADD_ACTION);
		if (allowedActions.contains(action.toUpperCase())
				&& hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber)) {
			if (action.toUpperCase().equals(DASHBOARD_ACTION)) {
				return AllowancesHelper.showDashboardPage(request);
			}
			if (action.toUpperCase().equals(CANCEL_ACTION)) {
				return AllowancesHelper.showConsumablesInitialPage(request);
			}

			if (action.toUpperCase().equals(ADD_ACTION)) {

				validateRequest(addConsumableFormVO.getConsumableItems(), bindingResult);
				if (!bindingResult.hasErrors()) {
					consumablesService.addConsumables(dsaApplicationNumber, studentReferenceNumber,
							addConsumableFormVO.getConsumableItems());
					// After adding the item, showing the Consumables Summary page
					view = AllowancesHelper.showConsumableSummary(request);

				} else {
					view = ADVISOR_ADD_CONSUMABLES_PAGE;
					model.addAttribute(ADD_CONSUMABLE_FORM_VO, addConsumableFormVO);
				}

			}
		} else {
			addErrorMessage(model, action, request);
		}

		return view;
	}

	/**
	 * initialising change action
	 * 
	 * @param model            Model
	 * @param consumableItemId
	 * @return html view
	 * @throws NumberFormatException
	 * @throws IllegalAccessException
	 */
	@PostMapping(path = { CHANGE_CONSUMABLE_PATH })
	public String inintChangeConsuambleItem(Model model, @RequestParam(value = ACTION, required = true) String action,
			String consumableItemId) throws NumberFormatException, IllegalAccessException {
		logger.info("initialising ChangeConsuambleItem item id {}", consumableItemId);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		ConsumableTypeVO consumableTypeVO = consumablesService.getConsumableItem(Long.valueOf(consumableItemId));
		ConsumableItemChangeFormVO consumableItemChangeFormVO = mapToFormData(consumableTypeVO);
		model.addAttribute(CONSUMABLE_ITEM_DATA, consumableItemChangeFormVO);
		model.addAttribute(CONSUMABLE_ITEM_CHANGE_FORM_VO, consumableItemChangeFormVO);

		if (action.equals("CHANGE_FROM_SUMMARY")) {
			model.addAttribute("CANCEL_ACTION", "CHANGE_FROM_SUMMARY");
		}

		setKeyValuesToModel(model, consumableTypeVO.getDsaApplicationNumber(),
				consumableTypeVO.getStudentReferenceNumber());
		return ADVISOR_CHANGE_CONSUMABLE_PAGE;
	}

	/**
	 * To change the consumable item
	 * 
	 * @param model              Model
	 * @param action             user action
	 * @param request            HttpServletRequest
	 * @param redirectAttributes RedirectAttributes
	 * @param consuItemFormVO    ConsumableItemChangeFormVO
	 * @param bindingResult      BindingResult
	 * @return html view
	 * @throws NumberFormatException
	 * @throws IllegalAccessException
	 */
	@PostMapping(path = { CHANGE_CONSUMABLE_ITEM_PATH })
	public String changeConsuambleItem(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request, RedirectAttributes redirectAttributes,
			@Valid ConsumableItemChangeFormVO consuItemFormVO, BindingResult bindingResult)
			throws NumberFormatException, IllegalAccessException {
		logger.info("changeConsumableItem call - action: {}, form data {}", action, consuItemFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		List<String> allowedActions = Arrays.asList(BACK_ACTION, CANCEL_ACTION, DASHBOARD_ACTION, CHANGE_ACTION,
				"CHANGE_FROM_SUMMARY");
		model.addAttribute("CANCEL_ACTION", CANCEL_ACTION);
		if (allowedActions.contains(action.toUpperCase())) {
			if (action.toUpperCase().equals(DASHBOARD_ACTION)) {
				return AllowancesHelper.showDashboardPage(request);
			}
			if (action.toUpperCase().equals("CHANGE_FROM_SUMMARY")) {
				return AllowancesHelper.showAllowancesSummary(request);
			}
			if (action.toUpperCase().equals(CANCEL_ACTION)) {
				// After Cancelling the change, showing the Consumables Summary page
				return AllowancesHelper.showConsumableSummary(request);
			}
			if (!StringUtils.hasText(consuItemFormVO.getCost())) {
				bindingResult.rejectValue(COST, CONSUMABLE_COST_REQUIRED,
						new Object[] { consuItemFormVO.getConsumableItem().getItemName() }, "");
			} else {
				String cost = sanitizeCost(bindingResult, messageSource, consuItemFormVO.getConsumableItem(),
						consuItemFormVO.getCost(), COST_REGEX, COST, CONSUMABLE_COST_INVALID);
				consuItemFormVO.setCost(cost);
			}
			if (!bindingResult.hasErrors()) {
				ConsumableTypeVO consumableItem = consumablesService.updateConsumableItem(consuItemFormVO);
				ConsumableItemChangeFormVO consumableItemChangeFormVO = mapToFormData(consumableItem);
				model.addAttribute(CONSUMABLE_ITEM_DATA, consumableItemChangeFormVO);
				model.addAttribute(CONSUMABLE_ITEM_CHANGE_FORM_VO, consumableItemChangeFormVO);
				view = AllowancesHelper.showConsumableSummary(request);
			} else {
				model.addAttribute(CONSUMABLE_ITEM_DATA, consuItemFormVO);
				ConsumableTypeVO consumableTypeVO = consumablesService
						.getConsumableItem(Long.valueOf(consuItemFormVO.getId()));
				setKeyValuesToModel(model, consumableTypeVO.getDsaApplicationNumber(),
						consumableTypeVO.getStudentReferenceNumber());
				view = ADVISOR_CHANGE_CONSUMABLE_PAGE;

			}
		} else {
			addErrorMessage(model, action, request);
		}

		return view;
	}

	/**
	 * Remove Consumable Item
	 * 
	 * @param model                      Model
	 * @param request                    HttpServletRequest
	 * @param action
	 * @param redirectAttributes         RedirectAttributes
	 * @param consumableItemRemoveFormVO ConsumableItemRemoveFormVO
	 * @param bindingResult              BindingResult
	 * @return html view
	 * @throws IllegalAccessException
	 */
	@PostMapping(REMOVE_CONSUMABLE_PATH)
	public String removeConsumableItem(Model model, HttpServletRequest request,
			@RequestParam(value = ACTION) String action, RedirectAttributes redirectAttributes,
			@ModelAttribute(name = CONSUMABLE_ITEM_REMOVE_FORM_VO) ConsumableItemRemoveFormVO consumableItemRemoveFormVO,
			BindingResult bindingResult) throws IllegalAccessException {
		logger.info("remove consumables call {}", action);

		hasMandatoryValues(model, consumableItemRemoveFormVO.getDsaApplicationNumber(),
				consumableItemRemoveFormVO.getStudentReferenceNumber());

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, CONSUMABLES_SUMMARY_ACTION);
		switch (action.toUpperCase()) {
		case "REMOVE_FROM_SUMMARY":
			model.addAttribute(CONSUMABLE_ITEM_REMOVE_FORM_VO, consumableItemRemoveFormVO);
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, DSAConstants.ALLOWANCES_SUMMARY_ACTION);
			view = REMOVE_CONSUMABLE_PAGE;
			break;
		case REMOVE_ACTION:
			model.addAttribute(CONSUMABLE_ITEM_REMOVE_FORM_VO, consumableItemRemoveFormVO);
			view = REMOVE_CONSUMABLE_PAGE;
			break;

		case DSAConstants.ALLOWANCES_SUMMARY_ACTION:

			view = AllowancesHelper.showAllowancesSummary(request);
			break;
		case CONSUMABLES_SUMMARY_ACTION:

			view = AllowancesHelper.showConsumableSummary(request);
			break;
		case DASHBOARD_ACTION:
			view = AllowancesHelper.showDashboardPage(request);
			break;
		case SAVE_AND_CONTINUE_ACTION:
			String removeItem = consumableItemRemoveFormVO.getRemoveItem();
			if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
				if (removeItem.equals(YesNoType.YES.name())) {
					consumablesService.deleteItem(Long.valueOf(consumableItemRemoveFormVO.getConsumableItemId()));
					ServiceUtil.updateSectionStatus(applicationService,
							consumableItemRemoveFormVO.getDsaApplicationNumber(), Section.ALLOWANCES,
							SectionStatus.STARTED);
				}
				view = AllowancesHelper.showConsumableSummary(request);
			} else {
				bindingResult.rejectValue(REMOVE_ITEM, CONSUMABLE_REMOVE_OPTION_REQUIRED);
				model.addAttribute(CONSUMABLE_ITEM_REMOVE_FORM_VO, consumableItemRemoveFormVO);
				view = REMOVE_CONSUMABLE_PAGE;
			}
			break;
		default:
			addErrorMessage(model, action, request);
			break;
		}

		return view;
	}

	@PostMapping(CONSUMABLES_SUMMARY_PATH)
	public String consumablesSummary(Model model, HttpServletRequest request,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			BindingResult bindingResult) throws Exception {

		logger.info("Loading summary page for the DSA Application number: {}", keyDataVO.getDsaApplicationNumber());

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		boolean hasValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());
		if (hasValues) {
			setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);
			setConsumablesSummaryData(model, consumablesService, configDataService,
					keyDataVO.getDsaApplicationNumber());
			view = ADVISOR_CONSUMABLES_SUMMARY_PAGE;
		}

		return view;
	}

	private void validateRequest(@Valid List<ConsumableItemFormVO> consumableItems, BindingResult bindingResult) {
		consumableItems.stream().forEach(consuItem -> {
			int itemIndex = consumableItems.indexOf(consuItem);
			validateCostValue(bindingResult, itemIndex, consuItem);
		});

	}

	private void validateCostValue(BindingResult bindingResult, int itemIndex, ConsumableItemFormVO consuItemFormVO) {
		String fieldName = "consumableItems[" + itemIndex + "].cost";
		if (!StringUtils.hasText(consuItemFormVO.getCost())) {
			bindingResult.rejectValue(fieldName, CONSUMABLE_COST_REQUIRED,
					new Object[] { consuItemFormVO.getConsumableItem().getItemName() }, "");

		} else {

			String cost = sanitizeCost(bindingResult, messageSource, consuItemFormVO.getConsumableItem(),
					consuItemFormVO.getCost(), COST_REGEX, fieldName, CONSUMABLE_COST_INVALID);
			consuItemFormVO.setCost(cost);
		}
	}

	private BindingResult validateOtherCheckBox(BindingResult bindingResult,
			@Valid ConsumableTypeFormVO consumableFormVo) {
		boolean hasErrors = bindingResult.hasErrors();
		if (!hasErrors) {
			String notListedText = consumableFormVo.getOtherDescription();
			boolean hasText = StringUtils.hasText(notListedText);
			boolean hasDisabilityNotListedCode = consumableFormVo.getConsumableItemCodes()
					.contains(ConsumableItem.OTHER.name());
			if (!hasText && hasDisabilityNotListedCode) {
				bindingResult.rejectValue(OTHER_DESCRIPTION, OTHER_DESC_REQUIRED);
			} else if (hasDisabilityNotListedCode && !matches(Pattern.compile(OTHER_TEXT_PATTERN), notListedText)) {
				bindingResult.rejectValue(OTHER_DESCRIPTION, OTHER_DESC_INVALID);
			}
		}
		return bindingResult;
	}

	private void setKeyValuesToModel(Model model, long dsaApplicationNUmber, long studentReferenceNumber) {
		model.addAttribute(DSAConstants.DSA_APPLICATION_NUMBER, dsaApplicationNUmber);
		model.addAttribute(DSAConstants.STUDENT_REFERENCE_NUMBER, studentReferenceNumber);
	}

	private void populateModelData(Model model, BindingResult bindingResult, ConsumableTypeFormVO consumableTypeForm) {

		model.addAttribute(CONSUMABLES_TYPE_FORM_VO, consumableTypeForm);
	}

	private void setConsumablesCapIntheModel(Model model) {
		model.addAttribute(CONSUMABLES_CAP, currencyLocalisation(getConsumableCap(configDataService)));
	}

	private void deriveSkipConsumableLink(Model model, long dsaApplicationNumber) {
		boolean noConsumableItems = consumablesService.getAllConsumableItems(dsaApplicationNumber).isEmpty();
		model.addAttribute(SHOW_SKIP_CONSUMABLES, noConsumableItems);
	}
}
