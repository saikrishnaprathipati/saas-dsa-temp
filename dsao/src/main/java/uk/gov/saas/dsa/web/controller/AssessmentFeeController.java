package uk.gov.saas.dsa.web.controller;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;

import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ALLOWANCES_REMOVE_OPTION_REQUIRED;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.BACK_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COST;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COST_REGEX;
import static uk.gov.saas.dsa.web.helper.DSAConstants.DASHBOARD_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.TWO_DIG_NO_REGEX;
import static uk.gov.saas.dsa.web.helper.DSAConstants.NMPH_SUMMARY_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REMOVE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REMOVE_ITEM;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SAVE_AND_CONTINUE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SKIP_ACTION;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.List;

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

import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.AssessmentFeeService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeFormVO;
import uk.gov.saas.dsa.vo.assessment.AssessmentFeeVO;
import uk.gov.saas.dsa.vo.nmph.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.allowances.NMPHController;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

@Controller
public class AssessmentFeeController {
	private static final String ASSESSMENT_FEE_SUMMARY = "assessmentFeeSummary";
	private static final String ADVISOR_ASSESSMENT_FEE_REMOVE_ASSESSMENT_FEE = "advisor/assessmentFee/removeAssessmentFee";
	public static final String ADD_ASSESSMENT_FEE_PATH = "addAssessmentFee";
	private static final String CHANGE_ASSESSMENT_FEE_PATH = "changeAssessmentFee";
	private static final String ORG_SPRINGFRAMEWORK_WEB_SERVLET_HANDLER_MAPPING_BEST_MATCHING_PATTERN = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
	private static final String ACTION_NAME = "ACTION_NAME";
	private static final String ADD_ASSESSMENT_FEE_FROM_TRAVEL_EXP_SUMMARY = "ADD_ASSESSMENT_FEE_FROM_TRAVEL_EXP_SUMMARY";
	private static final String DASHBOARD_ADD_ACTION = "DASHBOARD_ADD_ACTION";
	private static final String SAVE_ALLOWANCES_ACTION = "SAVE_ALLOWANCES_ACTION";
	private static final String ADD_ASSESSMENT_FEE_FROM_SUMMARY = "ADD_ASSESSMENT_FEE_FROM_SUMMARY";
	private static final String ADVISOR_ASSESSMENT_FEE_SUMMARY = "advisor/assessmentFee/assessmentFeeSummary";
	private static final String ADVISOR_ADD_ASSESSMENT_FEE_PAGE = "advisor/assessmentFee/addAssessmentFee";
	private static final String ASSESSOR_NAME = "assessorName";
	private static final String ASSESSMENT_FEE_CENTRE_NAME = "assessmentFeeCentreName";

	private static final String TOTAL_HOURS = "totalHours";
	private static final String ASSESSMENT_FEE_FORM_VO = "assessmentFeeFormVO";
	private static final String ASSESSMENT_FIELD_INVALID = "assessmentFee.%s.invalid";
	public static final String SHOW_SKIP_ASSESSMENT_FEE = "showSkipAssessmentFeeLink";
	private final AssessmentFeeService assessmentFeeService;
	private final ApplicationService applicationService;

	public AssessmentFeeController(AssessmentFeeService assessmentFeeService, ApplicationService applicationService) {
		this.assessmentFeeService = assessmentFeeService;
		this.applicationService = applicationService;

	}

	private final Logger logger = LogManager.getLogger(this.getClass());

	@PostMapping(ASSESSMENT_FEE_SUMMARY)
	public String assessmentFeeSummary(Model model, HttpServletRequest request,
									   @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
									   BindingResult bindingResult) throws Exception {
		logger.info("nmphSummary Path: {}, form data: {}", NMPH_SUMMARY_PATH, keyDataVO);

		if (securityContext() == null) {
			return DSAConstants.LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		boolean hasValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());
		if (hasValues) {

			AllowancesHelper.setAssessmentFeeSummaryData(model, assessmentFeeService,
					keyDataVO.getDsaApplicationNumber());
			AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
					keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());

			view = ADVISOR_ASSESSMENT_FEE_SUMMARY;
		}
		logger.info("View or redirecting to {}", view);
		return view;
	}

	@PostMapping("completeAssessmentFee")
	public String completeAssessmentFee(Model model, HttpServletRequest request,
										@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO,
										BindingResult bindingResult) throws Exception {
		if (securityContext() == null) {
			return DSAConstants.LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("nmphSummary Path: {}, form data: {}", NMPH_SUMMARY_PATH, keyDataVO);
		String view = ERROR_PAGE;
		boolean hasValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());
		if (hasValues) {

			if (getAssessmentItems(keyDataVO.getDsaApplicationNumber()).size() == 0) {
				ServiceUtil.setSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(),
						Section.NEEDS_ASSESSMENT_FEE, SectionStatus.STARTED);
				view = AllowancesHelper.showNeedsAssessmentFeeInitialPage(request);
			} else {

				ServiceUtil.updateSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(),
						Section.NEEDS_ASSESSMENT_FEE, SectionStatus.COMPLETED);
				ServiceUtil.updateSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(),
						Section.ADVISOR_DECLARATION, SectionStatus.NOT_STARTED);
