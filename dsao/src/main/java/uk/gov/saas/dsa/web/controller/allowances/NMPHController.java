package uk.gov.saas.dsa.web.controller.allowances;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.mapToNMPHAllowanceVO;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setNMPHSummaryData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setStudentDetailsInTheModel;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.showNMPHSummary;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.service.allowances.NMPHAllowancesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.nmph.AddNMPHFormVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

/**
 * The NMPH controller
 */
@Controller
public class NMPHController {
	public static final String INT_CHANGEN_MPH_ACTION = "INT_CHANGEN_MPH";
	public static final String ADD_NMPH_FROM_EQUIPMENT_ACTION = "ADD_NMPH_FROM_EQUIPMENT";
	public static final String ADD_NMPH_FROM_SUMMARY_ACTION = "ADD_NMPH_FROM_SUMMARY";
	public static final String ADD_NMPH_FROM_CONSUMABLES_ACTION = "ADD_NMPH_FROM_CONSUMABLES";
	private static final String ADD = "ADD";
	private static final String CHANGE = "CHANGE";
	public static final String CHANGE_NMPH_PATH = "changeNMPH";
	public static final String REMOVE_NMPH_PATH = "removeNMPH";

	public static final String ADVISOR_NMPH_ALLOWANCES_PAGE = "advisor/nmph/nmphAllowances";
	public static final String ADVISOR_CHANGE_NMPH_PAGE = "advisor/nmph/changeNMPH";
	public static final String ADVISOR_REMOVE_NMPH_PAGE = "advisor/nmph/removeNMPH";
	public static final String ADVISOR_NMPH_SUMMARY_PAGE = "advisor/nmph/nmphSummary";

	public static final String REMOVE_ITEM_FORM_VO = "removeItemFormVO";
	public static final String CHANGE_NMPH_FORM_VO = "changeNMPHFormVO";
	private static final String NMPH_FIELD_INVALID = "nmph.%s.invalid";
	private static final String HOURS = "hours";

	private static final String HOURLY_RATE = "hourlyRate";

	private static final String HOURLY_RATE_NAME = "hourly rate";

	private static final String TYPE_OF_SUPPORT = "typeOfSupport";

	private static final String RECOMMENDED_PROVIDER = "recommendedProvider";

	public static final String ADD_NMPH_ALLOWANCE_PATH = "addNMPHAllowance";

	public static final String ADD_NMPH_FORM_VO = "addNMPHFormVO";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final ConfigDataService configDataService;
	private final NMPHAllowancesService nmphAllowancesService;

	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;
	public static final String SHOW_SKIP_NMPH = "showSkipNMPH";

	public NMPHController(NMPHAllowancesService nmphAllowancesService, FindStudentService findStudentService,
			ConfigDataService configDataService, ApplicationService applicationService) {

		this.nmphAllowancesService = nmphAllowancesService;
		this.findStudentService = findStudentService;
		this.configDataService = configDataService;
		this.applicationService = applicationService;

	}

