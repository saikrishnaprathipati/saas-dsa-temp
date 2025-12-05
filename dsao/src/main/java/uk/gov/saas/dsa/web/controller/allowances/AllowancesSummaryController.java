package uk.gov.saas.dsa.web.controller.allowances;

import static uk.gov.saas.dsa.model.Section.ADVISOR_DECLARATION;
import static uk.gov.saas.dsa.model.Section.STUDENT_DECLARATION;
import static uk.gov.saas.dsa.model.SectionStatus.COMPLETED;
import static uk.gov.saas.dsa.service.ServiceUtil.capitalizeFully;
import static uk.gov.saas.dsa.service.ServiceUtil.getApplicationSectionResponse;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setConsumablesSummaryData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setNMPHSummaryData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.setEquipmentSummaryData;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;

import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.domain.DSAApplicationSectionStatus;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.model.SectionStatusResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;

import uk.gov.saas.dsa.service.*;
import uk.gov.saas.dsa.service.allowances.AccommodationService;
import uk.gov.saas.dsa.service.allowances.ConsumablesService;
import uk.gov.saas.dsa.service.allowances.EquipmentPaymentService;
import uk.gov.saas.dsa.service.allowances.EquipmentService;
import uk.gov.saas.dsa.service.allowances.NMPHAllowancesService;
import uk.gov.saas.dsa.service.allowances.TravelExpAllowancesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;

import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;

import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.util.List;
import java.util.Optional;

/**
 * Allowances Summary Controller
 */
@Controller
public class AllowancesSummaryController {
	private static final String ADD_CONSUAMBELS_ACTION = "Add Consuambels";
	private static final String ADVISOR_ALLOWANCES_SUMMARY_PAGE = "advisor/allowancesSummary";
	private static final String ADD_NMPH_ACTION = "Add NMPH";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final ConsumablesService consumablesService;
	private final ConfigDataService configDataService;
	private final EquipmentService equipmentService;
	private final EquipmentPaymentService equipmentPaymentService;
	private final ApplicationService applicationService;
	private final NMPHAllowancesService nmphService;
	private final TravelExpAllowancesService travelExpService;
	private final AccommodationService accommodationService;
	private final QuoteUploadService quoteUploadService;

	private final FindStudentService findStudentService;

	public AllowancesSummaryController(ConsumablesService consumablesService, NMPHAllowancesService nmphService,
			EquipmentService equipmentService, TravelExpAllowancesService travelExpService,
			ConfigDataService configDataService, ApplicationService applicationService,
			QuoteUploadService quoteUploadService, FindStudentService findStudentService,
			EquipmentPaymentService equipmentPaymentService, AccommodationService accommodationService) {

		this.consumablesService = consumablesService;
		this.nmphService = nmphService;
		this.equipmentService = equipmentService;
		this.travelExpService = travelExpService;
		this.configDataService = configDataService;
		this.applicationService = applicationService;

		this.quoteUploadService = quoteUploadService;

		this.findStudentService = findStudentService;
		this.equipmentPaymentService = equipmentPaymentService;
		this.accommodationService = accommodationService;
	}