//				view = AllowancesHelper.showDeclrationsInitialPage(request);
				view = AllowancesHelper.showAdditionalInfoPage(request);
			}

		}
		logger.info("View or redirecting to {}", view);
		return view;
	}

	@PostMapping(path = {ADD_ASSESSMENT_FEE_PATH, CHANGE_ASSESSMENT_FEE_PATH})
	public String addAssessmentFee(Model model, @RequestParam(value = ACTION, required = true) String action,
								   HttpServletRequest request,
								   @Valid @ModelAttribute(name = ASSESSMENT_FEE_FORM_VO) AssessmentFeeFormVO assessmentFormVO,
								   BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		if (securityContext() == null) {
			return DSAConstants.LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("in add assessmentFee   call action: {}, request: {}", action, assessmentFormVO);
		long dsaApplicationNumber = assessmentFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = assessmentFormVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);

		String attribute = (String) request
				.getAttribute(ORG_SPRINGFRAMEWORK_WEB_SERVLET_HANDLER_MAPPING_BEST_MATCHING_PATTERN);
		if (attribute.contains(CHANGE_ASSESSMENT_FEE_PATH)) {
			model.addAttribute(ACTION_NAME, CHANGE_ASSESSMENT_FEE_PATH);
		} else {
			model.addAttribute(ACTION_NAME, ADD_ASSESSMENT_FEE_PATH);
			model.addAttribute(SHOW_SKIP_ASSESSMENT_FEE, true);
		}

		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
				case "INT_CHANGE":
					model.addAttribute(ASSESSMENT_FEE_FORM_VO, getAssessmentItem(assessmentFormVO.getId()));
					view = ADVISOR_ADD_ASSESSMENT_FEE_PAGE;
					break;
				case ADD_ASSESSMENT_FEE_FROM_SUMMARY:
				case "COMPLETE_ASSESSMENT_FEE":
					model.addAttribute(ASSESSMENT_FEE_FORM_VO, new AssessmentFeeFormVO());
					view = ADVISOR_ADD_ASSESSMENT_FEE_PAGE;
					break;
				case SAVE_ALLOWANCES_ACTION:
				case DASHBOARD_ADD_ACTION:
				case ADD_ASSESSMENT_FEE_FROM_TRAVEL_EXP_SUMMARY:
					boolean hasAssessments = !getAssessmentItems(assessmentFormVO.getDsaApplicationNumber()).isEmpty();
					if (hasAssessments) {
						view = AllowancesHelper.showAssessmentFeeSummary(request);
					} else {
						model.addAttribute(ASSESSMENT_FEE_FORM_VO, new AssessmentFeeFormVO());

						view = ADVISOR_ADD_ASSESSMENT_FEE_PAGE;
					}
					break;
				case DSAConstants.SAVE_AND_CONTINUE_ACTION:
					view = performAction(model, request, assessmentFormVO, bindingResult, dsaApplicationNumber);
					ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE,
							SectionStatus.COMPLETED);
					ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.ADVISOR_DECLARATION,
							SectionStatus.NOT_STARTED);
					break;

				case SKIP_ACTION:
				case "SKIP_TO_ADDITIONAL_INFO":
					ServiceUtil.setSectionStatus(applicationService, dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE,
							SectionStatus.SKIPPED);
					ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.ADVISOR_DECLARATION,
							SectionStatus.NOT_STARTED);
