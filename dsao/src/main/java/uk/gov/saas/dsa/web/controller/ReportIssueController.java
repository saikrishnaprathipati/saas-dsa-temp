package uk.gov.saas.dsa.web.controller;

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
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.model.IssueType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.ApplicationSectiponStatusVO;
import uk.gov.saas.dsa.vo.ReportIssueFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.model.Section.ADVISOR_DECLARATION;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.matches;

@Controller
public class ReportIssueController {
	private final Logger logger = LogManager.getLogger(this.getClass());

	public static final String REPORT_ISSUE_FORM_VO = "reportIssueFormVO";

	private static final String REPORT_ISSUE_DETAILS_URI = "reportIssueDetails";
	private static final String REPORT_ISSUE_URI = "reportIssue";
	private static final String REPORT_ISSUE = "REPORT_ISSUE";
	private static final String REPORT_ISSUE_VIEW = "student/issueReport.html";
	private static final String ISSUE_SUBMITTED_VIEW = "student/issueSubmitted.html";
	private static final String ISSUE_NOT_LISTED_TEXT_REQUIRED = "issue.notListedText.required";
	private static final String ISSUE_NOT_LISTED_TEXT_INVALID = "issue.notListedText.invalid";
	private static final String ISSUE_TYPE = "issueType";
	private static final String NOT_LISTED_TEXT = "notListedText";
	private static final String NOT_LISTED_TEXT_PATTERN = "[\\x00-\\x7F]+";
	private static final String ACADEMIC_YEAR = "academicYear";
	private static final String STUDENT_EMAIL_TEMPLATE = "mail-templates/issueReport/issueReportStudent.html";
	private static final String ADVISOR_EMAIL_TEMPLATE = "mail-templates/issueReport/issueReportHei.html";
	private static final String EMAIL_SUBJECT = "Disabled Students' Allowance (DSA) Application - Report a problem";
	private static final String ISSUE_TYPE_NAME = "issueTypeName";
	private static final String ISSUE_TYPE_DESCRIPTION = "issueTypeDescription";
	private static final String ISSUE_TYPE_INLINE_TEXT = "issueTypeInlineText";
	private static final String ADVISOR_FULL_NAME = "ADVISOR_FULL_NAME";

	private final ApplicationService applicationService;
	private final MessageSource messageSource;
	private final EmailSenderService emailSenderService;
	private final FindStudentService findStudentService;
	private final DsaAdvisorRepository dsaAdvisorRepository;

	public ReportIssueController(ApplicationService applicationService, MessageSource messageSource,
								 EmailSenderService emailSenderService, FindStudentService findStudentService, DsaAdvisorRepository dsaAdvisorRepository) {
		this.applicationService = applicationService;
		this.messageSource = messageSource;
		this.emailSenderService = emailSenderService;
		this.findStudentService = findStudentService;
		this.dsaAdvisorRepository = dsaAdvisorRepository;
	}

	@PostMapping(REPORT_ISSUE_DETAILS_URI)
	public String initReportIssue(Model model, @RequestParam(value = ACTION) String action,
								  @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO) throws IllegalAccessException {
		logger.info("init report issue {}", keyDataVO);
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());

		model.addAttribute(REPORT_ISSUE_FORM_VO, new ReportIssueFormVO());
		if (hasMandatoryValues && action.equalsIgnoreCase(REPORT_ISSUE)) {
			return REPORT_ISSUE_VIEW;
		}

