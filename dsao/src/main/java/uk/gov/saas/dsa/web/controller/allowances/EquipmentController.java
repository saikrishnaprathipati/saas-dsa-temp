package uk.gov.saas.dsa.web.controller.allowances;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.converters.LargeEquipmentPaymentTypeConverter;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.*;
import uk.gov.saas.dsa.service.allowances.EquipmentPaymentService;
import uk.gov.saas.dsa.service.allowances.EquipmentService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.ChooseQuoteOptionFormVO;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.equipment.AddEquipmentFormVO;
import uk.gov.saas.dsa.vo.equipment.AddEquipmentPaymentFormVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.equipment.RemoveItemFormVO;
import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

@Controller
public class EquipmentController {
	private final ConfigDataService configDataService;
	private final EquipmentService equipmentService;
	private final EquipmentPaymentService equipmentPaymentService;
	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;
	private final QuoteUploadService quoteUploadService;
	private final CourseDetailsService courseDetailsService;
	public static final String ADD_EQUIPMENT_PATH = "addEquipment";
	public static final String CHOOSE_QUOTE_PATH = "chooseQuoteOption";
	public static final String ADD_QUOTE_PATH = "addQuoteOption";
	public static final String INIT_CHANGE_EQUIPMENT_ACTION = "INIT_CHANGE_EQUIPMENT";
	public static final String CHANGE_EQUIPMENT_PATH = "changeEquipment";
	public static final String CHANGE_EQUIPMENT_FORM_VO = "changeEquipmentFormVO";
	public static final String ADD_ACTION = "ADD";
	public static final String BACK_TO_CHOOSE_QUOTE = "BACK_TO_CHOOSE_QUOTE";
	public static final String ADD_EQUIPMENT_FORM_VO = "addEquipmentFormVO";

	public static final String ADD_PAYMENT_FOR_FORM_VO = "addEquipmentPaymentFormVO";

	public static final String CHOOSE_QUOTE_EQUIPMENT_FORM_VO = "chooseQuoteOptionFormVO";
	public static final String QUOTE_DETAILS_FORM_VO = "quoteDetailsFormVO";

	private static final String EQUIPMENT_FIELD_INVALID = "equipment.%s.invalid";
	private static final String PRODUCT_NAME = "productName";
	private static final String DESCRIPTION = "description";
	private static final String COST = "cost";
	public static final String ADD_EQUIPMENT_FROM_CONSUMABLES_ACTION = "ADD_EQUIPMENT_FROM_CONSUMABLES";
	public static final String ADD_EQUIPMENT_FROM_SUMMARY_ACTION = "ADD_EQUIPMENT_FROM_SUMMARY";
	public static final String EQUIPMENT_SUMMARY_PATH = "equipmentSummary";
	public static final String ADVISOR_ADD_EQUIPMENT_PAGE = "advisor/equipment/addEquipment";
	public static final String ADVISOR_CHOOSE_QUOTE_OPTION_PAGE = "advisor/equipment/chooseQuoteOption";
	public static final String ADVISOR_UPLOAD_QUOTE_PAGE = "advisor/quote/addQuote";
	public static final String ADVISOR_CHANGE_EQUIPMENT_PAGE = "advisor/equipment/changeEquipment";
	public static final String ADVISOR_REMOVE_EQUIPMENT = "advisor/equipment/removeEquipment";
	public static final String ADVISOR_EQUIPMENT_SUMMARY_PAGE = "advisor/equipment/equipmentSummary";

	public static final String REMOVE_EQUIPMENT_PATH = "removeEquipment";
	public static final String REMOVE_ITEM_FORM_VO = "removeItemFormVO";
	public static final String ALLOWANCES_REMOVE_OPTION_REQUIRED = "allowances.remove.option.required";
	public static final String QUOTES_LIMIT_REACHED = "quotes.limit.reached";
	public static final String SHOW_SKIP_EQUIPMENT = "showSkipEquipment";
	private static final String SKIP_EQUIPMENT = "SKIP_EQUIPMENT";
	private final Logger logger = LogManager.getLogger(this.getClass());