//					view = AllowancesHelper.showDeclrationsInitialPage(request);
					view = AllowancesHelper.showAdditionalInfoPage(request);
					break;
				case DASHBOARD_ACTION:
					view = AllowancesHelper.showDashboardPage(request);
					break;
				case BACK_ACTION:
					view = AllowancesHelper.showAllowancesSummary(request);
					break;
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}
		logger.info("View or redirecting to {}", view);
		return view;
	}

	@PostMapping("removeAssessmentFee")
	public String removeNMPHItem(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
								 @ModelAttribute(name = NMPHController.REMOVE_ITEM_FORM_VO) RemoveItemFormVO removeItemFormVO,
								 BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return DSAConstants.LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("in Remove nmph allowances call action: {}, request: {}", action, removeItemFormVO);
		hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());

		String view = ERROR_PAGE;
		switch (action.toUpperCase()) {
			case REMOVE_ACTION:
				model.addAttribute(NMPHController.REMOVE_ITEM_FORM_VO, removeItemFormVO);
				view = ADVISOR_ASSESSMENT_FEE_REMOVE_ASSESSMENT_FEE;
				break;
			case BACK_ACTION:
				view = AllowancesHelper.showAssessmentFeeSummary(request);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			case SAVE_AND_CONTINUE_ACTION:
				String removeItem = removeItemFormVO.getRemoveItem();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						assessmentFeeService.deleteItem(Long.valueOf(removeItemFormVO.getItemId()));
						if (getAssessmentItems(removeItemFormVO.getDsaApplicationNumber()).size() == 0) {
							// After deleting all items make sure set back the status to started
							ServiceUtil.setSectionStatus(applicationService, removeItemFormVO.getDsaApplicationNumber(),
									Section.NEEDS_ASSESSMENT_FEE, SectionStatus.STARTED);
						}
					}
					view = AllowancesHelper.showAssessmentFeeSummary(request);
				} else {
					bindingResult.rejectValue(REMOVE_ITEM, ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(NMPHController.REMOVE_ITEM_FORM_VO, removeItemFormVO);
					view = ADVISOR_ASSESSMENT_FEE_REMOVE_ASSESSMENT_FEE;
				}
				break;
			default:
				addErrorMessage(model, action, request);
				break;
		}

		return view;
	}

	private List<AssessmentFeeVO> getAssessmentItems(long dsaApplicationNumber) {
		return assessmentFeeService.getAssessmentItems(dsaApplicationNumber);
	}

	private AssessmentFeeFormVO getAssessmentItem(long id) throws IllegalAccessException {
		AssessmentFeeVO assessmentItem = assessmentFeeService.getAssessmentItem(id);
		return voToFormData(assessmentItem);
	}

	private AssessmentFeeFormVO voToFormData(AssessmentFeeVO assessmentItem) {
		AssessmentFeeFormVO form = new AssessmentFeeFormVO();
		form.setId(assessmentItem.getId());
		form.setDsaApplicationNumber(assessmentItem.getDsaApplicationNumber());
		form.setStudentReferenceNumber(assessmentItem.getStudentReferenceNumber());
		form.setAssessmentFeeCentreName(assessmentItem.getAssessmentFeeCentreName());
		form.setAssessorName(assessmentItem.getAssessorName());

		form.setTotalHours(assessmentItem.getTotalHours().toString());
		form.setCost(AllowancesHelper.formatValue(assessmentItem.getCost(), 2).toString());
		return form;
	}

	private String performAction(Model model, HttpServletRequest request, AssessmentFeeFormVO assessmentFormVO,
								 BindingResult bindingResult, long dsaApplicationNumber) throws IllegalAccessException {
		String view;
		validateFormData(bindingResult, assessmentFormVO);
		if (bindingResult.hasErrors()) {

			view = ADVISOR_ADD_ASSESSMENT_FEE_PAGE;
			model.addAttribute(ASSESSMENT_FEE_FORM_VO, assessmentFormVO);
		} else {
			assessmentFeeService.addAssessmentFee(AssessmentFeeVO.builder().id(assessmentFormVO.getId())
					.dsaApplicationNumber(assessmentFormVO.getDsaApplicationNumber())
					.studentReferenceNumber(assessmentFormVO.getStudentReferenceNumber())
					.assessmentFeeCentreName(assessmentFormVO.getAssessmentFeeCentreName())
					.assessorName(assessmentFormVO.getAssessorName())
					.totalHours(Integer.valueOf(assessmentFormVO.getTotalHours()))

					.cost(AllowancesHelper.stringToBigDecimal(assessmentFormVO.getCost())).build());
			view = AllowancesHelper.showAssessmentFeeSummary(request);
		}
		return view;
	}

	private void validateFormData(BindingResult bindingResult, @Valid AssessmentFeeFormVO assessmentFormVO) {

		ValidationHelper.validateFieldValue(bindingResult, ASSESSMENT_FEE_CENTRE_NAME,
				assessmentFormVO.getAssessmentFeeCentreName(), ASSESSMENT_FIELD_INVALID);
		ValidationHelper.validateFieldValue(bindingResult, ASSESSOR_NAME, assessmentFormVO.getAssessorName(),
				ASSESSMENT_FIELD_INVALID);
		if (!bindingResult.hasFieldErrors(TOTAL_HOURS)) {
			assessmentFormVO.setTotalHours(AllowancesHelper.validateNumber(bindingResult, TOTAL_HOURS,
					assessmentFormVO.getTotalHours(), TOTAL_HOURS, String.format(ASSESSMENT_FIELD_INVALID, TOTAL_HOURS),
					NumberType.NUMBER_XX, TWO_DIG_NO_REGEX));
		}

		if (!bindingResult.hasFieldErrors(COST)) {
			assessmentFormVO.setCost(AllowancesHelper.validateNumber(bindingResult, COST, assessmentFormVO.getCost(),
					COST, String.format(ASSESSMENT_FIELD_INVALID, COST), NumberType.COST_XXXXX_YY, COST_REGEX));
		}
	}
}
