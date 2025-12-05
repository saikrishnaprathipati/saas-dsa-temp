package uk.gov.saas.dsa.web.controller;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.saas.dsa.model.Section.ADDITIONAL_INFO;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.redirectToView;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_DASHBOARD_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SKIP_ACTION;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

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

import uk.gov.saas.dsa.domain.DSAAppAdditionalInformation;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.AdditionalInfoService;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.AdditionalInfoFormVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Controller
public class AdditionalInfoController {
	private static final String DASHBOARD_ADD_ACTION = "DASHBOARD_ADD_ACTION";
	private static final String SHOW_SKIP_ADDITIONAL_INFO = "SHOW_SKIP_ADDITIONAL";
	public static final String ADD_ADDITIONAL_INFO_URI = "addAdditionalInfo";
	private static final String ADDITIONAL_INFO_FORM_VO = "additionalInfoFormVO";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private AdditionalInfoService additionalInfoService;
	private ApplicationService applicationService;
	private static final String ADDITIONAL_INFO_DETAILS_PAGE = "advisor/addAdditionalInfo";

	public AdditionalInfoController(AdditionalInfoService service, ApplicationService applicationService) {
		this.additionalInfoService = service;
		this.applicationService = applicationService;
	}

	@PostMapping(path = { "initAdditionalInfo", ADD_ADDITIONAL_INFO_URI })
	public String addAdditionalInfo(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = ADDITIONAL_INFO_FORM_VO) AdditionalInfoFormVO additionalInfoFormVO,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
		if (securityContext() == null) {
			return DSAConstants.LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		logger.info("in add additional info call action: {}, request: {}", action, additionalInfoFormVO);
		long dsaApplicationNumber = additionalInfoFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = additionalInfoFormVO.getStudentReferenceNumber();
		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		if (hasMandatoryValues) {
			AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
					additionalInfoFormVO.getDsaApplicationNumber(), additionalInfoFormVO.getStudentReferenceNumber());
			Boolean allowancesNotComplete = (Boolean) model.getAttribute("allowancesNotCompleted");
			Boolean declarationsNotCompleted = (Boolean) model.getAttribute("ADVISOR_DECLARTION_NOT_COMPLETED");

			model.addAttribute(SHOW_SKIP_ADDITIONAL_INFO, true);
			switch (action) {
			case "Back":
				view = redirectToView(request, APPLICATION_DASHBOARD_PATH);
				break;

			case SKIP_ACTION:
				ServiceUtil.setSectionStatus(applicationService, dsaApplicationNumber, ADDITIONAL_INFO,
						SectionStatus.COMPLETED);
				if (allowancesNotComplete) {
					view = redirectToView(request, APPLICATION_DASHBOARD_PATH);
				} else {
					view = AllowancesHelper.showDeclrationsInitialPage(request);
				}
				break;
			case "COMPLETE_ADDITIONAL_INFO":
				if (!bindingResult.hasErrors()) {

					String infoText = additionalInfoFormVO.getInfoText();
					additionalInfoService.addAdditionalInfo(dsaApplicationNumber, infoText);
					
					if (allowancesNotComplete) {
						view = redirectToView(request, APPLICATION_DASHBOARD_PATH);
					} else {
						view = AllowancesHelper.showDeclrationsInitialPage(request);
					}

				} else {
					model.addAttribute(ADDITIONAL_INFO_FORM_VO, additionalInfoFormVO);
					view = ADDITIONAL_INFO_DETAILS_PAGE;
				}
				break;
			case DASHBOARD_ADD_ACTION:
				setAdditionalInfoText(model, additionalInfoFormVO, dsaApplicationNumber);
				model.addAttribute(ADDITIONAL_INFO_FORM_VO, additionalInfoFormVO);
				view = ADDITIONAL_INFO_DETAILS_PAGE;
				break;
			default:
				setAdditionalInfoText(model, additionalInfoFormVO, dsaApplicationNumber);
				model.addAttribute(ADDITIONAL_INFO_FORM_VO, additionalInfoFormVO);
				view = ADDITIONAL_INFO_DETAILS_PAGE;
				break;

			}

		}

		return view;
	}

	private void setAdditionalInfoText(Model model, AdditionalInfoFormVO additionalInfoFormVO,
			long dsaApplicationNumber) {

		DSAAppAdditionalInformation additionalInfoData = additionalInfoService.getAdditionalInfo(dsaApplicationNumber);
		if (additionalInfoData != null) {
			additionalInfoFormVO.setInfoText(additionalInfoData.getInfoText());
			model.addAttribute(SHOW_SKIP_ADDITIONAL_INFO, isEmpty(additionalInfoData.getInfoText()));
		}
	}
}
