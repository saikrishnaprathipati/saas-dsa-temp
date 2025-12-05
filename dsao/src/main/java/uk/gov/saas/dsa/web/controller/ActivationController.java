package uk.gov.saas.dsa.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import uk.gov.saas.dsa.domain.validation.PasswordValidator;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.service.AdvisorLoginService;
import uk.gov.saas.dsa.service.RegistrationService;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

@Controller
public class ActivationController {

	private Logger logger = LogManager.getLogger(ActivationController.class);

	private static final String ACTIVATE_ACCOUNT = "/activateAccount";
	private static final String CHECK_ADVISOR_DETAILS = "checkAdvisorDetails";
	private static final String LOGIN_CHECK_ADVISOR_DETAILS = "register/checkAdvisorDetails";
	private static final String ACCOUNT = "register/account";
	private static final String HELP = "help";
	private static final String LOGGED_IN_HELP_ADVISOR = "helpLoggedInAdvisor";
	private static final String LOGGED_IN_HELP_STUDENT = "helpLoggedInStudent";
	private static final String CREATE_LOGIN_PASSWORD = "register/createPassword";
	private static final String CREATE_PASSWORD = "/createPassword";
	private static final String LOGIN_ACCOUNT_CREATED = "register/accountCreated";
	private static final String ACCOUNT_CREATED = "/accountCreated";
	private static final String EXCEEDED_NOTIFICATION = "register/exceededNotification";
	private static final String EXPIRED_NOTIFICATION = "register/expiredNotification";
	private static final String ACCOUNT_ALREADY_ACTIVATED = "register/accountAlreadyActivated";
	private static final String PAGE_NOT_AVAILABLE = "register/pageNotAvailable";
	public static final String REDIRECT = "redirect:/";
	private static final String CREATE_PASSWORD_FORM_VO = "createPasswordFormVO";
	private static final String CREATE_ACCOUNT_FORM_VO = "createAccountFormVO";
	private static final String ADVISOR = "advisor";

	private AdvisorLoginService advisorLoginService;
	private RegistrationService registrationService;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	public ActivationController(AdvisorLoginService advisorLoginService, RegistrationService registrationService) {
		this.advisorLoginService = advisorLoginService;
		this.registrationService = registrationService;
	}

	@GetMapping(ACTIVATE_ACCOUNT)
	public String activateAccount(@RequestParam String token, @RequestParam String userId, Model model,
								  HttpServletRequest request, RedirectAttributes redirectAttributes) throws IOException {
		logger.info("ActivateAccount for user with token: {} and userId {} requested", token, userId);
		redirectAttributes.addFlashAttribute("token", token);
		redirectAttributes.addFlashAttribute("userId", userId);
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + CHECK_ADVISOR_DETAILS;
	}

	@GetMapping(CHECK_ADVISOR_DETAILS)
	public String checkAdvisorDetails(Model model, HttpServletRequest request, String token, String userId,
									  RedirectAttributes redirectAttributes, HttpSession httpsession) throws IOException {
		Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
		if (inputFlashMap != null) {
			token = (String) inputFlashMap.get("token");
			userId = (String) inputFlashMap.get("userId");
		}

		logger.info("Check Advisor Details for user with token: {} and userId {} requested", token, userId);

		if (null == token || null == userId) {
			return PAGE_NOT_AVAILABLE;
		}

		Response result = registrationService.checkAdvisorDetails(token, userId);
		AdvisorResultVO advisor = (AdvisorResultVO) result.getModel();
		model.addAttribute(ADVISOR, advisor);

		logger.info("ActivateAccount for user with {}", model);

		switch (result.getResponseCode()) {
			case ACTIVATION_LIMIT_EXCEEDED:
				return EXCEEDED_NOTIFICATION;
			case ACCOUNT_ALREADY_ACTIVATED:
				return ACCOUNT_ALREADY_ACTIVATED;
			case ACTIVATION_LINK_EXPIRED:
				CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
				createAccountFormVO.setEmailAddress(advisor.getEmail());
				httpsession.setAttribute(CREATE_ACCOUNT_FORM_VO, createAccountFormVO);
				return EXPIRED_NOTIFICATION;
			case ACTIVATION_TOKEN_INVALID:
				return PAGE_NOT_AVAILABLE;
			default:
				return LOGIN_CHECK_ADVISOR_DETAILS;
		}
	}

	@PostMapping(CREATE_PASSWORD)
	public String confirmAdvisorDetails(Model model, @Valid @ModelAttribute AdvisorResultVO advisor,
										BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes,
										HttpSession httpsession) throws IOException {
		logger.info("Confirm Advisor details {}", advisor);

		CreatePasswordFormVO createPasswordFormVO = new CreatePasswordFormVO();
		createPasswordFormVO.setEmail(advisor.getEmail());
		createPasswordFormVO.setUserId(advisor.getUserId());
		model.addAttribute(CREATE_PASSWORD_FORM_VO, createPasswordFormVO);

		return CREATE_LOGIN_PASSWORD;
	}

	@PostMapping(ACCOUNT_CREATED)
	public String createPassword(Model model, @Valid @ModelAttribute CreatePasswordFormVO createPasswordFormVO,
								 BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes,
								 HttpSession httpsession) throws IOException {

		PasswordValidator passwordValidator = new PasswordValidator(advisorLoginService);
		passwordValidator.validate(createPasswordFormVO, bindingResult);

		model.addAttribute(CREATE_PASSWORD_FORM_VO, createPasswordFormVO);
		httpsession.setAttribute(CREATE_PASSWORD_FORM_VO, createPasswordFormVO);

		if (bindingResult.hasErrors()) {
			return CREATE_LOGIN_PASSWORD;
		} else {
			registrationService.completeRegistration(createPasswordFormVO);
			return LOGIN_ACCOUNT_CREATED;
		}
	}

	@GetMapping("/account")
	public String getAdvisorDetails(Model model, HttpSession httpsession) {
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN;
		} else {
			String emailAddress = (String) securityContext.getAuthentication().getPrincipal();
			logger.info("Fetch Advisor Details for user with emailAddress {} requested", emailAddress);
			if (null == emailAddress) {
				return PAGE_NOT_AVAILABLE;
			}

			Response result = registrationService.getAdvisorDetails(emailAddress);
			AdvisorResultVO advisor = (AdvisorResultVO) result.getModel();
			model.addAttribute(ADVISOR, advisor);
			LoggedinUserUtil.setLoggedinUserInToModel(model);
			return ACCOUNT;
		}
	}

	@GetMapping("/help")
	public String getHelpPage(Model model, HttpSession httpsession) {
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return HELP;
		} else {
			LoggedinUserUtil.setLoggedinUserInToModel(model);
			if (LoggedinUserUtil.isAdvisor())
				return LOGGED_IN_HELP_ADVISOR;
			else
				return LOGGED_IN_HELP_STUDENT;
		}
	}

	@GetMapping(value = "/static/file/download/{name}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> downloadFile(@PathVariable(value = "name") String fileName) {
		Resource dir = resourceLoader.getResource("classpath:file/download/" + fileName);

		try {
			if (dir.exists()) {
				Resource file = new UrlResource(dir.getFile().toURI());
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(file);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
