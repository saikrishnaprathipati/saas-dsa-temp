package uk.gov.saas.dsa.web.controller.declaration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DeclarationsService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.*;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.controller.ReportIssueController;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

/**
 * The student Declaration controller
 */
@Controller
public class StudentDeclarationsController {
	@Autowired
	ReportIssueController reportIssueController;

	private final Logger logger = LogManager.getLogger(this.getClass());

	public static final String STUDENT_DECLARATION = "STUDENT_DECLARATION";
	private static final String DECLARATION_CHECK_BOX_REQUIRED = "studentDeclaration.checkBox.required";
	private static final String DECLARATION_CODES = "declarationCodes";
	private static final String STUDENT_DECLARATION_TYPES = "studentDeclarationTypes";
	private static final String STUDENT_DECLARATION_NO_AGREEMENT_VIEW = "student/studentDeclarationNotAccepted";
	private static final String FINISH = "FINISH";
	public static final String CHOOSE_BANK_ACCOUNT_URI = "chooseBankAccount";
	public static final String STUDENT_DECLARATIONS_URI = "studentDeclaration";
	public static final String STUDENT_DECLARATION_DETAILS_URI = "studentDeclarationDetails";
	public static final String STUDENT_DECLARATION_VIEW = "student/studentDeclaration";
	public static final String DECLARATION_FORM_VO = "declarationFormVO";
	public static final String NO_AGREEMENT_VIEW_URI = "continueToDashboard";

	private final ApplicationService applicationService;
	private final MessageSource messageSource;
	private final FindStudentService findStudentService;
	private final DeclarationsService declarationsService;

	/**
	 * The Advisor Declarations Controller
	 */
	public StudentDeclarationsController(DeclarationsService declarationsService, MessageSource messageSource,
										 FindStudentService findStudentService, ApplicationService applicationService) {

		this.declarationsService = declarationsService;
		this.messageSource = messageSource;
		this.findStudentService = findStudentService;

		this.applicationService = applicationService;

	}

	/**
	 * Get all advisor declarations
	 *
	 * @return List of declaration types for the advisor
	 */
	@ModelAttribute(STUDENT_DECLARATION_TYPES)
	public List<DeclarationTypeVO> getStudentDeclarations() {
		// Hard coded the student declaration type as it has only one declaration to add
		// in to the DB.
		DeclarationTypeVO declarationTypeVO = new DeclarationTypeVO();
		declarationTypeVO.setDeclarationCode("I_AGREE");
		declarationTypeVO.setDeclarationTypeDesc("I have read and agree with the statements above.");
		declarationTypeVO.setDeclarationTypeId(1);
		return Collections.singletonList(declarationTypeVO);
	}

	/**
	 * Initialising the declarations
	 *
	 * @param model     the model data
	 * @param action    action
	 * @param keyDataVO the VO
	 * @return HTML view
	 */
	@PostMapping(STUDENT_DECLARATION_DETAILS_URI)
	public String initDeclarations(Model model, @RequestParam(value = ACTION) String action,
								   @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO) {
		logger.info("init declarations call {}", keyDataVO);
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		populateModelData(model, keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber(),
				new StudentDeclarationFormVO());
		if (action.equalsIgnoreCase(DSAConstants.STUDENT_ACTION)) {
			return STUDENT_DECLARATION_VIEW;
		}
		return ERROR_PAGE;
	}

	/**
	 * Save the declaration
	 *
	 * @param model             Model
	 * @param action            Action type
	 * @param request           Http Request
	 * @param declarationFormVO Declaration Form VO
	 * @param bindingResult     Binding Result
	 * @return The next HTML view
	 */
	@PostMapping(STUDENT_DECLARATIONS_URI)
	public String saveDeclaration(Model model, @RequestParam(value = ACTION) String action,
								  HttpServletRequest request,
								  @Valid @ModelAttribute(name = DECLARATION_FORM_VO) StudentDeclarationFormVO declarationFormVO,
								  BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, declarationFormVO.getDsaApplicationNumber(),
				declarationFormVO.getStudentReferenceNumber());
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
				case STUDENT_DECLARATION:
					populateModelData(model, declarationFormVO.getDsaApplicationNumber(),
							declarationFormVO.getStudentReferenceNumber(), new StudentDeclarationFormVO());
					return STUDENT_DECLARATION_VIEW;
				case I_AGREE_ACTION:
					validateUserHasSelectedAllDeclarationsOrNot(declarationFormVO, bindingResult);

					if (bindingResult.hasErrors()) {
						return returnToPageWithErrors(model, declarationFormVO);
					}

