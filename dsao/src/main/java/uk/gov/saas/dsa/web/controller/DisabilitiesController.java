package uk.gov.saas.dsa.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DisabilitiesService;
import uk.gov.saas.dsa.service.StudentDetailsService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DisabilitiesFormVO;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.vo.DisabilityTypeVO.DISABILITY_NOT_LISTED;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.matches;

/**
 * The course details controller
 */
@Controller
public class DisabilitiesController {

	private static final String DISABILITY_NOT_LISTED_TEXT_INVALID = "disability.notListedText.invalid";
	private static final String NOT_LISTED_TEXT_PATTERN = "[\\x00-\\x7F]+";
	private static final String DISABILITY_NOT_LISTED_TEXT_REQUIRED = "disability.notListedText.required";
	private static final String NOT_LISTED_TEXT = "notListedText";

	private static final String ALL_DISABILITIES = "allDisabilities";

	private static final String DISABILITIES_FORM_VO = "disabilitiesFormVO";

	private static final String INIT_DISABILITIES_URI = "/disabilityDetails";
	private static final String STUDENT_DISABILITIES_SELECTIN_URI = "/disabilities";
	private static final String DISABILITIES_VIEW = "advisor/disabilities";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final StudentDetailsService studentDetailsService;
	private final ApplicationService applicationService;

	private final DisabilitiesService disabilitiesService;
	private List<String> allowedActins = Arrays.asList(BACK_ACTION, SAVE_AND_CONTINUE_ACTION);

	public DisabilitiesController(StudentDetailsService studentDetailsService, DisabilitiesService disabilitiesService,
			ApplicationService applicationService) {
		this.studentDetailsService = studentDetailsService;
		this.disabilitiesService = disabilitiesService;
		this.applicationService = applicationService;
	}

	/**
	 * To load the disability details
	 *
	 * @param model
	 * @param keyDataVO
	 * @return the html page with disabilities data
	 */
	@PostMapping(INIT_DISABILITIES_URI)
	public String initDisabilities(Model model,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			BindingResult bindingResult) throws Exception {
		logger.info("init disabilities call {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		DisabilitiesFormVO disabilitiesFormVO = new DisabilitiesFormVO();
		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		StudentResultVO studentResultVO = studentDetailsService.findStudentDetailsFromDB(studentReferenceNumber);

		model.addAttribute(STUDENT_FIRST_NAME, studentResultVO.getFirstName());
		model.addAttribute(DSA_APPLICATION_NUMBER, keyDataVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());

		populateModelData(model, bindingResult, keyDataVO.getDsaApplicationNumber(), disabilitiesFormVO);
		logger.info("init disabilities call completed");

		return DISABILITIES_VIEW;
	}

	/**
	 * Add or updateDisabilities
	 *
	 * @param model              model data
	 * @param action             either Back or Continue
	 * @param request            servlet request
	 * @param disabilitiesFormVO the form vo
	 * @param bindingResult      binding result
	 * @return html to render
	 * @throws Exception
	 */
	@PostMapping(STUDENT_DISABILITIES_SELECTIN_URI)
	public String addDisabilities(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute(name = DISABILITIES_FORM_VO) DisabilitiesFormVO disabilitiesFormVO,
			BindingResult bindingResult) throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		logger.info("disabilities call action {}, form data {}", action, disabilitiesFormVO);
		if (allowedActins.contains(action.toUpperCase())) {
			setKeyValuesToModel(model, disabilitiesFormVO);
			if (action.toUpperCase().equalsIgnoreCase(BACK_ACTION)) {

				view = AllowancesHelper.showDashboardPage(request);
			} else if (action.toUpperCase().equalsIgnoreCase(SAVE_AND_CONTINUE_ACTION)) {
				bindingResult = validateDisabilityNotListedCheckBox(bindingResult, disabilitiesFormVO);
				if (bindingResult.hasErrors()) {
					populateModelData(model, bindingResult, disabilitiesFormVO.getDsaApplicationNumber(),
							disabilitiesFormVO);
					return DISABILITIES_VIEW;
				} else {
					saveDisabilities(disabilitiesFormVO);

					return AllowancesHelper.disabilitiesSummary(request);

				}
			}
		}

		logger.info("Redirecting to view:{}", view);

		return view;

	}