	public EquipmentController(EquipmentService equipmentService, EquipmentPaymentService equipmentPaymentService,
			ConfigDataService configDataService, FindStudentService findStudentService,
			ApplicationService applicationService, QuoteUploadService quoteUploadService,
							   CourseDetailsService courseDetailsService) {
		this.equipmentService = equipmentService;
		this.equipmentPaymentService = equipmentPaymentService;
		this.configDataService = configDataService;
		this.findStudentService = findStudentService;
		this.applicationService = applicationService;
		this.quoteUploadService = quoteUploadService;
		this.courseDetailsService = courseDetailsService;
	}

	@PostMapping("addPayment")
	public String addPaymentFor(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
			@Valid @ModelAttribute(name = ADD_PAYMENT_FOR_FORM_VO) AddEquipmentPaymentFormVO paymentForm,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IllegalAccessException {
		if (action.equalsIgnoreCase("Back")) {
			return AllowancesHelper.showConsumableSummary(request);
		}
		if (action.equalsIgnoreCase(BACK_TO_CHOOSE_QUOTE)) {
			return redirectToView(request, CHOOSE_QUOTE_PATH);
		}

		String paymentForItem = paymentForm.getPaymentForItem();

		LoggedinUserUtil.setLoggedinUserInToModel(model);
		hasMandatoryValues(model, paymentForm.getDsaApplicationNumber(), paymentForm.getStudentReferenceNumber());

		LargeEquipmentPaymentTypeConverter c = new LargeEquipmentPaymentTypeConverter();
		LargeEquipmentPaymentType selectedType = c.convertToEntityAttribute(paymentForItem);

		if (selectedType == null) {
			bindingResult.rejectValue("paymentForItem", ALLOWANCES_REMOVE_OPTION_REQUIRED);
			model.addAttribute(ADD_PAYMENT_FOR_FORM_VO, paymentForm);
			return redirectToView(request, EQUIPMENT_SUMMARY_PATH);
		} else {
			equipmentPaymentService.createPaymentFor(paymentForm.getDsaApplicationNumber(), selectedType);
		}
		if (action.equalsIgnoreCase("change_from_allowances_summary")) {
			return AllowancesHelper.showAllowancesSummary(request);
		}
		return AllowancesHelper.initAddNMPH(request);

	}

	/**
	 * Add Equipment and other large items
	 *
	 * @param model              Model
	 * @param action             Action
	 * @param request            HTTP Request
	 * @param addEquipmentFormVO Form VO
	 * @param bindingResult      Binding Result
	 * @param redirectAttributes Redirect Attributes
	 * @return HTML View
	 * @throws Exception Generic Exception
	 */
	@PostMapping(ADD_EQUIPMENT_PATH)
	public String addEquipment(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
			@Valid @ModelAttribute(name = ADD_EQUIPMENT_FORM_VO) AddEquipmentFormVO addEquipmentFormVO,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("adding equipment {}, action {}", addEquipmentFormVO, action);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = addEquipmentFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = addEquipmentFormVO.getStudentReferenceNumber();
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case ADD_EQUIPMENT_FROM_CONSUMABLES_ACTION:
			case SKIP_ACTION:
				boolean hasEquipmentAllowances = !equipmentService
						.getAllEquipmentAllowances(addEquipmentFormVO.getDsaApplicationNumber()).isEmpty();
				if (hasEquipmentAllowances) {
					return showEquipmentSummary(request);
				} else {
					return initialiseAddEquipmentPage(model, dsaApplicationNumber, studentReferenceNumber);
				}
			case SKIP_EQUIPMENT:
				return AllowancesHelper.initAddNMPH(request);
			case ADD_EQUIPMENT_FROM_SUMMARY_ACTION:
				List<EquipmentAllowanceVO> equipmentItems = equipmentService
						.getAllEquipmentAllowances(dsaApplicationNumber);
				if (EQUIPMENT_ITEMS_LIMIT <= equipmentItems.size()) {
					logger.info("You have reached the limit of equipment items you can add!");
					addErrorMessage(model, action, request);
				} else {
					return initialiseAddEquipmentPage(model, dsaApplicationNumber, studentReferenceNumber);
				}
			case DASHBOARD_ACTION:
				return redirectToView(request, APPLICATION_DASHBOARD_PATH);
			case CANCEL_ACTION:
				return redirectToView(request, EQUIPMENT_SUMMARY_PATH);
			case ADD_ACTION:
				return performAction(model, addEquipmentFormVO, bindingResult, dsaApplicationNumber,
						studentReferenceNumber, ADD_ACTION, request, redirectAttributes);
			case BACK_TO_CHOOSE_QUOTE:
				return redirectToView(request, CHOOSE_QUOTE_PATH);
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}

		return ERROR_PAGE;
	}

