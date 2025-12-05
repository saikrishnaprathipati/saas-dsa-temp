package uk.gov.saas.dsa.web.controller.declaration;

import static uk.gov.saas.dsa.domain.refdata.YesNoType.valueOf;
import static uk.gov.saas.dsa.web.controller.declaration.BankDetailsController.CHOOSE_BANK_ACCOUNT;
import static uk.gov.saas.dsa.web.controller.declaration.StudentDeclarationsController.STUDENT_DECLARATION;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.optionHasCorrectValue;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.DASHBOARD_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.domain.DSAAwardAccess;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.service.AwardAccessService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.AwardAccessFormVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

@Controller
public class AwardAccessController {
	private static final String AWARD_ACCESS_FORM_VO = "awardAccessFormVO";
	public static final String AWARD_ACCESS = "awardAccess";
	private static final String SAVE_AWARD_ACCESS = "saveAwardAccess";
	private static final String STUDENT_AWARD_ACCESS_VIEW = "student/awardAccess";

	private final AwardAccessService awardAccessService;

	public AwardAccessController(AwardAccessService awardAccessService) {
		this.awardAccessService = awardAccessService;
	}

	@PostMapping(AWARD_ACCESS)
	public String intAwardAccess(Model model, @RequestParam(value = ACTION) String action,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO) {

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		
		DSAAwardAccess awardAccess = awardAccessService.getAwardAccess(keyDataVO.getDsaApplicationNumber());
		AwardAccessFormVO accessFormVO = new AwardAccessFormVO();
		accessFormVO.setDsaApplicationNumber(keyDataVO.getDsaApplicationNumber());
		accessFormVO.setStudentReferenceNumber(keyDataVO.getStudentReferenceNumber());
		if (awardAccess != null) {
			String advisorCanAccess = awardAccess.getAdvisorCanAccess();
			accessFormVO.setCanAccess(Stream.of(YesNoType.values())
					.filter(t -> t.getDbValue().equalsIgnoreCase(advisorCanAccess)).findFirst().get().name());

		}
		model.addAttribute(AWARD_ACCESS_FORM_VO, accessFormVO);

		return STUDENT_AWARD_ACCESS_VIEW;

	}

	@PostMapping(SAVE_AWARD_ACCESS)
	public String saveAwardAccess(Model model, @RequestParam(value = ACTION) String action, HttpServletRequest request,
			@ModelAttribute(name = AWARD_ACCESS_FORM_VO) AwardAccessFormVO awardAccessFormVO,
			BindingResult bindingResult) throws IllegalAccessException {

		if (securityContext() == null) {
			return LOGIN;
		}
		String view = ERROR_PAGE;
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		boolean hasMandatoryValues = hasMandatoryValues(model, awardAccessFormVO.getDsaApplicationNumber(),
				awardAccessFormVO.getStudentReferenceNumber());
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case CHOOSE_BANK_ACCOUNT:
				String canAccess = awardAccessFormVO.getCanAccess();
				if (optionHasCorrectValue(canAccess)) {
					awardAccessService.saveAwardAccess(awardAccessFormVO.getDsaApplicationNumber(),
							valueOf(canAccess).getDbValue());

					view = AllowancesHelper.showChooseBankAccountPage(request);
				} else {
					bindingResult.rejectValue("canAccess", "awardAccess.selection.required");
					view = STUDENT_AWARD_ACCESS_VIEW;
				}
				break;
			case STUDENT_DECLARATION:
				view = AllowancesHelper.showStudentDeclarationPage(request);
				break;
			case DASHBOARD_ACTION:
				view = AllowancesHelper.showDashboardPage(request);
				break;
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}
		return view;
	}

}
