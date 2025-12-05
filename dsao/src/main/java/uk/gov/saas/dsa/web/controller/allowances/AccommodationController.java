package uk.gov.saas.dsa.web.controller.allowances;

import static uk.gov.saas.dsa.web.controller.allowances.NMPHController.REMOVE_ITEM_FORM_VO;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.redirectToView;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setStudentDetailsInTheModel;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.stringToBigDecimal;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ALLOWANCES_SUMMARY_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ALLOWANCES_SUMMARY_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.BACK_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.BACK_BUTTON_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COST_REGEX;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REMOVE_ITEM;
import static uk.gov.saas.dsa.web.helper.DSAConstants.TWO_DIG_NO_REGEX;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.gov.saas.dsa.domain.converters.AccommodationTypeConverter;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.AccommodationType;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.service.allowances.AccommodationService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationAllowanceFormVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationTypeFormVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.nmph.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Controller
public class AccommodationController {
	private static final String ACCOMMODATION_ENHANCED_ACCOMMODATION_COST_LESSTHAN = "accommodation.enhancedAccommodationCost.lessthan";

	private static final String CHANGE_FROM_ACCOMMO_SUMMARY = "CHANGE_FROM_ACCOMMO_SUMMARY";

	private static final String ACCOMMODATION_SUMMARY = "ACCOMMODATION_SUMMARY";

	private static final String REMOVE_FROM_ALLOWANCES_SUMMARY = "REMOVE_FROM_ALLOWANCES_SUMMARY";

	private static final String STANDARD = "standard ";

	private static final String ENHANCED = "enhanced ";

	private static final String ACCOMMODATION_COST = "accommodation cost";

	private static final String ENHANCED_ACCOMMODATION_COST = "enhancedAccommodationCost";
	private static final String WEEKS = "weeks";
	private static final String STANDARD_ACCOMMODATION_COST = "standardAccommodationCost";
	private static final String ACCOMMODATION_ALLOWANCE_FORM_VO = "accommodationAllowanceFormVO";
	private static final String ADVISOR_ACCOMMODATION_SELECTION = "advisor/accommodation/accommodation";
	private static final String ACCOMMODATION_ALLOWANCE = "advisor/accommodation/accommodationAllowance";
	private static final String ACCOMMODATION_FORM_VO = "accommodationFormVO";
	private final AccommodationService accommodationService;
	private static final String ACCOMMODATION_FIELD_INVALID = "accommodation.%s.invalid";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;

	public AccommodationController(ApplicationService applicationService, FindStudentService findStudentService,
								   AccommodationService accommodationService) {
		this.applicationService = applicationService;
		this.findStudentService = findStudentService;
		this.accommodationService = accommodationService;

	}