		return ERROR_PAGE;
	}

	@PostMapping(REPORT_ISSUE_URI)
	public String reportIssue(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
							  @Valid @ModelAttribute(name = REPORT_ISSUE_FORM_VO) ReportIssueFormVO reportIssueFormVO,
							  BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, reportIssueFormVO.getDsaApplicationNumber(),
				reportIssueFormVO.getStudentReferenceNumber());

		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
				case SAVE_AND_CONTINUE_ACTION:
					validateIssueType(reportIssueFormVO, bindingResult);
					validateOtherOption(reportIssueFormVO, bindingResult);
					model.addAttribute(REPORT_ISSUE_FORM_VO, reportIssueFormVO);
					if (bindingResult.hasErrors()) {
						return REPORT_ISSUE_VIEW;
					}

					// Update advisor and student declaration status
					applicationService.setSectionStatus(reportIssueFormVO.getDsaApplicationNumber(),
							Section.ADVISOR_DECLARATION, SectionStatus.PENDING);
					applicationService.setSectionStatus(reportIssueFormVO.getDsaApplicationNumber(),
							Section.STUDENT_DECLARATION, SectionStatus.CANNOT_START_YET);

					sendEmails(reportIssueFormVO);

					return ISSUE_SUBMITTED_VIEW;
				case DASHBOARD_ACTION:
					return AllowancesHelper.showDashboardPage(request);
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}

		return ERROR_PAGE;
	}

    private void validateIssueType(ReportIssueFormVO reportIssueFormVO, BindingResult bindingResult) {
        if (Objects.equals(reportIssueFormVO.getIssueType(), "")) {
            ValidationHelper.addError(bindingResult, messageSource, reportIssueFormVO.getClass().getName(),
                    ISSUE_TYPE, GENERIC_MESSAGE_ERROR);
        }
    }

	private void populateModelData(Model model, long dsaApplicationNumber,
								   long studentReferenceNumber, ReportIssueFormVO reportIssueFormVO) throws IllegalAccessException {
		DSAApplicationsMade applicationsMade = applicationService.
				findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber, studentReferenceNumber);

		model.addAttribute(DSA_APPLICATION_NUMBER, dsaApplicationNumber);
		model.addAttribute(STUDENT_REFERENCE_NUMBER, studentReferenceNumber);
		model.addAttribute(REPORT_ISSUE_FORM_VO, reportIssueFormVO);
		model.addAttribute(HEI_NAME, getHEIName(dsaApplicationNumber));
		model.addAttribute(ACADEMIC_YEAR, getAcademicYear(studentReferenceNumber, applicationsMade.getSessionCode()));
	}

	// Validate other selection option
	private void validateOtherOption(ReportIssueFormVO reportIssueFormVO, BindingResult bindingResult) {
		boolean hasText = StringUtils.hasText(reportIssueFormVO.getNotListedText());
		boolean otherSelected = Objects.equals(reportIssueFormVO.getIssueType(), "OTHER");
		if (!hasText && otherSelected) {
			ValidationHelper.addError(bindingResult, messageSource, reportIssueFormVO.getClass().getName(),
					NOT_LISTED_TEXT, ISSUE_NOT_LISTED_TEXT_REQUIRED);
		} else if (otherSelected &&
				!matches(Pattern.compile(NOT_LISTED_TEXT_PATTERN), reportIssueFormVO.getNotListedText())) {
			ValidationHelper.addError(bindingResult, messageSource, reportIssueFormVO.getClass().getName(),
					NOT_LISTED_TEXT, ISSUE_NOT_LISTED_TEXT_INVALID);
		}
	}

	private String getHEIName(long dsaApplicationNumber) {
		ApplicationSectiponStatusVO status = applicationService.getApplicationSectionStatus(dsaApplicationNumber, ADVISOR_DECLARATION);
		String advisorEmail = status.getLastUpdatedBy();
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(advisorEmail);
		return dsaAdvisor.getInstitution();
	}

	private String getAcademicYear(long studentReferenceNumber, int sessionCode) throws IllegalAccessException {
		StudentResultVO studentResultVO = AllowancesHelper.getStudentResultWithSuid(findStudentService, studentReferenceNumber, sessionCode);
		return studentResultVO.getStudentCourseYear().getAcademicYearFull();
	}

	// Send email to advisor and student
	private void sendEmails(ReportIssueFormVO reportIssueFormVO) throws IllegalAccessException {
		String issueType = reportIssueFormVO.getIssueType();
		IssueType issueTypeEnum = IssueType.getIssueTypeByValue(issueType).orElse(null);

		DSAApplicationsMade applicationsMade = applicationService.
				findByDsaApplicationNumberAndStudentReferenceNumber(reportIssueFormVO.getDsaApplicationNumber(), reportIssueFormVO.getStudentReferenceNumber());

		if (issueTypeEnum != null) {
			StudentResultVO studentResultVO = AllowancesHelper.getStudentResultWithSuid(findStudentService,
					reportIssueFormVO.getStudentReferenceNumber(), applicationsMade.getSessionCode());

			HashMap<String, Object> modelMap = new HashMap<>();
			modelMap.put(SAAS_REFERNECE_NUMBER, reportIssueFormVO.getStudentReferenceNumber());
			modelMap.put(ACADEMIC_YEAR, getAcademicYear(reportIssueFormVO.getDsaApplicationNumber(), reportIssueFormVO.getStudentReferenceNumber()));
			modelMap.put(STUDENT_FULL_NAME, studentResultVO.getFirstName() + " " + studentResultVO.getLastName());
			modelMap.put(ISSUE_TYPE_NAME, issueTypeEnum.getName());

			// OTHER or ENUM description
			if (Objects.equals(issueTypeEnum.getCode(), "OTHER")) {
				modelMap.put(ISSUE_TYPE_DESCRIPTION, reportIssueFormVO.getNotListedText());
			} else {
				modelMap.put(ISSUE_TYPE_DESCRIPTION, issueTypeEnum.getDescription());
			}

			modelMap.put(ISSUE_TYPE_INLINE_TEXT, issueTypeEnum.getInlineMailText());

			// Send Advisor Email
			DSAApplicationsMade dsaApplicationsMade = applicationService
					.findByDsaApplicationNumberAndStudentReferenceNumber(reportIssueFormVO.getDsaApplicationNumber(), reportIssueFormVO.getStudentReferenceNumber());
			if (dsaApplicationsMade != null) {
				String advisorEmail = dsaApplicationsMade.getCreatedBy();
				DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(advisorEmail);
				String dsaFullName = dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName();
				modelMap.put(ADVISOR_FULL_NAME, dsaFullName);
				emailSenderService.sendEmailNotification(new String[]{advisorEmail}, new String[]{}, EMAIL_SUBJECT, ADVISOR_EMAIL_TEMPLATE, modelMap);
			}

			// Send Student Email
			emailSenderService.sendEmailNotification(studentResultVO, EMAIL_SUBJECT, STUDENT_EMAIL_TEMPLATE, modelMap);
		} else {
			logger.error("The IssueType was null!");
		}
	}

	private String getAcademicYear(long dsaApplicationNumber, long studentReferenceNumber) throws IllegalAccessException {
		ApplicationResponse applicationResponse = applicationService.findApplication(dsaApplicationNumber, studentReferenceNumber);
		return applicationResponse.getAcademicYear();
	}
}