	@GetMapping(CHOOSE_QUOTE_PATH)
	public String redirectToChooseQuoteOption(Model model, HttpSession httpsession) throws Exception {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		ChooseQuoteOptionFormVO chooseQuoteOptionFormVO = (ChooseQuoteOptionFormVO) httpsession
				.getAttribute("chooseQuoteOptionFormVO");
		logger.info("Choose Quote Option {} and model {}", model, chooseQuoteOptionFormVO);

		long dsaApplicationNumber = chooseQuoteOptionFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = chooseQuoteOptionFormVO.getStudentReferenceNumber();
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		if (hasMandatoryValues) {
			setStudentDetailsInTheModel(model, chooseQuoteOptionFormVO.getStudentReferenceNumber(), findStudentService);
			setEquipmentCap(model);
			setSkipEquipmentLink(model, dsaApplicationNumber);
			model.addAttribute(CHOOSE_QUOTE_EQUIPMENT_FORM_VO, chooseQuoteOptionFormVO);
			return ADVISOR_CHOOSE_QUOTE_OPTION_PAGE;
		}
		return ERROR_PAGE;
	}

	@PostMapping(CHOOSE_QUOTE_PATH)
	public String chooseQuoteOption(Model model, @RequestParam(value = ACTION) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = CHOOSE_QUOTE_EQUIPMENT_FORM_VO) ChooseQuoteOptionFormVO chooseQuoteOptionFormVO,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("Choose Quote Option {}, action {}", chooseQuoteOptionFormVO, action);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = chooseQuoteOptionFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = chooseQuoteOptionFormVO.getStudentReferenceNumber();
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		if (hasMandatoryValues) {
			List<EquipmentAllowanceVO> equipmentItems = equipmentService
					.getAllEquipmentAllowances(dsaApplicationNumber);
			if (EQUIPMENT_ITEMS_LIMIT <= equipmentItems.size()) {
				logger.info("You have reached the limit of equipment items you can add!");
				addErrorMessage(model, action, request);
			} else {
				setStudentDetailsInTheModel(model, chooseQuoteOptionFormVO.getStudentReferenceNumber(),
						findStudentService);
				setEquipmentCap(model);
				setSkipEquipmentLink(model, dsaApplicationNumber);
				model.addAttribute(CHOOSE_QUOTE_EQUIPMENT_FORM_VO, chooseQuoteOptionFormVO);
				return ADVISOR_CHOOSE_QUOTE_OPTION_PAGE;
			}
		}
		return ERROR_PAGE;
	}

	@PostMapping(ADD_QUOTE_PATH)
	public String addQuoteOption(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
			@Valid @ModelAttribute(name = CHOOSE_QUOTE_EQUIPMENT_FORM_VO) ChooseQuoteOptionFormVO chooseQuoteOptionFormVO,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("Add quote option {}, action {}", chooseQuoteOptionFormVO, action);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = chooseQuoteOptionFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = chooseQuoteOptionFormVO.getStudentReferenceNumber();
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		DSAApplicationsMade dsaApplication = applicationService
				.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber, studentReferenceNumber);
		chooseQuoteOptionFormVO.setSessionCode(dsaApplication.getSessionCode());

		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case SKIP_EQUIPMENT:
				return AllowancesHelper.initAddNMPH(request);
			case BACK_ACTION:
				return showConsumableSummary(request);
			case DASHBOARD_ACTION:
				return redirectToView(request, APPLICATION_DASHBOARD_PATH);
			case CONFIRM_AND_CONTINUE_ACTION:
				String chooseQuote = chooseQuoteOptionFormVO.getUseQuote();
				StudentResultVO studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber);
				model.addAttribute(STUDENT_FIRST_NAME, studentResultVO.getFirstName());
				setEquipmentCap(model);
				setSkipEquipmentLink(model, dsaApplicationNumber);

				if (AllowancesHelper.chooseQuoteHasCorrectValue(chooseQuote)) {
					if (chooseQuote.equals(YesNoType.YES.name())) {
						QuoteDetailsFormVO quoteDetailsFormVO = mapQuoteOptionFormVO(model, dsaApplicationNumber,
								studentResultVO, dsaApplication.getSessionCode());

						//  Maximum QUOTE_ITEMS_LIMIT quotes
						List<QuoteResultVO> quotes = quoteUploadService.fetchAllQuotesForStudentApplication(quoteDetailsFormVO.getDsaApplicationNumber());
						if( QUOTE_ITEMS_LIMIT <= quotes.size()){
							bindingResult.rejectValue("useQuote", QUOTES_LIMIT_REACHED);
							model.addAttribute(CHOOSE_QUOTE_EQUIPMENT_FORM_VO, chooseQuoteOptionFormVO);
							return ADVISOR_CHOOSE_QUOTE_OPTION_PAGE;
						}

						model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
						return ADVISOR_UPLOAD_QUOTE_PAGE;
					} else {
						return initialiseAddEquipmentPage(model, dsaApplicationNumber, studentReferenceNumber);
					}
				} else {
					bindingResult.rejectValue("useQuote", ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(CHOOSE_QUOTE_EQUIPMENT_FORM_VO, chooseQuoteOptionFormVO);
					return ADVISOR_CHOOSE_QUOTE_OPTION_PAGE;
				}
			default:
				addErrorMessage(model, action, request);
				break;
			}

		}
		return ERROR_PAGE;
	}

	/**
	 * @param model         Model
	 * @param request       Servlet Request
	 * @param keyDataVO     Application Key Data Form VO
	 * @param bindingResult Biding Result
	 * @return HTML View
	 * @throws Exception Generic Exception
	 */
	@PostMapping(EQUIPMENT_SUMMARY_PATH)
	public String equipmentSummary(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			BindingResult bindingResult) throws Exception {
		logger.info("equipment Path: {}, form data: {}", EQUIPMENT_SUMMARY_PATH, keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());

		if (hasValues) {
			int currentPage = page.orElse(1);
			int pageSize = size.orElse(PAGINATION_SIZE);
			StudentResultVO studentResultVO = setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(),
					findStudentService);
			DSAApplicationsMade dsaApplication = equipmentPaymentService
					.findByDsaApplicationNumberAndStudentReferenceNumber(keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
			QuoteDetailsFormVO quoteDetailsFormVO = mapQuoteOptionFormVO(model, keyDataVO.getDsaApplicationNumber(),
					studentResultVO, dsaApplication.getSessionCode());
			setEquipmentSummaryData(model, equipmentService, configDataService, quoteUploadService,
					keyDataVO.getDsaApplicationNumber(), currentPage, pageSize);

			if (action != null && action.equals("change_from_allowances_summary")) {
				model.addAttribute("actionName", "change_from_allowances_summary");
			}

			setQuoteSummaryData(model, quoteUploadService, quoteDetailsFormVO, currentPage, pageSize, configDataService);
			AllowancesHelper.setEquipmentPaymentForData(model, equipmentPaymentService, configDataService,
					keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
			AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
					keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
			CourseDetailsVO courseDetails;
			String institutionName = " ";
			try {
				courseDetails = courseDetailsService.findCourseDetailsFromDB(keyDataVO.getStudentReferenceNumber(),
						dsaApplication.getSessionCode());
				institutionName = " " + courseDetails.getInstitutionName() + " ";
			} catch (IllegalAccessException e) {
				logger.info(e.getMessage());
			}
			model.addAttribute("institutionName", institutionName);

			return ADVISOR_EQUIPMENT_SUMMARY_PAGE;
		}
		return ERROR_PAGE;
	}

	/**
	 * @param model                 Model
	 * @param request               Servlet Request
	 * @param action                User Action
	 * @param redirectAttributes    Redirect Attributes
	 * @param changeEquipmentFormVO Change Form Data
	 * @param bindingResult         Binding Result
	 * @return HTML View
	 * @throws Exception Generic Exception
	 */
	@PostMapping(path = { CHANGE_EQUIPMENT_PATH })
	public String changeEquipmentItem(Model model, HttpServletRequest request,
			@RequestParam(value = ACTION) String action, RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute(name = CHANGE_EQUIPMENT_FORM_VO) AddEquipmentFormVO changeEquipmentFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("Change equipment allowances call action: {}, request: {}", action, changeEquipmentFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = changeEquipmentFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = changeEquipmentFormVO.getStudentReferenceNumber();
		hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		switch (action.toUpperCase()) {
		case INIT_CHANGE_EQUIPMENT_ACTION:
			EquipmentAllowanceVO item = equipmentService.getEquipmentItem(changeEquipmentFormVO.getId());
			changeEquipmentFormVO = AllowancesHelper.mapToEquipmentAllowanceFormVO(item);
			model.addAttribute(CHANGE_EQUIPMENT_FORM_VO, changeEquipmentFormVO);
			setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
			setEquipmentCap(model);
			setSkipEquipmentLink(model, dsaApplicationNumber);

			return ADVISOR_CHANGE_EQUIPMENT_PAGE;
		case CHANGE_ACTION:
			return performAction(model, changeEquipmentFormVO, bindingResult, dsaApplicationNumber,
					studentReferenceNumber, CHANGE_ACTION, request, redirectAttributes);
		case CANCEL_ACTION:
			return showEquipmentSummary(request);
		case DASHBOARD_ACTION:
			return redirectToView(request, APPLICATION_DASHBOARD_PATH);
		default:
			addErrorMessage(model, action, request);
			break;
		}

		return ERROR_PAGE;
	}

	/**
	 * Remove equipment
	 *
	 * @param model              Model
	 * @param request            Servlet Request
	 * @param action             user acton
	 * @param redirectAttributes redirect attributes
	 * @param removeItemFormVO   form data
	 * @param bindingResult      binding result
	 * @return HTML View
	 * @throws IllegalAccessException Illegal Access Exception
	 */

	@PostMapping(REMOVE_EQUIPMENT_PATH)
	public String removeEquipmentItem(Model model, HttpServletRequest request,
			@RequestParam(value = ACTION) String action, RedirectAttributes redirectAttributes,
			@ModelAttribute(name = REMOVE_ITEM_FORM_VO) RemoveItemFormVO removeItemFormVO, BindingResult bindingResult)
			throws IllegalAccessException {
		logger.info("Remove equipment allowances call action: {}, request: {}", action, removeItemFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());

		switch (action.toUpperCase()) {
		case REMOVE_ACTION:
			model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
			return ADVISOR_REMOVE_EQUIPMENT;
		case BACK_ACTION:
			return showEquipmentSummary(request);
		case DASHBOARD_ACTION:
			return redirectToView(request, APPLICATION_DASHBOARD_PATH);
		case SAVE_AND_CONTINUE_ACTION:
			String removeItem = removeItemFormVO.getRemoveItem();

			if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
				if (removeItem.equals(YesNoType.YES.name())) {
					equipmentService.deleteItem(removeItemFormVO.getItemId());
					ServiceUtil.updateSectionStatus(applicationService, removeItemFormVO.getDsaApplicationNumber(),
							Section.ALLOWANCES, SectionStatus.STARTED);
				}
				return showEquipmentSummary(request);
			} else {
				bindingResult.rejectValue(REMOVE_ITEM, ALLOWANCES_REMOVE_OPTION_REQUIRED);
				model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
				return ADVISOR_REMOVE_EQUIPMENT;
			}
		default:
			addErrorMessage(model, action, request);
			break;
		}

		return ERROR_PAGE;
	}

	private String performAction(Model model, AddEquipmentFormVO formVO, BindingResult bindingResult,
			long dsaApplicationNumber, long studentReferenceNumber, String operation, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws Exception {
		validateEquipmentFormData(formVO, bindingResult);
		setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);


		if (bindingResult.hasErrors()) {
			setEquipmentCap(model);
			setSkipEquipmentLink(model, dsaApplicationNumber);
			if (operation.equalsIgnoreCase(ADD_ACTION)) {
				model.addAttribute(ADD_EQUIPMENT_FORM_VO, formVO);
				return ADVISOR_ADD_EQUIPMENT_PAGE;
			} else {
				model.addAttribute(CHANGE_EQUIPMENT_FORM_VO, formVO);
				return ADVISOR_CHANGE_EQUIPMENT_PAGE;
			}
		} else {
			EquipmentAllowanceVO equipmentAllowanceVO = mapToEquipmentAllowanceVO(formVO);
			if (operation.equalsIgnoreCase(ADD_ACTION)) {
				equipmentService.addEquipmentAllowance(equipmentAllowanceVO);
			} else {
				equipmentService.changeEquipment(equipmentAllowanceVO);
			}
			return showEquipmentSummary(request);
		}
	}

	private String initialiseAddEquipmentPage(Model model, long dsaApplicationNumber, long studentReferenceNumber)
			throws Exception {
		setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
		setEquipmentCap(model);
		setSkipEquipmentLink(model, dsaApplicationNumber);

		model.addAttribute(ADD_EQUIPMENT_FORM_VO, new AddEquipmentFormVO());
		return ADVISOR_ADD_EQUIPMENT_PAGE;
	}

	private void setEquipmentCap(Model model) {
		model.addAttribute(EQUIPMENT_CAP, currencyLocalisation(AllowancesHelper.getEquipmentCap(configDataService)));
	}

	private void validateEquipmentFormData(AddEquipmentFormVO addEquipmentFormVO, BindingResult bindingResult) {
		ValidationHelper.validateFieldValue(bindingResult, PRODUCT_NAME, addEquipmentFormVO.getProductName(),
				EQUIPMENT_FIELD_INVALID);

		// Description optional
		boolean hasText = StringUtils.hasText(addEquipmentFormVO.getDescription());
		if (hasText) {
			ValidationHelper.validateFieldValue(bindingResult, DESCRIPTION, addEquipmentFormVO.getDescription(),
					EQUIPMENT_FIELD_INVALID);
		}

		if (!bindingResult.hasFieldErrors(COST)) {
			addEquipmentFormVO
					.setCost(AllowancesHelper.validateNumber(bindingResult, COST, addEquipmentFormVO.getCost(), COST,
							String.format(EQUIPMENT_FIELD_INVALID, COST), NumberType.COST_XXXXX_YY, COST_REGEX));
		}
	}

	private void setSkipEquipmentLink(Model model, long dsaApplicationNumber) {
		boolean noEquipment = equipmentService.getAllEquipmentAllowances(dsaApplicationNumber).isEmpty();
		model.addAttribute(SHOW_SKIP_EQUIPMENT, noEquipment);
	}
}
