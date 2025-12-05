package uk.gov.saas.dsa.web.controller;

import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DisabilitiesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Controller
public class ApplicationSummaryController {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private ApplicationService applicationService;
	private DisabilitiesService disabilitiesService;

	/**
	 * Constructor
	 *
	 * @param applicationService
	 * @param disabilitiesService
	 */
	public ApplicationSummaryController(ApplicationService applicationService,
										DisabilitiesService disabilitiesService) {
		this.applicationService = applicationService;
		this.disabilitiesService = disabilitiesService;
	}

	// TODO this will be the last action to render the summary
	@PostMapping("summary")
	public String stepTwoSummary(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		try {
			ApplicationResponse dashboardData = applicationService.findApplication(keyDataVO.getDsaApplicationNumber(),
					keyDataVO.getStudentReferenceNumber());
			List<DisabilityTypeVO> populateApplicationDisabilities = disabilitiesService
					.populateApplicationDisabilities(keyDataVO.getDsaApplicationNumber());

			dashboardData.setApplicationDisabilities(populateApplicationDisabilities);
			logger.info(dashboardData);
			model.addAttribute("summaryData", dashboardData);
			model.addAttribute(DSA_APPLICATION_NUMBER, keyDataVO.getDsaApplicationNumber());
			model.addAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "advisor/disabilityAndAllowanceSummary";
	}

	@PostMapping("applicationSummary")
	public String initApplicationSummary(Model model, HttpServletRequest request,
										 @RequestParam(value = ACTION, required = true) String action,
										 @Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {
		logger.info("init declarations call {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		if (Arrays.asList(DSAConstants.APPLICATION_DASHBOARD_ACTION, "DISABILITYDETAILS",
				DSAConstants.SAVE_AND_CONTINUE_ACTION).contains(action.toUpperCase())) {
			if (action.toUpperCase().equals(DSAConstants.APPLICATION_DASHBOARD_ACTION)
					|| action.toUpperCase().equals(DSAConstants.SAVE_AND_CONTINUE_ACTION)) {
				request.setAttribute(DSA_APPLICATION_NUMBER, keyDataVO.getDsaApplicationNumber());
				request.setAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());
				request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
				view = REDIRECT + APPLICATION_DASHBOARD_PATH;
			}
			if (action.toUpperCase().equals("DISABILITYDETAILS")) {
				request.setAttribute(DSA_APPLICATION_NUMBER, keyDataVO.getDsaApplicationNumber());
				request.setAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());
				request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
				view = REDIRECT + "disabilityDetails";
			}

		}
		return view;
	}

}
