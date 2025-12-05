package uk.gov.saas.dsa.web.controller;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CONTACT_US_URL;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.service.award.DSAAwardService;
import uk.gov.saas.dsa.service.notification.NotificationUtil;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.DashboardFormVO;
import uk.gov.saas.dsa.vo.award.DSAAwardDetailsVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

@Controller
public class AwardDetailsController {
	private static final String PAYMENT_FOR_TEXT = "If your course has started, we will pay this into your bank account within the next 3 to 5 working days. Otherwise, we will make the payment within 2 weeks of your course start date.";
	private static final String AWARD_DETAILS_MODEL = "awardDetails";
	private static final String AWARD_DETAILS = "/awardDetails";
	private static final String PREVIOUS_AWARD_DETAILS = "/previousAwardDetails";
	private static final String DOWNLOAD_AWARD = "/downloadAward";
	private static final String DOWNLOAD_PREVIOUS_AWARD = "/downloadPreviousAward";
	private static final String DOWNLOAD_QUOTE = "/downloadQuote/{dsaQuoteId}";
	private static final String AWARD_DETAILS_HTML = "award/awardDetails";
	private static final String BACK_LINK = "backLink";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DSAAwardService dsaAwardService;
	private DSAEmailConfigProperties dsaEmailConfigProperties;
	private final CourseDetailsService courseDetailsService;

	public AwardDetailsController(DSAAwardService dsaAwardService, CourseDetailsService courseDetailsService,
			DSAEmailConfigProperties dsaEmailConfigProperties) {
		this.dsaAwardService = dsaAwardService;
		this.courseDetailsService = courseDetailsService;
		this.dsaEmailConfigProperties = dsaEmailConfigProperties;
	}

	/**
	 * @param model Model for the template
	 * @return advisor/studentDetails
	 * @throws Exception For null student
	 */
	@GetMapping(AWARD_DETAILS)
	public String awardDetails(Model model, HttpSession httpSession, HttpServletRequest request) throws Exception {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		Map<String, Object> modelMap = new HashMap<String, Object>();
		NotificationUtil.buildCommonUrls(modelMap, dsaEmailConfigProperties);
		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpSession);
		DashboardFormVO dashboardFormVO = (DashboardFormVO) httpSession.getAttribute("dashboardFormVO");

		DSAAwardDetailsVO awardDetails = dsaAwardService.getAwardDetails(studentReferenceNumber, dashboardFormVO);
		model.addAttribute(CONTACT_US_URL, modelMap.get(CONTACT_US_URL));
		model.addAttribute(AWARD_DETAILS_MODEL, awardDetails);
		logger.info("Previous Award Details awardDetails {}", awardDetails);

		String total = currencyLocalisation(
				null != awardDetails.accomTotal() ? awardDetails.accomTotal().doubleValue() : 0);
		model.addAttribute("ACCOMM_TOTAL", total);

		if (awardDetails.paymentToHEI() != null) {
			String text = "";
			if (awardDetails.paymentToHEI().equalsIgnoreCase(YesNoType.YES.getDbValue())) {
				text = "We will pay this allowance to your " + awardDetails.institution()
						+ " on receipt of an invoice.";
			} else {
				text = PAYMENT_FOR_TEXT;
			}
			model.addAttribute("PAYMENT_FOR_TEXT", text);
		}

		// Back link for awards
		if (LoggedinUserUtil.isAdvisor()) {
			model.addAttribute(BACK_LINK, "/advisorDashboard");
		} else {
			model.addAttribute(BACK_LINK, "/studentDsaDashboard");
		}