					boolean advisorDeclarationsCompleted = isStudentDeclarationsCompleted(
							declarationFormVO.getDsaApplicationNumber());
					if (!advisorDeclarationsCompleted) {
						declarationsService.saveStudentDeclarations(declarationFormVO.getDsaApplicationNumber());
					}

 
//					return AllowancesHelper.showChooseBankAccountPage(request); 
					return AllowancesHelper.showAwardAccessPage(request); 
				case I_DO_NOT_AGREE_ACTION:
					if (bindingResult.hasErrors()) {
						return returnToPageWithErrors(model, declarationFormVO);
					}
					populateModelData(model, declarationFormVO.getDsaApplicationNumber(),
							declarationFormVO.getStudentReferenceNumber(), new StudentDeclarationFormVO());

					// Redirect to report issue page
					ApplicationKeyDataFormVO applicationKeyDataFormVO = new ApplicationKeyDataFormVO();
					applicationKeyDataFormVO.setStudentReferenceNumber(declarationFormVO.getStudentReferenceNumber());
					applicationKeyDataFormVO.setDsaApplicationNumber(declarationFormVO.getDsaApplicationNumber());
					return reportIssueController.initReportIssue(model, "REPORT_ISSUE", applicationKeyDataFormVO);
				case DASHBOARD_ACTION:
					return AllowancesHelper.showDashboardPage(request);
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}

		return ERROR_PAGE;
	}

	private String returnToErrorPage(Model model, IllegalAccessException e) {
		model.addAttribute(ERROR_MESSAGE, e.getMessage());
		return ERROR_PAGE;
	}

	private String returnToPageWithErrors(Model model, StudentDeclarationFormVO declarationFormVO) {
		model.addAttribute(DSA_APPLICATION_NUMBER, declarationFormVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, declarationFormVO.getStudentReferenceNumber());
		model.addAttribute(DECLARATION_FORM_VO, declarationFormVO);
		return STUDENT_DECLARATION_VIEW;
	}

	private String proceedToNextView(HttpServletRequest request, Model model,
									 StudentDeclarationFormVO declarationFormVO, StudentResultVO studentResultVO) {
		String view;
		model.addAttribute(STUDENT_FIRST_NAME, studentResultVO.getFirstName());
		model.addAttribute(DSA_APPLICATION_NUMBER, declarationFormVO.getDsaApplicationNumber());
		model.addAttribute(STUDENT_REFERENCE_NUMBER, declarationFormVO.getStudentReferenceNumber());
		view = AllowancesHelper.showChooseBankAccountPage(request);
		return view;
	}

	private void validateUserHasSelectedAllDeclarationsOrNot(StudentDeclarationFormVO declarationFormVO,
															 BindingResult bindingResult) {
		if (declarationFormVO.getDeclarationCodes() != null) {
			List<DeclarationTypeVO> declarationTypes = getStudentDeclarations();
			String allDeclarationCodes = declarationTypes.stream().map(DeclarationTypeVO::getDeclarationCode).sorted()
					.collect(Collectors.joining(COMMA_DELIMETER));
			String selectedDeclarationCodes = declarationFormVO.getDeclarationCodes().stream().sorted()
					.collect(Collectors.joining(COMMA_DELIMETER));
			boolean allSelected = allDeclarationCodes.equalsIgnoreCase(selectedDeclarationCodes);
			if (!allSelected) {
				ValidationHelper.addError(bindingResult, messageSource, declarationFormVO.getClass().getName(),
						DECLARATION_CODES, DECLARATION_CHECK_BOX_REQUIRED);
			}

		} else {
			ValidationHelper.addError(bindingResult, messageSource, declarationFormVO.getClass().getName(),
					DECLARATION_CODES, DECLARATION_CHECK_BOX_REQUIRED);
		}

	}

	private StudentResultVO findStudentByReferenceNumber(long studentReferenceNumber) throws IllegalAccessException {
		return findStudentService.findByStudReferenceNumber(studentReferenceNumber);
	}

	private void populateModelData(Model model, long dsaApplicationNumber, long studentReferenceNumber,
								   StudentDeclarationFormVO declarationFormVO) {

		boolean studentDeclarationsCompleted = isStudentDeclarationsCompleted(dsaApplicationNumber);
		if (studentDeclarationsCompleted) {
			List<DeclarationTypeVO> advisorDeclarations = getStudentDeclarations();
			List<String> declarationCodes = advisorDeclarations.stream()
					.map(DeclarationTypeVO::getDeclarationCode).collect(Collectors.toList());
			declarationFormVO.setDeclarationCodes(declarationCodes);
		}

		model.addAttribute(DSA_APPLICATION_NUMBER, dsaApplicationNumber);
		model.addAttribute(STUDENT_REFERENCE_NUMBER, studentReferenceNumber);
		model.addAttribute(DECLARATION_FORM_VO, declarationFormVO);
	}

	private boolean isStudentDeclarationsCompleted(long dsaApplicationNumber) {
		ApplicationSectiponStatusVO sectionStatus = applicationService.getApplicationSectionStatus(dsaApplicationNumber,
				Section.STUDENT_DECLARATION);

		return sectionStatus.getSectionStatus().equals(SectionStatus.COMPLETED)
				|| sectionStatus.getSectionStatus().equals(SectionStatus.STARTED);
	}

}