	/**
	 * Add NMPH
	 *
	 * @param model              the model
	 * @param action             user action from the HTMNL
	 * @param request            Servlet request
	 * @param addNMPHFormVO      form data
	 * @param bindingResult      binding result
	 * @param redirectAttributes Redirect attributes
	 * @return html view
	 * @throws Exception
	 */
	@PostMapping(ADD_NMPH_ALLOWANCE_PATH)
	public String addNMPH(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request, @Valid @ModelAttribute(name = ADD_NMPH_FORM_VO) AddNMPHFormVO addNMPHFormVO,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("in add nmph allowances call action: {}, request: {}", action, addNMPHFormVO);

		if (securityContext() == null) { return LOGIN; }
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = addNMPHFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = addNMPHFormVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {

			switch (action.toUpperCase()) {

			case ADD_NMPH_FROM_EQUIPMENT_ACTION:
			case SKIP_ACTION:
			case "SKIP_EQUIPMENT":
				boolean hasNMPHAllowances = !nmphAllowancesService
						.getAllNMPHAllowances(addNMPHFormVO.getDsaApplicationNumber()).isEmpty();
				if (hasNMPHAllowances) {
					view = showNMPHSummary(request);
				} else {
					view = initialiseAddNMPHPage(model, dsaApplicationNumber, studentReferenceNumber);
				}
				break;
			case "SKIP_NMPH":
				view = AllowancesHelper.initTravelExp(request);
				break;
			case ADD_NMPH_FROM_SUMMARY_ACTION:
				view = initialiseAddNMPHPage(model, dsaApplicationNumber, studentReferenceNumber);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case BACK_ACTION:
				view = AllowancesHelper.showEquipmentSummary(request);
				break;
			case SAVE_AND_CONTINUE_ACTION:
				view = performAction(model, addNMPHFormVO, bindingResult, dsaApplicationNumber, studentReferenceNumber,
						ADD, request);
				break;
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}
		logger.info("View or redirecting to {}", view);
		return view;
	}

	/**
	 * Display the NMPH summary list
	 *
	 * @param model         model
	 * @param request       servlet request
	 * @param keyDataVO     form data
	 * @param bindingResult binding result
	 * @return html view.
	 * @throws Exception
	 */
	@PostMapping(NMPH_SUMMARY_PATH)
	public String nmphSummary(Model model, HttpServletRequest request,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			BindingResult bindingResult) throws Exception {
		logger.info("nmphSummary Path: {}, form data: {}", NMPH_SUMMARY_PATH, keyDataVO);

		if (securityContext() == null) { return LOGIN; }
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		boolean hasValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());
		if (hasValues) {
			setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);
			setNMPHSummaryData(model, nmphAllowancesService, configDataService, keyDataVO.getDsaApplicationNumber());
			view = ADVISOR_NMPH_SUMMARY_PAGE;
		}
		logger.info("View or redirecting to {}", view);
		return view;

	}

	/**
	 * Change nmph item
	 *
	 * @param model              the model
	 * @param request            servlet request
	 * @param action             user action
	 * @param redirectAttributes redirect attributes
	 * @param changeNMPHFormVO   change from data
	 * @param bindingResult      binding result
	 * @return html view
	 * @throws Exception
	 */
	@PostMapping(path = { CHANGE_NMPH_PATH })
	public String changeNMPHItem(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
			RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute(name = CHANGE_NMPH_FORM_VO) AddNMPHFormVO changeNMPHFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("in change nmph allowances call action: {}, request: {}", action, changeNMPHFormVO);

		if (securityContext() == null) { return LOGIN; }
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		model.addAttribute(DSAConstants.CANCEL_BUTTON_ACTION, DSAConstants.NMPH_SUMMARY_ACTION);
		long dsaApplicationNumber = changeNMPHFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = changeNMPHFormVO.getStudentReferenceNumber();
		hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (action.toUpperCase().equals("CHANGE_FROM_SUMMARY")) {
			model.addAttribute("CANCEL_ACTION", "CHANGE_FROM_SUMMARY");
		}
		switch (action.toUpperCase()) {
		case DSAConstants.ALLOWANCES_SUMMARY_ACTION:
			view = AllowancesHelper.showAllowancesSummary(request);
			break;
		case INT_CHANGEN_MPH_ACTION:
			model.addAttribute("CANCEL_ACTION", "CHANGE_FROM_SUMMARY");
			NMPHAllowanceVO item = nmphAllowancesService.getNMPHItem(changeNMPHFormVO.getId());
			changeNMPHFormVO = AllowancesHelper.mapToNMPHAllowanceFormVo(item);
			model.addAttribute(CHANGE_NMPH_FORM_VO, changeNMPHFormVO);

			setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);

			setNMPHCap(model);
			setSkipNMPHLink(model, dsaApplicationNumber);

			view = ADVISOR_CHANGE_NMPH_PAGE;
			break;

		case CHANGE_ACTION:
			view = performAction(model, changeNMPHFormVO, bindingResult, dsaApplicationNumber, studentReferenceNumber,
					CHANGE, request);
			break;
		case CANCEL_ACTION:
		case "CHANGE_FROM_SUMMARY":
			view = showNMPHSummary(request);
			break;
		case DASHBOARD_ACTION:
			view = AllowancesHelper.showDashboardPage(request);
			break;
		default:
			addErrorMessage(model, action, request);
			break;
		}
		logger.info("View or redirecting to {}", view);
		return view;
	}

	/**
	 * Remove nmph
	 *
	 * @param model            the model
	 * @param request          servlet request
	 * @param action           user acton
	 * @param removeItemFormVO form data
	 * @param bindingResult    binding result
	 * @return html view
	 * @throws IllegalAccessException
	 */

	@PostMapping(REMOVE_NMPH_PATH)
	public String removeNMPHItem(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
			@ModelAttribute(name = REMOVE_ITEM_FORM_VO) RemoveItemFormVO removeItemFormVO, BindingResult bindingResult)
			throws IllegalAccessException {
		logger.info("in Remove nmph allowances call action: {}, request: {}", action, removeItemFormVO);

		if (securityContext() == null) { return LOGIN; }
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());

		String view = ERROR_PAGE;
		model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, DSAConstants.BACK_ACTION);
		switch (action.toUpperCase()) {
		case "REMOVE_FROM_SUMMARY":
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, DSAConstants.ALLOWANCES_SUMMARY_ACTION);
			model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
			view = ADVISOR_REMOVE_NMPH_PAGE;
			break;

		case REMOVE_ACTION:
			model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
			view = ADVISOR_REMOVE_NMPH_PAGE;
			break;
		case DSAConstants.ALLOWANCES_SUMMARY_ACTION:
			view = AllowancesHelper.showAllowancesSummary(request);
			break;
		case BACK_ACTION:
			view = showNMPHSummary(request);
			break;
		case DASHBOARD_ACTION:
			view = AllowancesHelper.showDashboardPage(request);
			break;
		case SAVE_AND_CONTINUE_ACTION:
			String removeItem = removeItemFormVO.getRemoveItem();
			if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
				if (removeItem.equals(YesNoType.YES.name())) {
					nmphAllowancesService.deleteItem(Long.valueOf(removeItemFormVO.getItemId()));
					ServiceUtil.updateSectionStatus(applicationService, removeItemFormVO.getDsaApplicationNumber(),
							Section.ALLOWANCES, SectionStatus.STARTED);
				}
				view = showNMPHSummary(request);
			} else {
				bindingResult.rejectValue(REMOVE_ITEM, ALLOWANCES_REMOVE_OPTION_REQUIRED);
				model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
				view = ADVISOR_REMOVE_NMPH_PAGE;
			}
			break;
		default:
			addErrorMessage(model, action, request);
			break;
		}

		return view;
	}

	private void validateNMPHFormData(AddNMPHFormVO nmphFormVO, BindingResult bindingResult) {
		ValidationHelper.validateFieldValue(bindingResult, RECOMMENDED_PROVIDER, nmphFormVO.getRecommendedProvider(),
				NMPH_FIELD_INVALID);
		ValidationHelper.validateFieldValue(bindingResult, TYPE_OF_SUPPORT, nmphFormVO.getTypeOfSupport(),
				NMPH_FIELD_INVALID);
		if (!bindingResult.hasFieldErrors(HOURLY_RATE)) {
			nmphFormVO.setHourlyRate(AllowancesHelper.validateNumber(bindingResult, HOURLY_RATE_NAME,
					nmphFormVO.getHourlyRate(), HOURLY_RATE, String.format(NMPH_FIELD_INVALID, HOURLY_RATE),
					NumberType.COST_XXXXX_YY, COST_REGEX));
		}

		if (!bindingResult.hasFieldErrors(HOURS)) {
			nmphFormVO.setHours(AllowancesHelper.validateNumber(bindingResult, HOURS, nmphFormVO.getHours(), HOURS,
					String.format(NMPH_FIELD_INVALID, HOURS), NumberType.HOURS, TWO_DIG_NO_REGEX));
		}

	}

	private void setNMPHCap(Model model) {
		model.addAttribute(NMPH_CAP, currencyLocalisation(AllowancesHelper.getNMPHCap(configDataService)));
	}

	private void setSkipNMPHLink(Model model, long dsaApplicationNumber) {
		boolean noNMPHItems = nmphAllowancesService.getAllNMPHAllowances(dsaApplicationNumber).isEmpty();
		model.addAttribute(SHOW_SKIP_NMPH, noNMPHItems);
	}

	private String performAction(Model model, AddNMPHFormVO formVO, BindingResult bindingResult,
			long dsaApplicationNumber, long studentReferenceNumber, String operation, HttpServletRequest request)
			throws Exception {
		String view;
		validateNMPHFormData(formVO, bindingResult);
		calculateCost(formVO);
		setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
		if (bindingResult.hasErrors()) {
			setNMPHCap(model);
			setSkipNMPHLink(model, dsaApplicationNumber);
			if (operation.equalsIgnoreCase(ADD)) {
				view = ADVISOR_NMPH_ALLOWANCES_PAGE;
				model.addAttribute(ADD_NMPH_FORM_VO, formVO);
			} else {
				view = ADVISOR_CHANGE_NMPH_PAGE;
				model.addAttribute(CHANGE_NMPH_FORM_VO, formVO);
			}
		} else {
			NMPHAllowanceVO nmphAllowanceVO = mapToNMPHAllowanceVO(formVO);
			if (operation.equalsIgnoreCase(ADD)) {
				nmphAllowancesService.addNMPHAllowance(nmphAllowanceVO);
			} else {
				nmphAllowancesService.changeNMPHItem(nmphAllowanceVO);
			}
			view = showNMPHSummary(request);
		}
		return view;
	}

	private void calculateCost(AddNMPHFormVO formVO) {
		if(!ObjectUtils.isEmpty(formVO.getWeeks()) && !ObjectUtils.isEmpty(formVO.getHourlyRate())
				&& !ObjectUtils.isEmpty(formVO.getHours())) {
			BigDecimal cost = new BigDecimal(formVO.getHourlyRate())
					.multiply(new BigDecimal(formVO.getHours())
							.multiply(new BigDecimal(formVO.getWeeks())));
			logger.info("setting form vo {} with cost {}", formVO, cost);
			formVO.setCost(cost.toString());
		}
	}

	private String initialiseAddNMPHPage(Model model, long dsaApplicationNumber, long studentReferenceNumber)
			throws Exception {
		String view;
		setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
		setNMPHCap(model);
		setSkipNMPHLink(model, dsaApplicationNumber);

		model.addAttribute(ADD_NMPH_FORM_VO, new AddNMPHFormVO());
		view = ADVISOR_NMPH_ALLOWANCES_PAGE;
		return view;
	}

}
