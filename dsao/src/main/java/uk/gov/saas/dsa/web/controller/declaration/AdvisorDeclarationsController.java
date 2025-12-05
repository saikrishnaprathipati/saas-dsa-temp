package uk.gov.saas.dsa.web.controller.declaration;

import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ADVISOR_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_DASHBOARD_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_DASHBOARD_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COMMA_DELIMETER;
import static uk.gov.saas.dsa.web.helper.DSAConstants.DSA_APPLICATION_NUMBER;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_MESSAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.I_AGREE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REDIRECT;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SKIP_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_FIRST_NAME;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_FULL_NAME;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_REFERENCE_NUMBER;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DeclarationsService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DeclarationFormVO;
import uk.gov.saas.dsa.vo.DeclarationTypeVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

/**
 * The Advisor Declaration controller
 */
@Controller
public class AdvisorDeclarationsController {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final String BACK_ACTION = "BACK_ACTION";

	private static final String BACK_TO_DASHBOARD = "BACK_TO_DASHBOARD";

	private static final String BACK_TO_ASSESSMENT_SUMMARY = "BACK_TO_ASSESSMENT_SUMMARY";
	private static final String BACK_TO_ADDITIONAL_INFO = "BACK_TO_ADDITIONAL_INFO";

	private static final String ACADEMIC_YEAR = "academicYear";

	public static final String ADVISOR_DECLARATIONS_URI = "advisorDeclarations";

	private static final String DECLARATION_CHECK_BOX_REQUIRED = "declaration.checkBox.required";

	private static final String DECLARATION_CODES = "declarationCodes";

	private static final String ADVISOR_DECLARATION_TYPES = "advisorDeclarationTypes";
	public static final String DECLARATION_DETAILS_URI = "declarationDetails";
	public static final String CONTINUE_NEXT_URI = "continueNext";
	public static final String ADVISOR_DECLARATION_VIEW = "advisor/advisorDeclaration";
	public static final String STUDENT_DECLARATION_VIEW = "advisor/studentDeclaration";
	public static final String ADVISOR_WHAT_HAPPENS_NEXT = "advisor/whathappensNext";
	public static final String DECLARATION_FORM_VO = "declarationFormVO";
	private static final List<String> allowedActions = Arrays.asList(BACK_ACTION, I_AGREE_ACTION,
			BACK_TO_ASSESSMENT_SUMMARY, BACK_TO_DASHBOARD, BACK_TO_ADDITIONAL_INFO);

	private static final List<String> whatHappensNextAllowedActions = Arrays.asList(BACK_ACTION,
			DSAConstants.SAVE_AND_CONTINUE_ACTION, APPLICATION_DASHBOARD_ACTION, "FINISH");

	private ApplicationService applicationService;

	private MessageSource messageSource;
	private EmailSenderService emailNotificataionService;
	private FindStudentService findStudentService;
	private final DeclarationsService declarationsService;

	/**
	 * The Advisor Declarations Controller
	 *
	 * @param declarationsService
	 * @param messageSource
	 * @param emailNotificataionService
	 * @param findStudentService
	 * @param applicationService
	 */
	public AdvisorDeclarationsController(DeclarationsService declarationsService, MessageSource messageSource,

			EmailSenderService emailNotificataionService, FindStudentService findStudentService,

			ApplicationService applicationService) {

		this.declarationsService = declarationsService;
		this.messageSource = messageSource;
		this.emailNotificataionService = emailNotificataionService;
		this.findStudentService = findStudentService;

		this.applicationService = applicationService;

	}

	/**
	 * Get all advisor declarations
	 *
	 * @return List of declaration types for the advisor
	 */
	@ModelAttribute(ADVISOR_DECLARATION_TYPES)
	public List<DeclarationTypeVO> getAdvisorDeclarations() {
		List<DeclarationTypeVO> advisorDeclarations = declarationsService
				.findAllActiveDeclarations(DSAConstants.ADVISOR_ACTION);
		logger.info("Advisor Declaration Types ref data {}", advisorDeclarations);
		return advisorDeclarations;
	}