	@PostMapping("removeAccommodation")
	public String removeAccommodation(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
								 @ModelAttribute(name = REMOVE_ITEM_FORM_VO) RemoveItemFormVO removeItemFormVO, BindingResult bindingResult)
			throws IllegalAccessException {
		logger.info("in Remove nmph allowances call action: {}, request: {}", action, removeItemFormVO);

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
				view = AllowancesHelper.showAccommodationsSummary(request);
				break;
			case REMOVE_FROM_ALLOWANCES_SUMMARY:
				model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, ALLOWANCES_SUMMARY_ACTION);
				model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
				view = "advisor/accommodation/removeAccommodation";
				break;
			case "REMOVE_FROM_ACCOMMO_SUMMARY":
				model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, ACCOMMODATION_SUMMARY);
				model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
				view = "advisor/accommodation/removeAccommodation";
				break;
			case ALLOWANCES_SUMMARY_ACTION:
				view = AllowancesHelper.showAllowancesSummary(request);
				break;
			case "CONFIRM_REMOVE_ACCOMMODATION":
				String removeItem = removeItemFormVO.getRemoveItem();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						accommodationService.deleteAccommodation(removeItemFormVO.getItemId());
						ServiceUtil.updateSectionStatus(applicationService, removeItemFormVO.getDsaApplicationNumber(),
								Section.ALLOWANCES, SectionStatus.STARTED);
						if (removeItemFormVO.getBackAction().equalsIgnoreCase(ALLOWANCES_SUMMARY_ACTION)) {
							view = AllowancesHelper.showAllowancesSummary(request);
						} else {
							view = AllowancesHelper.showAccommodationsSummary(request);
						}
					} else if (removeItemFormVO.getBackAction() != null
							&& removeItemFormVO.getBackAction().equals(ALLOWANCES_SUMMARY_ACTION)) {
						view = AllowancesHelper.showAllowancesSummary(request);
					} else {
						view = AllowancesHelper.showAccommodationsSummary(request);
					}
				} else {
					bindingResult.rejectValue(REMOVE_ITEM, DSAConstants.ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
					view = "advisor/accommodation/removeAccommodation";
				}

				break;
		}
		return view;

	}

	@PostMapping("selectAccommodation")
	public String selectAccommodation(Model model, @RequestParam(value = ACTION, required = true) String action,
									  HttpServletRequest request,
									  @Valid @ModelAttribute(name = "addAccommodationFormVO") AccommodationTypeFormVO keyDataVO,
									  BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
				case "SKIP_ACCOMMODATION":
					view = redirectToView(request, ALLOWANCES_SUMMARY_PATH);
					break;
				case "SELECT_ACCOMMO_FROM_TRAVEL_SUMMARY":
					if (accommodationService.getAccommodations(dsaApplicationNumber).isEmpty()) {
						view = performAcion(model, keyDataVO, dsaApplicationNumber, studentReferenceNumber);
					} else {
						view = AllowancesHelper.showAccommodationsSummary(request);
					}

					break;
				case ACCOMMODATION_SUMMARY:
					view = AllowancesHelper.showAccommodationsSummary(request);
					break;
				case ALLOWANCES_SUMMARY_ACTION:
					view = AllowancesHelper.showAllowancesSummary(request);
					break;
				case "ADD_ACCOMMTYPE_FROM_SUMMARY":
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, ALLOWANCES_SUMMARY_ACTION);
					view = performAcion(model, keyDataVO, dsaApplicationNumber, studentReferenceNumber);
					break;
				default:

					view = performAcion(model, keyDataVO, dsaApplicationNumber, studentReferenceNumber);
					break;
			}

		}
		return view;
	}

	private void setSkipAccommodationLink(Model model, long dsaApplicationNumber) {
		boolean noAccommodations = accommodationService.getAccommodations(dsaApplicationNumber).isEmpty();

		model.addAttribute("showSkipAccommodation", noAccommodations);

	}

	private String performAcion(Model model, AccommodationTypeFormVO keyDataVO, long dsaApplicationNumber,
								long studentReferenceNumber) throws Exception {
		String view;
		setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);
		model.addAttribute(ACCOMMODATION_FORM_VO, keyDataVO);
		setSkipAccommodationLink(model, dsaApplicationNumber);
		view = ADVISOR_ACCOMMODATION_SELECTION;
		return view;
	}

	@PostMapping("accommodationSummary")
	public String accommodationSummary(Model model, HttpServletRequest request,
									   @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
									   BindingResult bindingResult) throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		boolean hasValues = hasMandatoryValues(model, dsaApplicationNumber, keyDataVO.getStudentReferenceNumber());
		if (hasValues) {
			setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(), findStudentService);
			AllowancesHelper.setAccommodationSummaryData(model, accommodationService, dsaApplicationNumber);
			view = "advisor/accommodation/accommodationSummary";
		}
		logger.info("View or redirecting to {}", view);
		return view;

	}

	@PostMapping("addAccommodationType")
	public String addAccommodationType(Model model, @RequestParam(value = ACTION, required = true) String action,
									   HttpServletRequest request,
									   @Valid @ModelAttribute(name = ACCOMMODATION_FORM_VO) AccommodationTypeFormVO formVO,
									   BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = formVO.getDsaApplicationNumber();
		long studentReferenceNumber = formVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, "SELECT_ACCOMMODATION_TYPE");
			AccommodationType accommodationType = deriveType(formVO.getAccommodationType());

			switch (action.toUpperCase()) {
				case "SKIP_ACCOMMODATION":
					view = redirectToView(request, ALLOWANCES_SUMMARY_PATH);
					break;
				case ACCOMMODATION_SUMMARY:
					view = AllowancesHelper.showAccommodationsSummary(request);
					break;
				case ALLOWANCES_SUMMARY_ACTION:
					view = AllowancesHelper.showAllowancesSummary(request);
					break;
				case "SELECT_ACCOMMODATION_TYPE":
				case "ADD_ACCOMMTYPE_FROM_SUMMARY":
					AccommodationTypeFormVO keyDataVO = new AccommodationTypeFormVO();
					keyDataVO.setStudentReferenceNumber(formVO.getStudentReferenceNumber());
					keyDataVO.setDsaApplicationNumber(formVO.getDsaApplicationNumber());

					view = this.selectAccommodation(model, action, request, keyDataVO, bindingResult, redirectAttributes);
					break;
				case "BACK_ACTION":
					view = AllowancesHelper.showTravelExpSummary(request);
					break;
				default:
					if (action.isEmpty()) {
						view = AllowancesHelper.showAllowancesSummary(request);
					} else if (accommodationType != null) {
						AccommodationVO vo = new AccommodationVO();
						vo.setDsaApplicationNumber(dsaApplicationNumber);
						vo.setStudentReferenceNumber(studentReferenceNumber);
						vo.setAccommodationType(deriveType(formVO.getAccommodationType()));
						AccommodationVO accommodationVO = accommodationService.addAccommodationType(vo);
						model.addAttribute(ACCOMMODATION_ALLOWANCE_FORM_VO, voToForm(accommodationVO));
						view = AllowancesHelper.showAccommodationAllowanceDetails(request);
					} else {
						bindingResult.rejectValue("accommodationType", "accommodation.selection.required");
						setStudentDetailsInTheModel(model, studentReferenceNumber, findStudentService);
						view = ADVISOR_ACCOMMODATION_SELECTION;
					}
					break;
			}
		}

		return view;
	}

	@PostMapping(path = {"accommodationAllowance", "changeAccommodation"})
	public String addAccommodationAllowance(Model model, @RequestParam(value = ACTION, required = true) String action,
											HttpServletRequest request,
											@Valid @ModelAttribute(name = ACCOMMODATION_ALLOWANCE_FORM_VO) AccommodationAllowanceFormVO formVO,
											BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = formVO.getDsaApplicationNumber();
		long studentReferenceNumber = formVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, "SELECT_ACCOMMODATION_TYPE");
			switch (action.toUpperCase()) {
				case "INIT_ACCOMMODATION_DETAILS":
					AccommodationAllowanceFormVO accommodationAllowanceFormVO = new AccommodationAllowanceFormVO();
					accommodationAllowanceFormVO.setDsaApplicationNumber(dsaApplicationNumber);
					accommodationAllowanceFormVO.setStudentReferenceNumber(studentReferenceNumber);

					AccommodationVO accommodationVO = accommodationService.findAccommodationType(toAccommodationVO(formVO));
					AccommodationAllowanceFormVO voToForm = voToForm(accommodationVO);
					model.addAttribute(ACCOMMODATION_ALLOWANCE_FORM_VO, voToForm);

					view = ACCOMMODATION_ALLOWANCE;
					break;
				case "SELECT_ACCOMMODATION_TYPE":
					AccommodationTypeFormVO keyDataVO = new AccommodationTypeFormVO();
					keyDataVO.setDsaApplicationNumber(dsaApplicationNumber);
					keyDataVO.setStudentReferenceNumber(studentReferenceNumber);
					view = this.selectAccommodation(model, action, request, keyDataVO, bindingResult, redirectAttributes);
					break;
				case CHANGE_FROM_ACCOMMO_SUMMARY:
				case "CHANGE_ACCOMO_FROM_ALLOWANCE_SUMMARY":
					long id = formVO.getId();
					AccommodationVO accommodation = accommodationService.getAccommodation(id);
					formVO = voToForm(accommodation);
					model.addAttribute(ACCOMMODATION_ALLOWANCE_FORM_VO, formVO);

					if (action.toUpperCase().equals(CHANGE_FROM_ACCOMMO_SUMMARY)) {
						model.addAttribute(BACK_BUTTON_ACTION, ACCOMMODATION_SUMMARY);
					} else {
						model.addAttribute(BACK_BUTTON_ACTION, ALLOWANCES_SUMMARY_ACTION);
					}

					view = ACCOMMODATION_ALLOWANCE;
					break;
				case "ADD_ACCOMMODATION_DETAILS":
				case DSAConstants.SAVE_AND_CONTINUE_ACTION:
					validateFormData(formVO, bindingResult);
					if (bindingResult.hasErrors()) {
						view = ACCOMMODATION_ALLOWANCE;
					} else {
						accommodationService.updateAccommodation(toAccommodationVO(formVO));
						ServiceUtil.updateSectionStatus(applicationService, formVO.getDsaApplicationNumber(),
								Section.ALLOWANCES, SectionStatus.COMPLETED);
						view = AllowancesHelper.showAccommodationsSummary(request);

					}
					break;
				default:
					view = AllowancesHelper.showAllowancesSummary(request);
					break;
			}
		}
		return view;
	}

	private AccommodationType deriveType(String typeStr) {
		AccommodationType type = null;
		if (typeStr != null && !typeStr.equals("")) {
			try {
				AccommodationTypeConverter con = new AccommodationTypeConverter();
				type = con.convertToEntityAttribute(typeStr);
			} catch (Exception e) {
				logger.error("Can not convert to AccommodationType for {}", typeStr);
			}
		}

		return type;
	}

	private void validateFormData(AccommodationAllowanceFormVO formVO, BindingResult bindingResult) {
		String standardAccommodationCost = formVO.getStandardAccommodationCost();
		String enhancedAccommodationCost = formVO.getEnhancedAccommodationCost();
		if (!bindingResult.hasFieldErrors(STANDARD_ACCOMMODATION_COST)) {
			formVO.setStandardAccommodationCost(AllowancesHelper.validateNumber(bindingResult,
					STANDARD + ACCOMMODATION_COST, standardAccommodationCost, STANDARD_ACCOMMODATION_COST,
					String.format(ACCOMMODATION_FIELD_INVALID, STANDARD_ACCOMMODATION_COST), NumberType.COST_XXXXX_YY,
					DSAConstants.COST_REGEX));
 
		}
		if (!bindingResult.hasFieldErrors(ENHANCED_ACCOMMODATION_COST)) {
			formVO.setEnhancedAccommodationCost(AllowancesHelper.validateNumber(bindingResult,
					ENHANCED + ACCOMMODATION_COST, enhancedAccommodationCost, ENHANCED_ACCOMMODATION_COST,
					String.format(ACCOMMODATION_FIELD_INVALID, ENHANCED_ACCOMMODATION_COST), NumberType.COST_XXXXX_YY,
					DSAConstants.COST_REGEX));
		}
		if (!bindingResult.hasFieldErrors(STANDARD_ACCOMMODATION_COST)
				&& !bindingResult.hasFieldErrors(ENHANCED_ACCOMMODATION_COST)) {

			BigDecimal standardCost = stringToBigDecimal(standardAccommodationCost);
			BigDecimal enhancedCost = stringToBigDecimal(enhancedAccommodationCost);
			if (enhancedCost.compareTo(standardCost) <= 0) {
				bindingResult.rejectValue(ENHANCED_ACCOMMODATION_COST,
						ACCOMMODATION_ENHANCED_ACCOMMODATION_COST_LESSTHAN);
			}

		}

		if (!bindingResult.hasFieldErrors(WEEKS)) {
			formVO.setWeeks(AllowancesHelper.validateNumber(bindingResult, WEEKS, formVO.getWeeks(), WEEKS,
					String.format(ACCOMMODATION_FIELD_INVALID, WEEKS), NumberType.WEEKS, TWO_DIG_NO_REGEX));
		}
	}

	private AccommodationAllowanceFormVO voToForm(AccommodationVO accommodationVO) {
		AccommodationAllowanceFormVO form = new AccommodationAllowanceFormVO();
		form.setDsaApplicationNumber(accommodationVO.getDsaApplicationNumber());
		form.setStudentReferenceNumber(accommodationVO.getStudentReferenceNumber());
		form.setId(accommodationVO.getId());
		form.setAccommodationType(accommodationVO.getAccommodationType().name());
		if (accommodationVO.getEnhancedCostStr() != null) {
			BigDecimal enhancedCost = accommodationVO.getEnhancedCost();
			form.setEnhancedAccommodationCost(enhancedCost.toString());
		}
		if (accommodationVO.getStandardCost() != null) {

			form.setStandardAccommodationCost(
					formatCost(NumberType.COST_XXXXX_YY, accommodationVO.getStandardCost().doubleValue()));
		}
		if (accommodationVO.getEnhancedCost() != null) {

			form.setEnhancedAccommodationCost(
					formatCost(NumberType.COST_XXXXX_YY, accommodationVO.getEnhancedCost().doubleValue()));
		}

		if (accommodationVO.getWeeks() != null && accommodationVO.getWeeks() > 0) {
			form.setWeeks(accommodationVO.getWeeks().toString());
		}
		return form;
	}

	private AccommodationVO toAccommodationVO(AccommodationAllowanceFormVO form) {
		AccommodationVO vo = new AccommodationVO();
		vo.setDsaApplicationNumber(form.getDsaApplicationNumber());
		vo.setStudentReferenceNumber(form.getStudentReferenceNumber());
		vo.setAccommodationType(deriveType(form.getAccommodationType()));
		vo.setId(form.getId());
		if (form.getEnhancedAccommodationCost() != null) {
			vo.setEnhancedCost(stringToBigDecimal(form.getEnhancedAccommodationCost()));
		}
		if (form.getStandardAccommodationCost() != null) {

			vo.setStandardCost(stringToBigDecimal(form.getStandardAccommodationCost()));
		}
		if (form.getWeeks() != null && form.getWeeks() != "") {
			vo.setWeeks(Integer.valueOf(form.getWeeks()));
		}
		return vo;
	}
}