	@PostMapping("saveAllowancesSummary")
	public String saveAllowancesSummary(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size,
			@RequestParam(value = ACTION) String action, HttpServletRequest request) throws IllegalAccessException {
		logger.info("Save allowances summary for {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		ApplicationResponse applicationResponse = applicationService
				.findApplication(keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());

		if (applicationResponse.isAllAllowancesCompleted()) {
			ServiceUtil.updateSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(), Section.ALLOWANCES,
					COMPLETED);
			ServiceUtil.updateSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(),
					Section.NEEDS_ASSESSMENT_FEE, SectionStatus.NOT_STARTED);

			return AllowancesHelper.showNeedsAssessmentFeeInitialPage(request);
		} else {

			return AllowancesHelper.showAllowancesSummary(request);
		}
	}

	/**
	 * To get the allowances summary
	 *
	 * @param model     the model data
	 * @param keyDataVO key id's data
	 * @param action    the action name
	 * @param request   HttpServlet request
	 * @return view name
	 * @throws IllegalAccessException
	 */
	@PostMapping(ALLOWANCES_SUMMARY_PATH)
	public String getAllowancesSummary(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size,
			@RequestParam(value = ACTION) String action, HttpServletRequest request) throws Exception {
		logger.info("Get allowances summary for {}", keyDataVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		int currentPage = page.orElse(1);
		int pageSize = size.orElse(DSAConstants.PAGINATION_SIZE);
		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		setStudentDetails(model, studentReferenceNumber, dsaApplicationNumber);

		setConsumablesSummaryData(model, consumablesService, configDataService, dsaApplicationNumber);
		setEquipmentSummaryData(model, equipmentService, configDataService, quoteUploadService,
				dsaApplicationNumber, currentPage, pageSize);
		StudentResultVO studentResultVO = setStudentDetailsInTheModel(model, keyDataVO.getStudentReferenceNumber(),
				findStudentService);

		DSAApplicationsMade dsaApplication = equipmentPaymentService
				.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber,
						keyDataVO.getStudentReferenceNumber());
		showChangePaymentForLink(model, keyDataVO, dsaApplication);

		QuoteDetailsFormVO quoteDetailsFormVO = mapQuoteOptionFormVO(model, keyDataVO.getDsaApplicationNumber(),
				studentResultVO, dsaApplication.getSessionCode());
		setQuoteSummaryData(model, quoteUploadService, quoteDetailsFormVO, currentPage, pageSize, configDataService);

		AllowancesHelper.setEquipmentPaymentForData(model, equipmentPaymentService, configDataService,
				dsaApplicationNumber, studentReferenceNumber);
		setNMPHSummaryData(model, nmphService, configDataService, dsaApplicationNumber);
		AllowancesHelper.setTravelExpSummaryData(model, travelExpService, dsaApplicationNumber);
		AllowancesHelper.setAccommodationSummaryData(model, accommodationService, dsaApplicationNumber);
		model.addAttribute(DSA_APPLICATION_NUMBER, dsaApplicationNumber);
		model.addAttribute(STUDENT_REFERENCE_NUMBER, keyDataVO.getStudentReferenceNumber());

		String view = DSAConstants.ERROR_PAGE;
		AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
				dsaApplicationNumber, keyDataVO.getStudentReferenceNumber());
		switch (action) {
		case DASHBOARD_ACTION:
			view = AllowancesHelper.showDashboardPage(request);
			break;
		case ADD_CONSUAMBELS_ACTION:
			view = AllowancesHelper.showConsumablesInitialPage(request);
			break;
		case ADD_NMPH_ACTION:
			view = AllowancesHelper.initAddNMPH(request);
			break;
		default:
			view = ADVISOR_ALLOWANCES_SUMMARY_PAGE;
			break;
		}
		return view;
	}

	private void showChangePaymentForLink(Model model, ApplicationKeyDataFormVO keyDataVO,
			DSAApplicationsMade dsaApplication) {

		List<DSAApplicationSectionStatus> statusList = dsaApplication.getDsaApplicationSectionStatus();
		SectionStatusResponse advisorDeclaration = getApplicationSectionResponse(ADVISOR_DECLARATION, statusList);
		model.addAttribute("SHOW_CHANGE_PAYMENT_FOR", !advisorDeclaration.getSectionStatus().equals(COMPLETED));
		SectionStatusResponse studentDeclaration = getApplicationSectionResponse(STUDENT_DECLARATION, statusList);
		model.addAttribute("SHOW_REPORT_PROB_PAYMENT_FOR", !studentDeclaration.getSectionStatus().equals(COMPLETED));

	}

	private void setStudentDetails(Model model, long studentReferenceNumber, long dsaApplicationNumber)
			throws IllegalAccessException {
		DSAApplicationsMade dsaApplication = applicationService
				.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber, studentReferenceNumber);

		StudentResultVO studVo = findStudentService.findByStudReferenceNumber(studentReferenceNumber,
				dsaApplication.getSessionCode());
		model.addAttribute(STUDENT_FULL_NAME,
				ServiceUtil.capitalizeFully(studVo.getFirstName() + " " + studVo.getLastName()));
		StudentCourseYearVO studentCourseYear = studVo.getStudentCourseYear();
		if (studentCourseYear != null) {
			model.addAttribute(DSAConstants.HEI_NAME, capitalizeFully(studentCourseYear.getInstitutionName()));
		}
	}
}
