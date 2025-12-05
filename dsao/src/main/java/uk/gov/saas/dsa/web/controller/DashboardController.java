package uk.gov.saas.dsa.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import uk.gov.saas.dsa.domain.DSAAwardAccess;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaStudentAuthDetails;
import uk.gov.saas.dsa.domain.helpers.EncryptionHelper;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.domain.readonly.StudSession;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.model.OverallApplicationStatus;
import uk.gov.saas.dsa.service.*;
import uk.gov.saas.dsa.service.award.DSAAwardService;
import uk.gov.saas.dsa.service.notification.NotificationUtil;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DashboardFormVO;
import uk.gov.saas.dsa.vo.LoginFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.award.DSAAwardVO;
import uk.gov.saas.dsa.vo.withdraw.WithdrawPreSubmittedApplicationFormVO;
import uk.gov.saas.dsa.web.config.DsaAuthenticationProvider;
import uk.gov.saas.dsa.web.helper.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static uk.gov.saas.dsa.model.OverallApplicationStatus.*;
import static uk.gov.saas.dsa.service.ServiceUtil.capitalizeFully;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

/**
 * Dashboard controller
 */
@Controller
public class DashboardController {

	private static final String CAN_SHOW_AWARD_STATUS_MESSAGE = "canShowAwardStatusMessage";
	private static final String CAN_ADVISOR_SEE_AWARD = "canAdvisorSeeAward";
	private static final String CAN_SHOW_PREVIOUS_AWARD_STATUS_MESSAGE = "canShowAwardStatusMessage";
	private static final String CAN_ADVISOR_SEE_PREVIOUS_AWARD = "canAdvisorSeePreviousAward";
	private static final String CAN_SHOW_APPLICATION_STATUS_MESSAGE = "canShowApplicationStatusMessage";
	private static final String AWARD_DETAILS = "AWARD_DETAILS";
	private static final String PREVIOUS_AWARD_DETAILS = "PREVIOUS_AWARD_DETAILS";
	private static final String DASHBOARD_DATA = "dashboardData";
	private static final String DASHBOARD_FORM_VO = "dashboardFormVO";
	private static final String PREVIOUS_YEAR_FORM_VO = "previousYearFormVO";
	private static final String ADVISOR_APPLICATION_DASHBOARD = "advisor/applicationDashboard";
	private static final String ADVISOR_SELECT_YEAR = "advisor/selectYear";
	private static final String CONFIRM_RESUBMIT_APPLICATION = "advisor/confirmResubmitApplication";
	private static final String ADVISOR_PERMISSION_DENIED_PAGE = "advisor/permissionDenied";
	private static final String STUDENT_NOT_ELIGIBLE_PAGE = "advisor/studentNotEligible";
	private static final String START_APPLICATION_PAGE = "advisor/startApplication";
	private static final String RESUME_APPLICATION_PAGE = "advisor/resumeApplication";
	private static final String PAGE_NOT_AVAILABLE = "register/pageNotAvailable";
	private static final String SCOT_HEI = "1";
	private static final String YES = "Y";
	private static final String OLS_APPLICATION_REJECTED = "R";
	private static final String SELECT_YEAR_SUBMIT_ERROR = "selectYear.submit.error";
	private static final String ACADEMIC_YEAR_FIELD = "academicYear";
	public static final String WITHDRAW_PRE_SUBMITTED_APPLICATION = "/withdrawPreSubmitApplication";
	public static final String WITHDRAW_APPLICATION_HTML = "advisor/withdraw/withdrawPreSubmit";
	public static final String WITHDRAW_PRE_SUBMITTED_APPLICATION_FORM_VO = "withdrawPreSubmittedApplicationFormVO";
	private static final String ADVISOR_DASHBOARD = "/advisorDashboard";
	private static final String STUDENT_DSA_DASHBOARD = "/studentDsaDashboard";
	private static final String STUDENT_DASHBOARD = "/studentDashboard";
	private static final String DSA_APPLICATION = "/dsaApplication";
	private static final String TOKEN = "token";
	private static final String DASHBOARD = "studentDashboard";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final FindStudentService findStudentService;
	private final ApplicationService applicationService;
	private final AdvisorLookupService advisorLookupService;
	private final StudentLookupService studentLookupService;
	private final ApplicationStatusService applicationStatusService;
	private final DSAAwardService dsaAwardService;
	private final DisabilitiesService disabilitiesService;
	private final ConfigDataService configDataService;
	private final AwardAccessService awardAccessService;

	@Autowired
	private DsaAuthenticationProvider dsaAuthenticationProvider;

	/**
	 * Constructor
	 *
	 * @param findStudentService   Find Student Service
	 * @param applicationService   Application Service
	 * @param advisorLookupService Advisor Lookup Service
	 */
	public DashboardController(FindStudentService findStudentService, ApplicationService applicationService,
			AdvisorLookupService advisorLookupService, StudentLookupService studentLookupService,
			ApplicationStatusService applicationStatusService, DSAAwardService dsaAwardService,
			DisabilitiesService disabilitiesService, ConfigDataService configDataService,
							   AwardAccessService awardAccessService) {
		this.findStudentService = findStudentService;
		this.applicationService = applicationService;
		this.advisorLookupService = advisorLookupService;
		this.studentLookupService = studentLookupService;
		this.applicationStatusService = applicationStatusService;
		this.dsaAwardService = dsaAwardService;
		this.disabilitiesService = disabilitiesService;
		this.configDataService = configDataService;
		this.awardAccessService = awardAccessService;
	}

	/**
	 * to get the application full details
	 *
	 * @param model     Application Model
	 * @param keyDataVO ApplicationKeyDataFormVO
	 * @return html Template with dashboard data.
	 */
	@PostMapping("/applicationDashboard")
	public String applicationDetails(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO,
			HttpSession httpSession) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("ApplicationKeyDataFormVO request: {}", keyDataVO);

		ApplicationResponse dashboardData;