	/**
	 * Initialising the declarations
	 *
	 * @param model     the model data
	 * @param action    action
	 * @param keyDataVO the VO
	 * @return HTML view
	 * @throws Exception
	 */
	@PostMapping(DECLARATION_DETAILS_URI)
	public String initDeclarations(Model model, @RequestParam(value = ACTION, required = true) String action,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("init declarations call {}", keyDataVO);
		String view = ERROR_PAGE;

		populateModelData(model, keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber(),
				new DeclarationFormVO());

		switch (action.toUpperCase()) {
		case ADVISOR_ACTION:
			model.addAttribute(BACK_ACTION, BACK_TO_DASHBOARD);
			view = ADVISOR_DECLARATION_VIEW;
			break;
		case SKIP_ACTION:
		case "COMPLETE_ADDITIONAL_INFO":
			model.addAttribute(BACK_ACTION, BACK_TO_ADDITIONAL_INFO);
			view = ADVISOR_DECLARATION_VIEW;
			break;
		case DSAConstants.STUDENT_ACTION:
			view = STUDENT_DECLARATION_VIEW;
			break;

		}
		return view;
	}

	/**
	 * Save the declaration
	 *
	 * @param model
	 * @param action
	 * @param request
	 * @param declarationFormVO
	 * @param bindingResult
	 * @return the next HTML view
	 * @throws IllegalAccessException
	 */
	@PostMapping(ADVISOR_DECLARATIONS_URI)
	public String saveDecalration(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = DECLARATION_FORM_VO) DeclarationFormVO declarationFormVO,
			BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		if (allowedActions.contains(action.toUpperCase())) {

			model.addAttribute(BACK_ACTION, declarationFormVO.getBackAction());
			if (action.toUpperCase().equalsIgnoreCase("BACK_TO_ADDITIONAL_INFO")) {
				view = AllowancesHelper.showAdditionalInfoPage(request);
			} else if (action.toUpperCase().equalsIgnoreCase(BACK_TO_ADDITIONAL_INFO)) {
//				view = AllowancesHelper.showAssessmentFeeSummary(request);
				view = AllowancesHelper.showAdditionalInfoPage(request);
			} else if (action.toUpperCase().equalsIgnoreCase(BACK_TO_DASHBOARD)) {
				view = redirectToDashboardPage(request, declarationFormVO);
			} else if (action.toUpperCase().equalsIgnoreCase(I_AGREE_ACTION)) {

				validateUserHasSelectedAllDeclarationsOrNot(declarationFormVO, bindingResult);

				if (bindingResult.hasErrors()) {
					return returnToPageWithErrors(model, declarationFormVO);
				}
				boolean advisorDeclarationsCompleted = AllowancesHelper.isAdvisorDeclarationsCompleted(
						declarationFormVO.getDsaApplicationNumber(), applicationService);
				if (!advisorDeclarationsCompleted) {

					declarationsService.saveAdvisorDeclarations(declarationFormVO.getDsaApplicationNumber());
				}
				final long studentReferenceNumber = declarationFormVO.getStudentReferenceNumber();

				DSAApplicationsMade applicationsMade = applicationService
						.findByDsaApplicationNumberAndStudentReferenceNumber(
								declarationFormVO.getDsaApplicationNumber(), studentReferenceNumber);
				logger.info("applicationsMade applicationsMade.getSessionCode() call {}",
						applicationsMade.getSessionCode());

				StudentResultVO studentResultVO;
				try {
					studentResultVO = AllowancesHelper.getStudentResultWithSuid(findStudentService,
							studentReferenceNumber, applicationsMade.getSessionCode());
				} catch (final IllegalAccessException e) {
					return returnToErrorPage(model, view, e);
				}

				sendEmailNotificationToStudent(advisorDeclarationsCompleted, studentResultVO, model);

				view = proceedToNextView(model, declarationFormVO, studentResultVO);

			} else {
				view = REDIRECT + APPLICATION_DASHBOARD_PATH;
			}
		}
		return view;
	}

	/**
	 * @param model
	 * @param action
	 * @param request
	 * @param keyDataVO
	 * @return
	 * @throws Exception
	 */
	@PostMapping(CONTINUE_NEXT_URI)
	public String continueApplication(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		logger.info("Action {}", action.toUpperCase());
		if (whatHappensNextAllowedActions.contains(action.toUpperCase())) {

			request.setAttribute(DSA_APPLICATION_NUMBER, keyDataVO.getDsaApplicationNumber());
			request.setAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());
			request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
			if (action.toUpperCase().equalsIgnoreCase(BACK_ACTION)) {
				view = initDeclarations(model, DSAConstants.ADVISOR_ACTION, keyDataVO);
			} else {
				view = REDIRECT + APPLICATION_DASHBOARD_PATH;
			}

		}
		return view;
	}

	private String returnToErrorPage(Model model, String view, IllegalAccessException e) {
		model.addAttribute(ERROR_MESSAGE, e.getMessage());
		return view;
	}

	private String returnToPageWithErrors(Model model, DeclarationFormVO declarationFormVO) {
		model.addAttribute(DSA_APPLICATION_NUMBER, declarationFormVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, declarationFormVO.getStudentReferenceNumber());
		model.addAttribute(DECLARATION_FORM_VO, declarationFormVO);
		return ADVISOR_DECLARATION_VIEW;
	}

	private String proceedToNextView(Model model, DeclarationFormVO declarationFormVO,
			StudentResultVO studentResultVO) {
		String view;
		model.addAttribute(STUDENT_FIRST_NAME, studentResultVO.getFirstName());
		model.addAttribute(DSA_APPLICATION_NUMBER, declarationFormVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, declarationFormVO.getStudentReferenceNumber());
		view = ADVISOR_WHAT_HAPPENS_NEXT;
		return view;
	}

	private boolean validateUserHasSelectedAllDeclarationsOrNot(DeclarationFormVO declarationFormVO,
			BindingResult bindingResult) {
		boolean allSelected = false;
		if (declarationFormVO.getDeclarationCodes() != null) {
			List<DeclarationTypeVO> declarationTypes = getAdvisorDeclarations();
			String allDecaltarionCodes = declarationTypes.stream().map(DeclarationTypeVO::getDeclarationCode).sorted()
					.collect(Collectors.joining(COMMA_DELIMETER));
			String selectedDeclarationCodes = declarationFormVO.getDeclarationCodes().stream().sorted()
					.collect(Collectors.joining(COMMA_DELIMETER));
			allSelected = allDecaltarionCodes.equalsIgnoreCase(selectedDeclarationCodes);
			if (!allSelected) {
				ValidationHelper.addError(bindingResult, messageSource, declarationFormVO.getClass().getName(),
						DECLARATION_CODES, DECLARATION_CHECK_BOX_REQUIRED);
			}

		} else {
			ValidationHelper.addError(bindingResult, messageSource, declarationFormVO.getClass().getName(),
					DECLARATION_CODES, DECLARATION_CHECK_BOX_REQUIRED);
		}
		return allSelected;
	}

	private void populateModelData(Model model, long dsaApplicationNumber, long studentReferenceNumber,
			DeclarationFormVO declarationFormVO) {

		boolean advisorDeclarationsCompleted = AllowancesHelper.isAdvisorDeclarationsCompleted(dsaApplicationNumber,
				applicationService);
		if (advisorDeclarationsCompleted) {
			List<DeclarationTypeVO> advisorDeclarations = getAdvisorDeclarations();
			List<String> declarationCodes = advisorDeclarations.stream()
					.map(declarationType -> declarationType.getDeclarationCode()).collect(Collectors.toList());
			declarationFormVO.setDeclarationCodes(declarationCodes);
		}

		model.addAttribute(DSA_APPLICATION_NUMBER, dsaApplicationNumber);
		model.addAttribute(STUDENT_REFERENCE_NUMBER, studentReferenceNumber);
		model.addAttribute(DECLARATION_FORM_VO, declarationFormVO);
	}

	private String redirectToDashboardPage(HttpServletRequest request, DeclarationFormVO declarationFormVO) {
		request.setAttribute(DSA_APPLICATION_NUMBER, declarationFormVO.getDsaApplicationNumber());
		request.setAttribute(STUDENT_REFERENCE_NUMBER, declarationFormVO.getStudentReferenceNumber());
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + APPLICATION_DASHBOARD_PATH;
	}

	private boolean sendEmailNotificationToStudent(boolean advisorDeclarationsCompleted,
			StudentResultVO studentResultVO, Model model) {
		boolean isSuccess = true;

		try {
			if (!advisorDeclarationsCompleted) {
				HashMap<String, Object> modelMap = new HashMap<String, Object>();
				modelMap.put(ACADEMIC_YEAR, studentResultVO.getStudentCourseYear().getAcademicYearFull());
				modelMap.put(STUDENT_FULL_NAME, studentResultVO.getFirstName() + " " + studentResultVO.getLastName());

				emailNotificataionService.sendEmailNotification(studentResultVO, "Please complete your DSA application",

						DSAConstants.STUDENT_NOTIFICATION_TEMPLATE_HTML, modelMap);
				logger.info("Send email Please complete your DSA application {}", modelMap);
			}
		} catch (IllegalAccessException e) {
			model.addAttribute(ERROR_MESSAGE, e.getMessage());
			isSuccess = false;
		}
		return isSuccess;
	}

}