	@PostMapping(DSAConstants.DISABILITY_DETAILS_SUMMARY_PATH)
	public String disabilitiesSummary(Model model,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
			@RequestParam(value = ACTION, required = true) String action,
			@RequestParam(value = "backAction", required = false) String backAction, HttpServletRequest request)
			throws Exception {
		logger.info("init disabilities call {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
				keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
		String view = ERROR_PAGE;
		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, backAction);
		if ((backAction == null && action == "")
				|| (backAction == null && action.equals("BACK_TO_DISABILITY_SUMMARY"))) {
			action = BACK_ACTION;
		}
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case SAVE_AND_CONTINUE_ACTION:
			case "DISBILITIES_SUMMARY":
			case "BACK_TO_DISABILITY_SUMMARY":
				Map<String, String> disabilitiesMap = AllowancesHelper.populateDisabilitiesData(disabilitiesService,
						keyDataVO.getDsaApplicationNumber());
				model.addAttribute("disabilitiesMap", disabilitiesMap);
				view = "advisor/disabilities/disabilitiesSummary";
				break;
			case DSAConstants.BACK_TO_DASHBOARD:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case "BACK_TO_COURSE_DETAILS":
				view = AllowancesHelper.showCourseDetailsPage(request);
				break;
			case BACK_ACTION:
				if (LoggedinUserUtil.isAdvisor()) {
					view = AllowancesHelper.initDisabilities(request);
				} else {
					view = AllowancesHelper.showDashboardPage(request);
				}
				break;
			case "CONTINUE":
				if (LoggedinUserUtil.isAdvisor()) {
					ApplicationResponse applicationResponse = applicationService.findApplication(
							keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
					if (!applicationResponse.isAdvisorDeclarationCompleted()) {
						List<ConsumableTypeVO> consumableItems = applicationResponse.getConsumables();
						if (consumableItems.isEmpty()) {
							view = AllowancesHelper.showConsumablesInitialPage(request);
						} else {
							view = AllowancesHelper.showConsumableSummary(request);
						}
					} else {
						view = AllowancesHelper.showAllowancesSummary(request);
					}
				} else {
					view = AllowancesHelper.showAllowancesSummary(request);
				}
				break;
			case "BACK_FROM_CONSUMABLES":

			}
		}
		return view;

	}

	/**
	 * Get all active disability types
	 *
	 * @return list of disability types
	 */
	@ModelAttribute(ALL_DISABILITIES)
	public List<DisabilityTypeVO> getDisabilityTypes() {
		logger.info("Loading Disabilities Types ref data");
		List<DisabilityTypeVO> activeDisabilityTypes = disabilitiesService.getActiveDisabilityTypes();
		logger.info("Disability Types ref data {}", activeDisabilityTypes);
		return activeDisabilityTypes;
	}

	private void setKeyValuesToModel(Model model, DisabilitiesFormVO disabilitiesFormVO) {
		model.addAttribute(DSA_APPLICATION_NUMBER, disabilitiesFormVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, disabilitiesFormVO.getStudentReferenceNumber());
		model.addAttribute(STUDENT_FIRST_NAME, disabilitiesFormVO.getStudentFirstName());
	}

	private void saveDisabilities(DisabilitiesFormVO disabilitiesFormVO) throws IllegalAccessException {
		List<String> disabilityCodesToSave = disabilitiesFormVO.getDisabilityCodes();
		disabilitiesService.saveDisabilities(disabilitiesFormVO.getDsaApplicationNumber(),
				disabilitiesFormVO.getStudentReferenceNumber(), disabilityCodesToSave,
				disabilitiesFormVO.getNotListedText());
	}

	private void populateModelData(Model model, BindingResult bindingResult, Long dsaApplicationNumber,
			DisabilitiesFormVO disabilitiesFormVO) {
		List<DisabilityTypeVO> disabilityTypes = AllowancesHelper.getApplicationDisabilityTypes(disabilitiesService,
				dsaApplicationNumber);

		setDisabilityCodesToRender(disabilitiesFormVO, disabilityTypes);

		setDisabilityNotListedTextToRender(disabilitiesFormVO, disabilityTypes);

		model.addAttribute(DISABILITIES_FORM_VO, disabilitiesFormVO);
	}

	private void setDisabilityNotListedTextToRender(DisabilitiesFormVO disabilitiesFormVO,
			List<DisabilityTypeVO> disabilityTypes) {
		Optional<DisabilityTypeVO> notListedDisability = disabilityTypes.stream()
				.filter(disability -> disability.getDisabilityCode().equals(DISABILITY_NOT_LISTED)).findFirst();
		if (notListedDisability.isPresent()) {
			DisabilityTypeVO disabilityTypeVO = notListedDisability.get();
			String disabilityNotlistedText = disabilityTypeVO.getDisabilityNotlistedText();
			if (StringUtils.hasText(disabilityNotlistedText)) {
				disabilitiesFormVO.setNotListedText(disabilityTypeVO.getDisabilityNotlistedText());
			}
		}
	}

	private void setDisabilityCodesToRender(DisabilitiesFormVO disabilitiesFormVO,
			List<DisabilityTypeVO> disabilityTypes) {
		List<String> selectedDisabilityTypeCodesInDB = disabilityTypes.stream()
				.filter(disabilityType -> disabilityType.isSelected())
				.map(disabilityType -> disabilityType.getDisabilityCode()).collect(Collectors.toList());

		List<String> list = new ArrayList<>();
		if (disabilitiesFormVO.getDisabilityCodes() != null) {
			list = disabilitiesFormVO.getDisabilityCodes();
		} else {
			list = selectedDisabilityTypeCodesInDB;
		}
		disabilitiesFormVO.setDisabilityCodes(list);
	}

	private BindingResult validateDisabilityNotListedCheckBox(BindingResult bindingResult,
			@Valid DisabilitiesFormVO disabilitiesFormVO) {
		boolean hasErrors = bindingResult.hasErrors();
		if (!hasErrors) {
			String notListedText = disabilitiesFormVO.getNotListedText();
			boolean hasText = StringUtils.hasText(notListedText);
			boolean hasDisabilityNotListedCode = disabilitiesFormVO.getDisabilityCodes()
					.contains(DISABILITY_NOT_LISTED);

			if (!hasText && hasDisabilityNotListedCode) {
				bindingResult.rejectValue(NOT_LISTED_TEXT, DISABILITY_NOT_LISTED_TEXT_REQUIRED);
			} else if (hasDisabilityNotListedCode
					&& !matches(Pattern.compile(NOT_LISTED_TEXT_PATTERN), notListedText)) {
				bindingResult.rejectValue(NOT_LISTED_TEXT, DISABILITY_NOT_LISTED_TEXT_INVALID);
			}
		}
		return bindingResult;
	}

}