		return AWARD_DETAILS_HTML;
	}

	/**
	 * @param model Model for the template
	 * @return advisor/studentDetails
	 * @throws Exception For null student
	 */
	@GetMapping(PREVIOUS_AWARD_DETAILS)
	public String previousAwardDetails(Model model, HttpSession httpSession, HttpServletRequest request)
			throws Exception {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		Map<String, Object> modelMap = new HashMap<String, Object>();
		NotificationUtil.buildCommonUrls(modelMap, dsaEmailConfigProperties);
		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpSession);
		DashboardFormVO previousYearFormVO = (DashboardFormVO) httpSession.getAttribute("previousYearFormVO");

		DSAAwardDetailsVO awardDetails = dsaAwardService.getAwardDetails(studentReferenceNumber, previousYearFormVO);
		model.addAttribute(CONTACT_US_URL, modelMap.get(CONTACT_US_URL));
		model.addAttribute(AWARD_DETAILS_MODEL, awardDetails);
		logger.info("Previous Award Details awardDetails {}", awardDetails);

		String total = currencyLocalisation(
				null != awardDetails.accomTotal() ? awardDetails.accomTotal().doubleValue() : 0);
		model.addAttribute("ACCOMM_TOTAL", total);

		if (awardDetails.paymentToHEI() != null) {
			String text = "";
			if (awardDetails.paymentToHEI().equalsIgnoreCase(YesNoType.YES.getDbValue())) {
				CourseDetailsVO courseDetailsVO = courseDetailsService.findCourseDetailsFromDB(studentReferenceNumber);
				text = "We will pay this allowance to your " + courseDetailsVO.getInstitutionName()
						+ " on receipt of an invoice.";
			} else {
				text = PAYMENT_FOR_TEXT;
			}
			model.addAttribute("PAYMENT_FOR_TEXT", text);
		}

		// Back link for awards
		if (LoggedinUserUtil.isAdvisor()) {
			model.addAttribute(BACK_LINK, "/advisorDashboard");
		} else {
			model.addAttribute(BACK_LINK, "/studentDsaDashboard");
		}

		return AWARD_DETAILS_HTML;
	}

	@GetMapping(DOWNLOAD_AWARD + "/{currentSession}")
	public String downloadAwardDetailsForYear(Model model, @PathVariable int currentSession, HttpSession httpSession,
			HttpServletResponse response) throws Exception {

		String view = null;
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		Map<String, Object> modelMap = new HashMap<String, Object>();
		NotificationUtil.buildCommonUrls(modelMap, dsaEmailConfigProperties);
		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpSession);
		int currentActiveSession = ConfigDataService.getCurrentActiveSession();
		if (currentSession == currentActiveSession - 1 || currentSession == currentActiveSession) {
			Blob blob = dsaAwardService.getAwardPDF(studentReferenceNumber, currentSession);
			response.setContentType("application/pdf");
			response.setContentLength((int) blob.length());
			response.setHeader("Content-Disposition",
					"inline; document.fileName=\"" + "DSAAward_" + studentReferenceNumber + ".pdf" + "\"");
			FileCopyUtils.copy(blob.getBinaryStream(), response.getOutputStream());
		} else {
			logger.error("Invalid access for the award session {}", currentSession);
			view = ERROR_PAGE;
		}
		return view;
	}

	@GetMapping(DOWNLOAD_PREVIOUS_AWARD)
	public String downloadPreviousAwardDetails(Model model, HttpSession httpSession, HttpServletResponse response)
			throws Exception {
		logger.info("initAboutDetails call");
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		Map<String, Object> modelMap = new HashMap<String, Object>();
		NotificationUtil.buildCommonUrls(modelMap, dsaEmailConfigProperties);
		long studentReferenceNumber = AllowancesHelper.getStudentRefFromSession(httpSession);

		Blob blob = dsaAwardService.getAwardPDF(studentReferenceNumber,
				ConfigDataService.getCurrentActiveSession() - 1);
		response.setContentType("application/pdf");
		response.setContentLength((int) blob.length());
		response.setHeader("Content-Disposition",
				"inline; document.fileName=\"" + "DSAAward_" + studentReferenceNumber + ".pdf" + "\"");

		FileCopyUtils.copy(blob.getBinaryStream(), response.getOutputStream());
		return null;
	}

	@GetMapping(DOWNLOAD_QUOTE)
	public String downloadQuote(Model model, @PathVariable int dsaQuoteId, HttpSession httpSession,
			HttpServletResponse response) throws Exception {
		logger.info("initAboutDetails call");
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		Blob blob = dsaAwardService.getQuote(dsaQuoteId);
		response.setContentType("application/pdf");
		response.setContentLength((int) blob.length());
		response.setHeader("Content-Disposition",
				"inline; document.fileName=\"" + "DSAEquipmentQuote_" + dsaQuoteId + ".pdf" + "\"");

		FileCopyUtils.copy(blob.getBinaryStream(), response.getOutputStream());
		return null;
	}
}
