package uk.gov.saas.dsa.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.gov.saas.dsa.domain.validation.EmailAddressValidator;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.service.RegistrationService;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;

@Controller
public class RegistrationController {

	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final String LOGIN_PRIVACY_POLICY = "register/privacyPolicy";
	private static final String LOGIN_START = "register/start";
	private static final String API_RESEND_EMAIL = "/resendEmail";
	private static final String API_LOOKUP_ACCOUNT = "/lookupAccount";
	private static final String API_START = "/start";
	private static final String API_PRIVACY_POLICY = "/privacyPolicy";
	private static final String API_CREATE_ACCOUNT = "/createAccount";
	private static final String CONFIRM_EMAIL = "register/confirmEmail";
	private static final String CREATE_ACCOUNT = "register/createAccount";
	private static final String EXCEEDED_NOTIFICATION = "register/exceededNotification";
	private static final String EXPIRED_NOTIFICATION = "register/expiredNotification";
	private static final String ACCOUNT_ALREADY_ACTIVATED = "register/accountAlreadyActivated";
	private static final String CREATE_ACCOUNT_FORM_VO = "createAccountFormVO";

	private RegistrationService registrationService;
	private AdvisorLookupService advisorLookupService;

	@Autowired
	public RegistrationController(AdvisorLookupService advisorLookupService, RegistrationService registrationService) {
		this.advisorLookupService = advisorLookupService;
		this.registrationService = registrationService;
	}

	@GetMapping(path = { API_START })
	public String startPage(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO Start page");
		return LOGIN_START;
	}

	@PostMapping(path = { API_PRIVACY_POLICY })
	public String privacyPolicy(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO Privacy Policy  page");
		return LOGIN_PRIVACY_POLICY;
	}

	@PostMapping(path = { API_CREATE_ACCOUNT })
	public String createAccount(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO create account page");
		model.addAttribute(CREATE_ACCOUNT_FORM_VO, new CreateAccountFormVO());
		return CREATE_ACCOUNT;
	}

	@PostMapping(path = { API_LOOKUP_ACCOUNT })
	public String lookupAccount(Model model, @Valid @ModelAttribute CreateAccountFormVO createAccountFormVO,
			BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes,
			HttpSession httpsession) {

		logger.info("DSAO Lookup account request: {}", createAccountFormVO);

		EmailAddressValidator emailAddressValidator = new EmailAddressValidator(advisorLookupService);
		emailAddressValidator.validate(createAccountFormVO, bindingResult);

		model.addAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
		httpsession.setAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);

		if (bindingResult.hasErrors()) {
			return CREATE_ACCOUNT;
		} else {
			registrationService.requestActivation(createAccountFormVO.getEmailAddress());
			return CONFIRM_EMAIL;
		}
	}

	@GetMapping(path = { API_RESEND_EMAIL })
	public String resendActivationLink(HttpServletRequest request, HttpServletResponse httpResponse,
			HttpSession httpsession, Model model) {
		logger.info("DSAO Resend activation link {}", httpsession.getAttribute(CREATE_ACCOUNT_FORM_VO));

		CreateAccountFormVO createAccountFormVO = (CreateAccountFormVO) httpsession
				.getAttribute(CREATE_ACCOUNT_FORM_VO);
		createAccountFormVO.setResendEmailLink(true);

		Response response = registrationService.requestActivation(createAccountFormVO.getEmailAddress());

		switch (response.getResponseCode()) {
		case ACTIVATION_LIMIT_EXCEEDED:
			logger.info("DSAO activation link request exceeded for {}", createAccountFormVO);
			model.addAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
			return EXCEEDED_NOTIFICATION;
		case ACCOUNT_ALREADY_ACTIVATED:
			logger.info("DSAO account already activated for {}", createAccountFormVO);
			model.addAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
			return ACCOUNT_ALREADY_ACTIVATED;
		case ACTIVATION_LINK_EXPIRED:
			logger.info("DSAO account expired for {}", createAccountFormVO);
			model.addAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
			return EXPIRED_NOTIFICATION;
		default:
			logger.info("DSAO Resend activation link request for {}", createAccountFormVO);
			model.addAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
			return CONFIRM_EMAIL;
		}
	}
}