		try {
			dashboardData = applicationService.findApplication(keyDataVO.getDsaApplicationNumber(),
					keyDataVO.getStudentReferenceNumber());

			// Student details
			FindStudentHelper.setStudentDetails(findStudentService, dashboardData);
		} catch (IllegalAccessException e) {
			model.addAttribute(DSAConstants.ERROR_MESSAGE, e.getMessage());
			return "error";
		}

		model.addAttribute(DASHBOARD_DATA, dashboardData);

		return ADVISOR_APPLICATION_DASHBOARD;
	}

	/**
	 * @param model           Application Model
	 * @param dashboardFormVO Dashboard Form VO
	 * @return Page template
	 */
	@PostMapping("/startResumeApplication")
	public String startResumeApplication(Model model, @Valid @ModelAttribute DashboardFormVO dashboardFormVO,
										 HttpSession httpSession) throws IllegalAccessException {
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			logger.info("no security context found");
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("startResumeApplication  DSADashboardFormVO request: {}", dashboardFormVO);
		int dashboardFormSession = dashboardFormVO.getSessionCode();

		DsaAdvisor dsaAdvisor = advisorLookupService
				.findByEmail(securityContext.getAuthentication().getPrincipal().toString());
		int currentActiveSession = ConfigDataService.getCurrentActiveSession();
		int previousSession = currentActiveSession-1;

		if (dsaAdvisor != null) {
			long studentReferenceNumber = dashboardFormVO.getStudentReferenceNumber();
			StudentResultVO studentResult = getStudentResult(studentReferenceNumber, currentActiveSession);

			if(null == studentResult || null == studentResult.getFirstName()) {
				studentResult = getStudentResult(studentReferenceNumber, previousSession);
			}

			dashboardFormVO.setRoleName(dsaAdvisor.getRoleName());
			dashboardFormVO.setFundingEligibilityStatus(studentResult.getFundingEligibilityStatus());

			boolean fromTheSameInstitution = isLoggedInAdvisorUserIsFromTheSameInstitution(dsaAdvisor, studentResult);
			if (fromTheSameInstitution) {
				ApplicationResponse dashboardData;
				try {
					logger.info("startResumeApplication findApplication and {} to show {}", studentReferenceNumber,currentActiveSession);

					dashboardData = applicationService
							.findApplicationByStudentReferenceNumberAnSessionCode(studentReferenceNumber, currentActiveSession);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				AllowancesHelper.setStudentRefInSession(httpSession, studentReferenceNumber, studentResult.getSuid());
				dashboardFormVO.setNewApplication(dashboardData.isNewApplication());
				dashboardFormVO.setDsaApplicationNumber(dashboardData.getDsaApplicationNumber());

				//Default to current active session
				dashboardFormVO.setAcademicYear(currentActiveSession + " to " + (currentActiveSession + 1));
				dashboardFormVO.setSessionCode(currentActiveSession);

				// Overall application status
				if(!dashboardData.isNewApplication()) {
					OverallApplicationStatus deriveOverallStatus = deriveOverallStatus(studentReferenceNumber, currentActiveSession);
					dashboardData.setOverallApplicationStatus(deriveOverallStatus);
					dashboardFormVO.setApplicationStatus(deriveOverallStatus);
					dashboardFormVO.setApplicationUpdated(dashboardData.getApplicationUpdated());

					//set advisor declaration status
					dashboardFormVO.setAdvisorDeclaration(dashboardData.getSectionStatusData().getAdvisorDeclarationSectionData().getSectionStatus().getCode());
				}
				// Populate model
				model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);
				model.addAttribute(CAN_SHOW_APPLICATION_STATUS_MESSAGE,
						canShowStatusMessage(dashboardFormVO.getApplicationUpdated())
								&& !Arrays.asList(NOT_STARTED, STARTED, SUBMITTED, AWARDED)
										.contains(dashboardFormVO.getApplicationStatus()));
				populateAwardData(model, dashboardFormVO, studentReferenceNumber, httpSession);

				boolean isStudentEligiblePreviousYear = checkStudentEligibility(studentReferenceNumber, dsaAdvisor, previousSession);
				boolean isStudentEligibleCurrentYear = checkStudentEligibility(studentReferenceNumber, dsaAdvisor, currentActiveSession);

				logger.info("Student eligible {} for session : {}", isStudentEligiblePreviousYear, previousSession);
				logger.info("Student eligible {} for session : {}", isStudentEligibleCurrentYear, currentActiveSession);

				boolean isEligibleStudent = (isStudentEligiblePreviousYear || isStudentEligibleCurrentYear);

				// Check student eligibility for current and previous year
				isStudentEligibleForCurrentAndPreviousSession(model, dashboardFormVO, dsaAdvisor);

				if(null == dashboardFormVO.getSessionCode()) {
					dashboardFormVO.setSessionCode(dashboardFormSession);
				}
				model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);

				if (isEligibleStudent) {
					boolean newApplication = dashboardData.isNewApplication();

					// Get previous year data
					if (isStudentEligiblePreviousYear) {
						AllowancesHelper.setPreviousYearEligibility(httpSession, true);
						DashboardFormVO previousFO = populatePreviousYearApplication(model, studentReferenceNumber, previousSession, httpSession);
						logger.info("previousYearFormVO : {}", previousFO);
						if (previousFO != null) {
							model.addAttribute(PREVIOUS_YEAR_FORM_VO, previousFO);

							// Show resume page if there is a previous application
							if (!previousFO.isNewApplication()) {
								return RESUME_APPLICATION_PAGE;
							}
						}
					} else {
						AllowancesHelper.setPreviousYearEligibility(httpSession, false);
					}

					if (newApplication) {
						logger.info("This is the New DSA application for student ref no : {}", studentReferenceNumber);
						return START_APPLICATION_PAGE;
					} else {
						logger.info("This is an existing DSA application for student ref no : {}",
								studentReferenceNumber);
						return RESUME_APPLICATION_PAGE;
					}
				} else {
					logger.info("This student ref no : {} is not eligible for DSA Application", studentReferenceNumber);
					return STUDENT_NOT_ELIGIBLE_PAGE;
				}
			} else {
				logger.info("Student ref no {} institution [{}]and advisor {} institution [{}] are not same, ",
						studentReferenceNumber, studentResult.getStudentCourseYear().getInstitutionName().toUpperCase(),
						dsaAdvisor.getFirstName(), dsaAdvisor.getInstitution().toUpperCase());

				return ADVISOR_PERMISSION_DENIED_PAGE;
			}

		}

		return DSAConstants.ERROR_PAGE;
	}

	/**
	 * Select Year to apply for.
	 *
	 * @param model           Application Model
	 * @param dashboardFormVO Dashboard Form VO
	 * @return HTML page with select year to apply for.
	 */
	@PostMapping("/selectYear")
	public String selectYear(Model model, @Valid @ModelAttribute DashboardFormVO dashboardFormVO,
			HttpSession httpSession) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("selectYear  DSADashboardFormVO request: {}", dashboardFormVO);

		model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);

		// Check student eligibility for current and previous year
		DsaAdvisor dsaAdvisor = advisorLookupService
				.findByEmail(Objects.requireNonNull(securityContext()).getAuthentication().getPrincipal().toString());
		isStudentEligibleForCurrentAndPreviousSession(model, dashboardFormVO, dsaAdvisor);

		AllowancesHelper.setStudentRefInSession(httpSession, dashboardFormVO.getStudentReferenceNumber(),
				dsaAdvisor.getUserId());
		return ADVISOR_SELECT_YEAR;
	}

	private void isStudentEligibleForCurrentAndPreviousSession(Model model, DashboardFormVO dashboardFormVO, DsaAdvisor dsaAdvisor) {
		if (dsaAdvisor != null) {
			int currentSessionCode = ConfigDataService.getCurrentActiveSession();
			int previousSessionCode = currentSessionCode - 1;

			boolean isStudentEligibleForPreviousSession =
					checkStudentEligibility(dashboardFormVO.getStudentReferenceNumber(), dsaAdvisor, previousSessionCode);
			logger.info("isStudentEligible {} for PreviousSession : {}", isStudentEligibleForPreviousSession, previousSessionCode);

			if (isStudentEligibleForPreviousSession) {
				model.addAttribute("previousYear", previousSessionCode + " to " + currentSessionCode);
			}

			boolean isStudentEligibleForCurrentSession =
					checkStudentEligibility(dashboardFormVO.getStudentReferenceNumber(), dsaAdvisor, currentSessionCode);
			logger.info("isStudentEligible {} for currentSession : {}", isStudentEligibleForCurrentSession, currentSessionCode);

			if (isStudentEligibleForCurrentSession) {
				model.addAttribute("currentYear", currentSessionCode + " to " + (currentSessionCode + 1));
			}
			model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);
		}
	}

	/**
	 * Initialising the dashboard page data.
	 *
	 * @param model           Application Model
	 * @param dashboardFormVO Dashboard Form VO
	 * @return HTML page with dashboard page data.
	 */
	@PostMapping("/initApplicationDashboard")
	public String initDashboardDetailsFromSearchStudent(Model model,
			@Valid @ModelAttribute DashboardFormVO dashboardFormVO, HttpSession httpSession,
			BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("initApplicationDashboard  DSADashboardFormVO request: {}", dashboardFormVO);

		// No option selected
		if (dashboardFormVO.getAcademicYear() == null) {
			ValidationHelper.rejectFieldValue(bindingResult, ACADEMIC_YEAR_FIELD, GENERIC_MESSAGE_ERROR);
			return CONFIRM_RESUBMIT_APPLICATION;
		}

		long studentReferenceNumber = dashboardFormVO.getStudentReferenceNumber();
		int sessionCode = dashboardFormVO.getSessionCode();

		// Check student eligibility for previous year
		DsaAdvisor dsaAdvisor = advisorLookupService
				.findByEmail(Objects.requireNonNull(securityContext()).getAuthentication().getPrincipal().toString());

		isStudentEligibleForCurrentAndPreviousSession(model, dashboardFormVO, dsaAdvisor);

		// Adjust session code for previous year
		int firstPartAcademicYear = Integer.parseInt(dashboardFormVO.getAcademicYear().trim().substring(0, 4));
		logger.info("firstPartAcademicYear and {} to show {}", firstPartAcademicYear,sessionCode);

		boolean currentYearSelected = true;
		if (firstPartAcademicYear != sessionCode) {
			logger.info("firstPartAcademicYear and {} to show {}", firstPartAcademicYear,sessionCode);

			sessionCode = sessionCode - 1;
			currentYearSelected = false;
			logger.info("currentYearSelected and {} to show {}", currentYearSelected,sessionCode);

		}

		// Search by session code and student reference
		ApplicationResponse dashboardData;
		try {
			logger.info("findApplicationByStudentReferenceNumberAnSessionCode and {} to show {}", studentReferenceNumber,sessionCode);
			dashboardData = applicationService
					.findApplicationByStudentReferenceNumberAnSessionCode(studentReferenceNumber, sessionCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (dashboardData.isNewApplication()) {
			dashboardData = applicationService.createApplication(studentReferenceNumber,
					dashboardFormVO.getFundingEligibilityStatus(), sessionCode);
		} else {
			// Derive overall status
			OverallApplicationStatus deriveOverallStatus = deriveOverallStatus(studentReferenceNumber, sessionCode);
			dashboardData.setOverallApplicationStatus(deriveOverallStatus);
			dashboardFormVO.setApplicationStatus(deriveOverallStatus);

			// When selecting year hack
			// If there is an application in db not proceed
			if (dashboardFormVO.getEmailAddress() != null) {
				ValidationHelper.rejectFieldValue(bindingResult, ACADEMIC_YEAR_FIELD, SELECT_YEAR_SUBMIT_ERROR);
				return CONFIRM_RESUBMIT_APPLICATION;
			}
		}

		dashboardData.setFirstName(dashboardFormVO.getFirstName());
		dashboardData.setLastName(dashboardFormVO.getLastName());
		dashboardData.setAcademicYear(dashboardFormVO.getAcademicYear());
		dashboardData.setSessionCode(sessionCode);
		model.addAttribute(DASHBOARD_DATA, dashboardData);
		if (currentYearSelected) {
			checkDisabilitiesForReview(dashboardData);
		}
		AllowancesHelper.setStudentRefInSession(httpSession, studentReferenceNumber, LoggedinUserUtil.getUserId());

		logger.info("initApplicationDashboard applicationResponse: {}", dashboardData);

		dashboardFormVO.setDsaApplicationNumber(dashboardData.getDsaApplicationNumber());

		return ADVISOR_APPLICATION_DASHBOARD;
	}

	@PostMapping("/updateApplicationDashboard")
	public String updateApplicationDashboard(Model model,
														@Valid @ModelAttribute DashboardFormVO dashboardFormVO, HttpSession httpSession,
														BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("updateApplicationDashboard dashboardFormVO: {}", dashboardFormVO);

		long studentReferenceNumber = dashboardFormVO.getStudentReferenceNumber();
		int sessionCode = dashboardFormVO.getSessionCode();

		// Check student eligibility for previous year
		DsaAdvisor dsaAdvisor = advisorLookupService
				.findByEmail(Objects.requireNonNull(securityContext()).getAuthentication().getPrincipal().toString());

		isStudentEligibleForCurrentAndPreviousSession(model, dashboardFormVO, dsaAdvisor);

		// Adjust session code for previous year
		int firstPartAcademicYear = Integer.parseInt(dashboardFormVO.getAcademicYear().trim().substring(0, 4));
		boolean currentYearSelected = true;
		if (firstPartAcademicYear != sessionCode) {
			sessionCode = sessionCode - 1;
			currentYearSelected = false;
		}

		// Search by session code and student reference
		ApplicationResponse dashboardData;
		try {
			logger.info("resetApplicationByStudentReferenceNumberAndSessionCode and {} to show {}",
					studentReferenceNumber, sessionCode);
			dashboardData = applicationService
					.resetApplicationByStudentReferenceNumberAndSessionCode(studentReferenceNumber, sessionCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (dashboardData.isNewApplication()) {
			dashboardData = applicationService.createApplication(studentReferenceNumber,
					dashboardFormVO.getFundingEligibilityStatus(), sessionCode);
		} else {
			// Derive overall status
			OverallApplicationStatus deriveOverallStatus = deriveOverallStatus(studentReferenceNumber, sessionCode);
			dashboardData.setOverallApplicationStatus(deriveOverallStatus);
			dashboardFormVO.setApplicationStatus(deriveOverallStatus);

			// When selecting year hack
			// If there is an application in db not proceed
			if (dashboardFormVO.getEmailAddress() != null) {
				ValidationHelper.rejectFieldValue(bindingResult, ACADEMIC_YEAR_FIELD, SELECT_YEAR_SUBMIT_ERROR);
				return CONFIRM_RESUBMIT_APPLICATION;
			}
		}

		dashboardData.setFirstName(dashboardFormVO.getFirstName());
		dashboardData.setLastName(dashboardFormVO.getLastName());
		dashboardData.setAcademicYear(dashboardFormVO.getAcademicYear());
		dashboardData.setSessionCode(sessionCode);
		model.addAttribute(DASHBOARD_DATA, dashboardData);
		if (currentYearSelected) {
			checkDisabilitiesForReview(dashboardData);
		}
		AllowancesHelper.setStudentRefInSession(httpSession, studentReferenceNumber, LoggedinUserUtil.getUserId());
		logger.info("initApplicationDashboard applicationResponse: {}", dashboardData);

		dashboardFormVO.setDsaApplicationNumber(dashboardData.getDsaApplicationNumber());
		return ADVISOR_APPLICATION_DASHBOARD;
	}

	/**
	 * Student navigating from OLS dashboard
	 */
	@GetMapping(DSA_APPLICATION)
	public String dsaApplication(@RequestParam String token, @RequestParam String suid, @RequestParam String studRefNum,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		logger.info("Redirect to dsaApplication ");
		redirectAttributes.addFlashAttribute("token", token);
		redirectAttributes.addFlashAttribute("suid", suid);
		redirectAttributes.addFlashAttribute(DSAConstants.STUDENT_REFERENCE_NUMBER, studRefNum);
		return REDIRECT + DASHBOARD;
	}

	/**
	 * Student details
	 */

	@GetMapping(STUDENT_DSA_DASHBOARD)
	public String studentDsaDashboard(Model model, HttpSession httpSession, RedirectAttributes redirectAttributes) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpSession);
		String suid = AllowancesHelper.getSuidFromSession(httpSession);

		redirectAttributes.addFlashAttribute(TOKEN, TOKEN);
		redirectAttributes.addFlashAttribute(SUID, suid);
		redirectAttributes.addFlashAttribute(DSAConstants.STUDENT_REFERENCE_NUMBER,
				String.valueOf(studentReferenceNumber));
		return REDIRECT + "studentDashboard";
	}

	@GetMapping(STUDENT_DASHBOARD)
	public String checkStudentDetails(Model model, HttpServletRequest request, String token, String suid,
									  String studentReferenceNumber, RedirectAttributes redirectAttributes, HttpSession httpSession) throws IllegalAccessException {
			Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
			if (inputFlashMap != null) {
				if (TOKEN.equalsIgnoreCase((String) inputFlashMap.get(TOKEN))) {
					token = (String) inputFlashMap.get(TOKEN);
					suid = (String) inputFlashMap.get(SUID);
					studentReferenceNumber = (String) inputFlashMap.get(DSAConstants.STUDENT_REFERENCE_NUMBER);
				} else {
					token = EncryptionHelper.decrypt((String) inputFlashMap.get(TOKEN));
					suid = EncryptionHelper.decrypt((String) inputFlashMap.get(SUID));
					studentReferenceNumber = EncryptionHelper
							.decrypt((String) inputFlashMap.get(DSAConstants.STUDENT_REFERENCE_NUMBER));
				}
			}

		long studentRefNumber = Long.parseLong(studentReferenceNumber);

		AllowancesHelper.setStudentRefInSession(httpSession, studentRefNumber, suid);
		logger.info("Check Student Details for studentReferenceNumber {} ", studentRefNumber);

		if (null == token || null == suid) {
			return PAGE_NOT_AVAILABLE;
		}

		DsaStudentAuthDetails dsaStudentAuthDetails = studentLookupService.findStudentBySuid(suid);

		if (null == dsaStudentAuthDetails || !dsaStudentAuthDetails.getIsLoggedIn()
				|| DateHelper.getCurrentDate().after(getLastLoggedInDate(dsaStudentAuthDetails))) {
			return REDIRECT + "studentLogout";
		}

		// Set security context
		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress(suid);

		setSecurityContext(request, loginFormVO, model);
		logger.info("studentDashboard securityContext: {}", securityContext());

		// populate application
		populateStudentDashboardData(model, studentRefNumber, ConfigDataService.getCurrentActiveSession(), httpSession);
		logger.info("populateStudentDashboardData model: {}", model);

		DashboardFormVO dashboardFormVO = (DashboardFormVO) model.getAttribute(DASHBOARD_FORM_VO);
		logger.info("dashboardFormVO  {}", dashboardFormVO);

		// Previous year data
		int previousSessionCode = ConfigDataService.getCurrentActiveSession() - 1;
		logger.info("previousSessionCode model: {}", previousSessionCode);

		if (checkEligibility(studentRefNumber, previousSessionCode)) {
			logger.info("Previous year eligible {}", previousSessionCode);

			DashboardFormVO previousFO = populatePreviousYearApplication(model, studentRefNumber, previousSessionCode, httpSession);
			logger.info("previousFO  {}", previousFO);

			if (previousFO != null) {
				model.addAttribute(PREVIOUS_YEAR_FORM_VO, previousFO);

				if (null == dashboardFormVO) {
					dashboardFormVO = new DashboardFormVO();
					dashboardFormVO.setFirstName(previousFO.getFirstName());
					dashboardFormVO.setLastName(previousFO.getLastName());
					dashboardFormVO.setStudentReferenceNumber(previousFO.getStudentReferenceNumber());
					dashboardFormVO.setNewApplication(true);
					model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);
				}
			}
		}
		return RESUME_APPLICATION_PAGE;
	}

	/**
	 * Advisor dashboard navigation
	 */
	@GetMapping(ADVISOR_DASHBOARD)
	public String advisorDashboard(Model model, HttpSession httpsession) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpsession);
		logger.info("Back to advisorDashboard for studentReferenceNumber: {}", studentReferenceNumber);
		int currentSession = ConfigDataService.getCurrentActiveSession();
		// Search for student
		StudentResultVO studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber, currentSession);
		if (studentResultVO.getStudentReferenceNumber() <= 0) {
			studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber, currentSession - 1);
		}
		if (studentResultVO.getStudentReferenceNumber() <= 0) {
			return REDIRECT + "findStudent";
		}

		DashboardFormVO dashboardFormVO = new DashboardFormVO();
		dashboardFormVO.setStudentReferenceNumber(studentReferenceNumber);
		dashboardFormVO.setFirstName(studentResultVO.getFirstName());
		dashboardFormVO.setLastName(studentResultVO.getLastName());
		dashboardFormVO.setDob(studentResultVO.getDob());
		dashboardFormVO.setApplicationUpdated(studentResultVO.getApplicationUpdated());

		if (studentResultVO.getStudentCourseYear() != null) {
			dashboardFormVO.setInstitutionName(studentResultVO.getStudentCourseYear().getInstitutionName());
			dashboardFormVO.setSessionCode(studentResultVO.getStudentCourseYear().getSessionCode());
			dashboardFormVO.setAcademicYear(studentResultVO.getStudentCourseYear().getAcademicYearFull());
			dashboardFormVO.setFundingEligibilityStatus(studentResultVO.getFundingEligibilityStatus());
		}

		return startResumeApplication(model, dashboardFormVO, httpsession);
	}

	/**
	 * Withdraw PRE-SUBMITTED application
	 */
	@PostMapping(WITHDRAW_PRE_SUBMITTED_APPLICATION)
	public String withdrawPreSubmitApplication(Model model, HttpServletRequest request,
			@ModelAttribute(name = WITHDRAW_PRE_SUBMITTED_APPLICATION_FORM_VO) WithdrawPreSubmittedApplicationFormVO withdrawPreSubmittedApplicationFormVO,
			@RequestParam(value = ACTION) String action, BindingResult bindingResult) throws IllegalAccessException {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("Withdraw request: {}", withdrawPreSubmittedApplicationFormVO);

		boolean hasMandatoryValues = hasMandatoryValues(model,
				withdrawPreSubmittedApplicationFormVO.getDsaApplicationNumber(),
				withdrawPreSubmittedApplicationFormVO.getStudentReferenceNumber());
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case "WITHDRAW_REDIRECT":
				model.addAttribute(WITHDRAW_PRE_SUBMITTED_APPLICATION_FORM_VO, withdrawPreSubmittedApplicationFormVO);
				return WITHDRAW_APPLICATION_HTML;
			case SAVE_AND_CONTINUE_ACTION:
				String doWithdraw = withdrawPreSubmittedApplicationFormVO.getDoWithdraw();
				if (AllowancesHelper.optionHasCorrectValue(doWithdraw)) {
					// set application to WITHDRAWN
					if (Objects.equals(withdrawPreSubmittedApplicationFormVO.getDoWithdraw(), "YES")) {
						doWithdraw(withdrawPreSubmittedApplicationFormVO.getDsaApplicationNumber(),
								withdrawPreSubmittedApplicationFormVO.getStudentReferenceNumber());
						return REDIRECT + "advisorDashboard";
					}
					return AllowancesHelper.showDashboardPage(request);
				} else {
					bindingResult.rejectValue("doWithdraw", "generic.message.option");
					model.addAttribute(WITHDRAW_PRE_SUBMITTED_APPLICATION_FORM_VO,
							withdrawPreSubmittedApplicationFormVO);
					return WITHDRAW_APPLICATION_HTML;
				}
			case BACK_ACTION:
				return AllowancesHelper.showDashboardPage(request);
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}

		return ERROR_PAGE;
	}

	/**
	 * Set application to withdraw
	 */
	private void doWithdraw(long dsaApplicationNumber, long studentReferenceNumber) {
		applicationService.updateOverallApplciationStatus(dsaApplicationNumber, studentReferenceNumber, WITHDRAWN);
	}

	/**
	 * Check Disabilities if they have to be reviewed. Requirements: Overall status
	 * is STARTED, disability section is NOT_STARTED and there is a previous
	 * application.
	 */
	private void checkDisabilitiesForReview(ApplicationResponse dashboardData) throws IllegalAccessException {
		if (Objects.equals(dashboardData.getOverallApplicationStatus().getCode(), "STARTED")) {
			if (dashboardData.getSectionStatusData().getDisabilitySectionData().getSectionStatus().getCode()
					.matches("NOT_STARTED")) {
				long dsaApplicationNumberSelected = dashboardData.getDsaApplicationNumber();

				// Find DSA application from previous session
				ApplicationResponse previousApplication;
				try {
					logger.info(
							"checkDisabilitiesForReview findApplicationByStudentReferenceNumberAnSessionCode and {} to currentApplicationFormVO {}",
							dsaApplicationNumberSelected, dashboardData.getSessionCode());

					previousApplication = applicationService.findApplicationByStudentReferenceNumberAnSessionCode(
							dashboardData.getStudentReferenceNumber(), dashboardData.getSessionCode() - 1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (!previousApplication.isNewApplication()) {
					long dsaApplicationNumberPrevious = previousApplication.getDsaApplicationNumber();
					disabilitiesService.copyOverDisabilities(dsaApplicationNumberPrevious, dsaApplicationNumberSelected,
							dashboardData.getStudentReferenceNumber());
				}
			}
		}
	}

	private boolean isLoggedInAdvisorUserIsFromTheSameInstitution(DsaAdvisor dsaAdvisor,
			StudentResultVO studentResult) {
		String advisorInstitution = dsaAdvisor.getInstitution().toUpperCase();

		String studentInstitution = studentResult.getStudentCourseYear().getInstitutionName().toUpperCase();
		boolean isSame = Objects.equals(advisorInstitution, studentInstitution);
		logger.info("advisorInstitution {} , studentInstitution {} are {}", advisorInstitution, studentInstitution,
				isSame ? "same" : "not same");
		return isSame;
	}

	// Check if student is eligible
	private boolean checkStudentEligibility(long studentReferenceNumber, DsaAdvisor dsaAdvisor, int sessionCode) {
		logger.info("Check Student Eligibility with {} for session : {}", studentReferenceNumber, sessionCode);

		String institutionCode = dsaAdvisor.getInstCode().toUpperCase();
		String locationIndicator = findStudentService.findInstitutionByInstCode(institutionCode).getLocationIndicator();
		StudCourseYear studCourseYear = findStudentService
				.findStudCourseYearByStudentReferenceNumberAndSessionCode(studentReferenceNumber, sessionCode);

		// Non-Scottish HEI
		if (!Objects.equals(locationIndicator, SCOT_HEI))
			return false;

		// No OLS Account
		if (findStudentService.findStudentPersonDetailsStudByRefNumber(studentReferenceNumber) == null)
			return false;

		// Is an EU Student
		StudSession studSession = findStudentService.findStudSessionByRefNumberAndSessionCode(studentReferenceNumber,
				sessionCode);
		if (studSession != null && Objects.equals(studSession.getEuFlag(), YES))
			return false;

		// No Main Funding
		if (studCourseYear == null)
			return false;

		// GA Apprenticeship
		if (Objects.equals(studCourseYear.getGaStudent(), YES)) {
			return false;
		}

		// OLS Rejected
		if (Objects.equals(studCourseYear.getApplicationStatus(), OLS_APPLICATION_REJECTED))
			return false;

		return true;
	}

	// Check student's eligiblity when student is logged in
	private boolean checkEligibility(long studentReferenceNumber, int sessionCode) {
		StudCourseYear studCourseYear = findStudentService
				.findStudCourseYearByStudentReferenceNumberAndSessionCode(studentReferenceNumber, sessionCode);

		// No OLS Account
		if (findStudentService.findStudentPersonDetailsStudByRefNumber(studentReferenceNumber) == null)
			return false;

		// Is an EU Student
		StudSession studSession = findStudentService.findStudSessionByRefNumberAndSessionCode(studentReferenceNumber,
				sessionCode);
		if (studSession != null && Objects.equals(studSession.getEuFlag(), YES))
			return false;

		// No Main Funding
		if (studCourseYear == null)
			return false;

		// GA Apprenticeship
		if (Objects.equals(studCourseYear.getGaStudent(), YES)) {
			return false;
		}

		// OLS Rejected
		if (Objects.equals(studCourseYear.getApplicationStatus(), OLS_APPLICATION_REJECTED)) {
			return false;
		}

		return true;
	}

	private StudentResultVO getStudentResult(long studentReferenceNumber, int sessionCode)
			throws IllegalAccessException {
		return findStudentService.findByStudReferenceNumber(studentReferenceNumber, sessionCode);
	}

	private void populateAwardData(Model model, DashboardFormVO dashboardFormVO, long studentReferenceNumber, HttpSession httpSession) {
		logger.info("populateAwardData dashboardFormVO {} ", dashboardFormVO);

		if (dashboardFormVO.getApplicationStatus() != null &&
				dashboardFormVO.getApplicationStatus().equals(AWARDED)) {

			DSAAwardVO awardVO = dsaAwardService.getAwardDataFromSteps(dashboardFormVO, studentReferenceNumber);
			awardVO.setAcademicYear(dashboardFormVO.getAcademicYear());
			awardVO.setAwardStatus(dashboardFormVO.getApplicationStatus().getDescription());
			awardVO.setInstitution(dashboardFormVO.getInstitutionName());
			model.addAttribute(AWARD_DETAILS, awardVO);
			model.addAttribute(CAN_SHOW_AWARD_STATUS_MESSAGE, canShowStatusMessage(awardVO.getAwardDate()));

			logger.info("populateAwardData dashboardFormVO {} ", dashboardFormVO);
			DSAAwardAccess dsaAwardAccess = awardAccessService.getAwardAccess(dashboardFormVO.getDsaApplicationNumber());
			if (dsaAwardAccess != null) {
				model.addAttribute(CAN_ADVISOR_SEE_AWARD, dsaAwardAccess.getAdvisorCanAccess());
				logger.info("populate can advisor AwardData {} and {}", model.getAttribute(CAN_ADVISOR_SEE_AWARD), dsaAwardAccess);
			}
		}
		httpSession.setAttribute("dashboardFormVO", dashboardFormVO);
	}

	private void populatePreviousAwardData(Model model, DashboardFormVO previousYearFormVO, long studentReferenceNumber, HttpSession httpSession) {
		logger.info("populatePreviousAwardData previousFormVO {} ", previousYearFormVO);

		if (previousYearFormVO.getApplicationStatus() != null &&
				previousYearFormVO.getApplicationStatus().equals(AWARDED)) {

			DSAAwardVO awardVO = dsaAwardService.getAwardDataFromSteps(previousYearFormVO, studentReferenceNumber);
			awardVO.setAcademicYear(previousYearFormVO.getAcademicYear());
			awardVO.setAwardStatus(previousYearFormVO.getApplicationStatus().getDescription());
			awardVO.setInstitution(previousYearFormVO.getInstitutionName());
			model.addAttribute(PREVIOUS_AWARD_DETAILS, awardVO);
			model.addAttribute(CAN_SHOW_PREVIOUS_AWARD_STATUS_MESSAGE, canShowStatusMessage(awardVO.getAwardDate()));

			logger.info("populatePreviousAwardData previousYearFormVO {} ", previousYearFormVO);
			DSAAwardAccess dsaAwardAccess = awardAccessService.getAwardAccess(previousYearFormVO.getDsaApplicationNumber());
			if (dsaAwardAccess != null) {
				model.addAttribute(CAN_ADVISOR_SEE_PREVIOUS_AWARD, dsaAwardAccess.getAdvisorCanAccess());
				logger.info("populate Previous Award Data {} and {}", model.getAttribute(CAN_ADVISOR_SEE_PREVIOUS_AWARD), dsaAwardAccess);
			}
		}
		httpSession.setAttribute("previousYearFormVO", previousYearFormVO);
	}

	private boolean canShowStatusMessage(String dateStr) {
		boolean canShow = false;

		if(StringUtils.isNotBlank(dateStr)) {
			try {
				SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy");
				Date statusChangedDate = df.parse(dateStr);
				int daysDiff = NotificationUtil.daysDiff(statusChangedDate, new Date());
				canShow = daysDiff >= 0 && daysDiff <= 7;

			} catch (Exception e) {
				logger.error("Error while parsing the date {} ", dateStr);
			}
		}
		return canShow;
	}

	private OverallApplicationStatus deriveOverallStatus(long studentReferenceNumber, int sessionCode) {
		return applicationStatusService.deriveApplicationStatus(studentReferenceNumber, sessionCode);
	}

	private DashboardFormVO populatePreviousYearApplication(Model model, long studentReferenceNumber, int sessionCode, HttpSession httpSession) throws IllegalAccessException {
		logger.info("In populate Previous Year Application studentReferenceNumber {} and sessionCode {}", studentReferenceNumber,sessionCode);

		ApplicationResponse previousApplication = applicationService
				.findApplicationByStudentReferenceNumberAnSessionCode(studentReferenceNumber, sessionCode);

		logger.info("previousApplication for studentReferenceNumber:{} is {}", studentReferenceNumber,
				previousApplication);

		if (!previousApplication.isNewApplication()) {
			String nextSessionCode = Integer.valueOf(sessionCode + 1).toString();
			String academicYear = sessionCode + " to " + nextSessionCode;
			logger.info("previousApplication for academicYear: {}", academicYear);

			DashboardFormVO previousYearFormVO = new DashboardFormVO();
			previousYearFormVO.setStudentReferenceNumber(previousApplication.getStudentReferenceNumber());
			previousYearFormVO.setDsaApplicationNumber(previousApplication.getDsaApplicationNumber());
			previousYearFormVO.setApplicationStatus(previousApplication.getOverallApplicationStatus());
			logger.info("previousYearFormVO setApplicationStatus : {}",
					previousApplication.getOverallApplicationStatus());

			// Overall application status
			OverallApplicationStatus deriveOverallStatus = deriveOverallStatus(
					previousApplication.getStudentReferenceNumber(), previousApplication.getSessionCode());
			previousYearFormVO.setApplicationStatus(deriveOverallStatus);
			logger.info("Derive overall status from STEPS  {}", deriveOverallStatus);

			if (!Objects.equals(deriveOverallStatus.getCode(), "WITHDRAWN") && isStudentAndHasPendingDelcaration(previousApplication)) {
				previousYearFormVO.setApplicationStatus(NOT_COMPLETE);
				logger.info("previousYearFormVO application status set to NOT_COMPLETE");
			}

			previousYearFormVO.setApplicationUpdated(previousApplication.getApplicationUpdated());
			previousYearFormVO.setInstitutionName(capitalizeFully(previousApplication.getInstitutionName()));
			previousYearFormVO.setRoleName(SecurityContextHelper.getLoggedInUserRole());
			previousYearFormVO.setFundingEligibilityStatus(previousApplication.getFundingEligibilityStatus());

			//set advisor declaration status
			previousYearFormVO.setAdvisorDeclaration(previousApplication.getSectionStatusData().getAdvisorDeclarationSectionData().getSectionStatus().getCode());

			// Search for student
			StudentResultVO studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber, sessionCode);
			previousYearFormVO.setDob(studentResultVO.getDob());
			previousYearFormVO.setInstitutionName(capitalizeFully(studentResultVO.getStudentCourseYear().getInstitutionName()));


			previousYearFormVO.setFirstName(studentResultVO.getFirstName());
			previousYearFormVO.setLastName(studentResultVO.getLastName());
			previousYearFormVO.setAcademicYear(academicYear);
			previousYearFormVO.setSessionCode(sessionCode);
			previousYearFormVO.setNewApplication(previousApplication.isNewApplication());

			model.addAttribute(CAN_SHOW_APPLICATION_STATUS_MESSAGE,
					canShowStatusMessage(previousYearFormVO.getApplicationUpdated())
							&& !Arrays.asList(NOT_STARTED, STARTED, SUBMITTED, AWARDED)
							.contains(previousYearFormVO.getApplicationStatus()));
			populatePreviousAwardData(model, previousYearFormVO, studentReferenceNumber, httpSession);

			return previousYearFormVO;
		}

		return null;
	}

	private Date getLastLoggedInDate(DsaStudentAuthDetails dsaStudentAuthDetails) {
		return DateHelper.addMinutesToDate(dsaStudentAuthDetails.getLastLoggedInDate(), 15);
	}

	private void populateStudentDashboardData(Model model, Long studentRefNumber, int sessionCode, HttpSession httpSession) {
		logger.info("Populate StudentDashboardData for studentRefNumber {} and sessionCode {}", studentRefNumber, sessionCode);
		ApplicationResponse dashboardData;
		try {
			dashboardData = applicationService
					.findApplicationByStudentReferenceNumberAnSessionCode(studentRefNumber, sessionCode);
		} catch (Exception e) {
			logger.error("Unable to find the application", e);
			throw new RuntimeException(e);
		}

		// In case we did not find student's application
		if (dashboardData.getStudentReferenceNumber() > 0) {
			FindStudentHelper.setStudentDetails(findStudentService, dashboardData);

			DashboardFormVO dashboardFormVO = new DashboardFormVO();
			dashboardFormVO.setRoleName("STUDENT");
			dashboardFormVO.setStudentReferenceNumber(studentRefNumber);
			// dashboardFormVO.setApplicationStatus(dashboardData.getOverallApplicationStatus());
			logger.info("DashboardFormVO OverallApplicationStatus from DB : {}",
					dashboardData.getOverallApplicationStatus());

			// Overall application status
			OverallApplicationStatus deriveOverallStatus = deriveOverallStatus(studentRefNumber,
					dashboardData.getSessionCode());
			dashboardFormVO.setApplicationStatus(deriveOverallStatus);
			logger.info("Derive overall status from STEPS  {}", deriveOverallStatus);

			if (isStudentAndHasPendingDelcaration(dashboardData)) {
				dashboardFormVO.setApplicationStatus(NOT_COMPLETE);
				logger.info("DashboardFormVO application status set to NOT_COMPLETE");
			}

			dashboardFormVO.setDsaApplicationNumber(dashboardData.getDsaApplicationNumber());
			dashboardFormVO.setFirstName(dashboardData.getFirstName());
			dashboardFormVO.setLastName(dashboardData.getLastName());
			dashboardFormVO.setAcademicYear(dashboardData.getAcademicYear());
			dashboardFormVO.setSessionCode(dashboardData.getSessionCode());
			dashboardFormVO.setInstitutionName(capitalizeFully(dashboardData.getInstitutionName()));
			dashboardFormVO.setApplicationUpdated(dashboardData.getApplicationUpdated());
			dashboardFormVO.setFundingEligibilityStatus(dashboardData.getFundingEligibilityStatus());
			dashboardFormVO.setNewApplication(dashboardData.isNewApplication());

			//set advisor declaration status
			dashboardFormVO.setAdvisorDeclaration(dashboardData.getSectionStatusData().getAdvisorDeclarationSectionData().getSectionStatus().getCode());

			LoggedinUserUtil.setLoggedinUserInToModel(model);
			model.addAttribute(DASHBOARD_FORM_VO, dashboardFormVO);
			model.addAttribute(CAN_SHOW_APPLICATION_STATUS_MESSAGE,
					canShowStatusMessage(dashboardFormVO.getApplicationUpdated())
							&& !Arrays.asList(NOT_STARTED, STARTED, SUBMITTED, AWARDED)
									.contains(dashboardFormVO.getApplicationStatus()));
			logger.info("Set dashboardFormVO : {}", dashboardFormVO);
			populateAwardData(model, dashboardFormVO, studentRefNumber, httpSession);
		}
	}

	private static boolean isStudentAndHasPendingDelcaration(ApplicationResponse dashboardData) {
		return dashboardData.getSectionStatusData().getAdvisorDeclarationSectionData().getSectionStatus().getCode()
				.equalsIgnoreCase("COMPLETED")
				&& dashboardData.getSectionStatusData().getStudentDeclarationSectionData().getSectionStatus().getCode()
						.equalsIgnoreCase("NOT_STARTED")
				&& SecurityContextHelper.getLoggedInUserRole().equalsIgnoreCase("STUDENT");
	}

	public void setSecurityContext(HttpServletRequest request, LoginFormVO loginFormVO, Model model) {
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				loginFormVO.getEmailAddress(), loginFormVO.getPassword());
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(dsaAuthenticationProvider.authenticate(authRequest));
		HttpSession session = request.getSession(true);
		session.setAttribute("emailAddress", loginFormVO.getEmailAddress());
		session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		LoggedinUserUtil.setLoggedinUserInToModel(model);
	}
}
