package uk.gov.saas.dsa.web.controller;

import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static uk.gov.saas.dsa.web.controller.FindStudentController.FIND_STUDENT_OPTION_FORM_VO;
import static uk.gov.saas.dsa.web.controller.FindStudentController.FIND_STUDENT_OPTION;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.validation.EmailValidator;
import uk.gov.saas.dsa.domain.validation.LoginFormValidator;
import uk.gov.saas.dsa.domain.validation.PasswordValidator;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.service.AdvisorLoginService;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.service.LoginService;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;
import uk.gov.saas.dsa.vo.FindStudentOptionsFormVO;
import uk.gov.saas.dsa.vo.ForgotPasswordFormVO;
import uk.gov.saas.dsa.vo.LoginFormVO;
import uk.gov.saas.dsa.vo.SaveDeviceFormVO;
import uk.gov.saas.dsa.web.config.DsaAuthenticationProvider;

@Controller
public class LoginController {

	private final Logger logger = LogManager.getLogger(this.getClass());

	public static final String LOGIN_SET_PASSWORD = "/setPassword";
	public static final String SET_PASSWORD = "setPassword";
	private static final String LOGIN_EXCEEDED_NOTIFICATION = "login/exceededNotification";
	private static final String SAVE_MY_DEVICE = "/saveMyDevice";
	private static final String LOGIN_CHECK_EMAIL_FORGOT_PASSWORD = "login/checkEmailForgotPassword";
	private static final String LOGIN_PASSWORD_RESET = "login/passwordReset";
	private static final String LOGIN_CREATE_NEW_PASSWORD = "login/createNewPassword";
	private static final String SAVE_NEW_PASSWORD = "saveNewPassword";
	private static final String RESET_PASSWORD = "/resetPassword";
	private static final String LOGIN_FORGOT_PASSWORD = "login/forgotPassword";
	private static final String FORGOT_PASSWORD = "/forgotPassword";
	private static final String LOGIN_SAVE_DEVICE = "login/saveDevice";
	private static final String LOGIN_ACCOUNT_INACTIVE = "login/accountInactive";
	private static final String LOGIN_PAGE_NOT_AVAILABLE = "login/pageNotAvailable";
	private static final String LOGIN_LINK_EXPIRED = "login/linkExpired";
	private static final String LOGIN_ACCOUNT_ALREADY_VERIFIED = "login/accountAlreadyVerified";
	private static final String LOGIN_DEVICE_ALREADY_VERIFIED = "login/deviceAlreadyVerified";
	private static final String LOGIN_FORM_VO = "loginFormVO";
	private static final String LOGIN = "/login";
	private static final String STUDENT_LOGIN = "/studentLogin";
	private static final String LOGOUT = "/logout";
	private static final String LOGIN_PAGE = "login/login";
	private static final String CONFIRM_EMAIL = "login/confirmEmail";
	private static final String REDIRECT = "redirect:/";
	private static final String RESEND_LOGIN_LINK = "/resendLoginLink";
	private static final String REMEMBER_DEVICE_AND_CONTINUE = "/rememberDeviceAndContinue";
	private static final String ADVISOR = "advisor";
	private static final String SAVE_DEVICE = "/saveDevice";
	private static final String LOAD_SAVE_DEVICE = "/loadSaveDevice";
	private static final String LOGIN_LOAD_SAVE_DEVICE = "/loginAndLoadSaveDevice";

	@Value("${ols.logoutConfirm.url}")
	private String olsLogoutUrl;

	@Value("${ols.login.url}")
	private String olsLoginUrl;

	private AdvisorLoginService advisorLoginService;
	private AdvisorLookupService advisorLookupService;
	private LoginService loginService;

	private DsaAuthenticationProvider dsaAuthenticationProvider;

	private DSAEmailConfigProperties emailConfigProperties;

	@Autowired
	public LoginController(AdvisorLoginService advisorLoginService, AdvisorLookupService advisorLookupService,
			LoginService loginService, DsaAuthenticationProvider dsaAuthenticationProvider,
			DSAEmailConfigProperties emailConfigProperties) {
		this.advisorLoginService = advisorLoginService;
		this.advisorLookupService = advisorLookupService;
		this.loginService = loginService;
		this.dsaAuthenticationProvider = dsaAuthenticationProvider;
		this.emailConfigProperties = emailConfigProperties;
	}

	@GetMapping(path = { LOGIN })
	public String displaySignIn(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("Redirecting to DSAO Sign in page");
		LoginFormVO loginFormVO = new LoginFormVO();
		model.addAttribute(LOGIN_FORM_VO, loginFormVO);
		SecurityContextHolder.clearContext();
		clearAuthCookie(response);
		return LOGIN_PAGE;
	}

	@GetMapping(path = { STUDENT_LOGIN })
	public String studentSignIn(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("Redirecting to DSAO Student Sign in page");
		LoginFormVO loginFormVO = new LoginFormVO();
		model.addAttribute(LOGIN_FORM_VO, loginFormVO);
		SecurityContextHolder.clearContext();
		clearAuthCookie(response);
		return "redirect:" + olsLoginUrl;
	}

	@PostMapping(path = { LOGIN })
	public String loginAdvisor(HttpServletRequest request, HttpServletResponse httpServletResponse, Model model,
			LoginFormVO loginFormVO, BindingResult bindingResult, HttpSession httpsession) {

		LoginFormValidator loginFormValidator = new LoginFormValidator(advisorLoginService, loginService,
				emailConfigProperties);
		loginFormValidator.validate(loginFormVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return LOGIN_PAGE;
		} else {
			Response response = loginService.verifyDevice(request, loginFormVO.getEmailAddress());
			logger.info("Successfully validated {}", response);

			if (response.getResponseCode().equals(ResponseCode.DEVICE_VERIFIED)) {
				setSecurityContext(request, loginFormVO, model);
				addAuthCookie(httpServletResponse);
				return findStudent(model);

			} else {
				model.addAttribute(LOGIN_FORM_VO, loginFormVO);
				httpsession.setAttribute(LOGIN_FORM_VO, loginFormVO);
				return CONFIRM_EMAIL;
			}
		}
	}

	@GetMapping(SAVE_DEVICE)
	public String redirectSaveDevice(@RequestParam String token, @RequestParam String userId,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		logger.info("Redirect to save my device for user with token: {} and userId {} requested", token, userId);
		redirectAttributes.addFlashAttribute("token", token);
		redirectAttributes.addFlashAttribute("userId", userId);
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + "saveMyDevice";
	}

	@GetMapping(SAVE_MY_DEVICE)
	public String saveDeviceDetails(Model model, HttpServletRequest request, HttpServletResponse httpServletResponse,
			HttpSession httpsession, String token, String userId) {

		Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
		if (inputFlashMap != null) {
			token = (String) inputFlashMap.get("token");
			userId = (String) inputFlashMap.get("userId");
		}

		if (null == token || null == userId) {
			return LOGIN_PAGE_NOT_AVAILABLE;
		}

		logger.info("Check Advisor Details for user with token: {} and userId {} requested", token, userId);

		Response result = loginService.saveDeviceDetails(token, userId);

		AdvisorResultVO advisor = (AdvisorResultVO) result.getModel();
		SaveDeviceFormVO saveDeviceFormVO = new SaveDeviceFormVO();
		model.addAttribute(ADVISOR, advisor);
		model.addAttribute("saveDeviceFormVO", saveDeviceFormVO);

		switch (result.getResponseCode()) {
		case DEVICE_TOKEN_LIMIT_EXCEEDED:
			return LOGIN_EXCEEDED_NOTIFICATION;
		case DEVICE_VERIFIED:

			logger.info("Save device details for user with {}", model);
			LoginFormVO loginFormVO = new LoginFormVO();
			loginFormVO.setEmailAddress(advisor.getEmail());
			httpsession.setAttribute(LOGIN_FORM_VO, loginFormVO);
			setSecurityContext(request, loginFormVO, model);
			addAuthCookie(httpServletResponse);
			return findStudent(model);
		case DEVICE_TOKEN_EXPIRED:
			loginFormVO = new LoginFormVO();
			loginFormVO.setEmailAddress(advisor.getEmail());
			httpsession.setAttribute(LOGIN_FORM_VO, loginFormVO);
			return LOGIN_LINK_EXPIRED;
		case DEVICE_TOKEN_INVALID:
			return LOGIN_PAGE_NOT_AVAILABLE;
		case ACCOUNT_INACTIVE:
			return LOGIN_ACCOUNT_INACTIVE;
		case DEVICE_VERIFIED_LINK_EXPIRED:
		case DEVICE_ALREADY_VERIFIED:
			return LOGIN_DEVICE_ALREADY_VERIFIED;
		default:
			logger.info("Save device details for user with {}", model);
			loginFormVO = new LoginFormVO();
			loginFormVO.setEmailAddress(advisor.getEmail());
			httpsession.setAttribute(LOGIN_FORM_VO, loginFormVO);
			setSecurityContext(request, loginFormVO, model);
			addAuthCookie(httpServletResponse);
			return LOGIN_SAVE_DEVICE;
		}
	}

	@PostMapping(path = { REMEMBER_DEVICE_AND_CONTINUE })
	public String saveDeviceAndContinue(HttpServletRequest request, HttpServletResponse response, Model model,
			SaveDeviceFormVO saveDeviceFormVO) {
		logger.info("DSAO saveDeviceAndContinue {}", saveDeviceFormVO);
		loginService.rememberDeviceAndContinue(request, saveDeviceFormVO);
		return findStudent(model);
	}

	private String findStudent(Model model) {
		FindStudentOptionsFormVO findStudentFormVO = new FindStudentOptionsFormVO();
		model.addAttribute(FIND_STUDENT_OPTION_FORM_VO, findStudentFormVO);
		return REDIRECT + FIND_STUDENT_OPTION;
	}

	@GetMapping(path = { FORGOT_PASSWORD })
	public String displayForgotPasswordPage(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO forgot password page");

		ForgotPasswordFormVO forgotPasswordFormVO = new ForgotPasswordFormVO();
		model.addAttribute("forgotPasswordFormVO", forgotPasswordFormVO);

		return LOGIN_FORGOT_PASSWORD;
	}

	@PostMapping(path = { FORGOT_PASSWORD })
	public String validateForgotEmail(HttpServletRequest request, HttpServletResponse response, Model model,
			ForgotPasswordFormVO forgotPasswordFormVO, BindingResult bindingResult) {
		logger.info("DSAO forgotPasswordFormVO", forgotPasswordFormVO);

		EmailValidator emailValidator = new EmailValidator(advisorLookupService);
		emailValidator.validate(forgotPasswordFormVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return LOGIN_FORGOT_PASSWORD;
		} else {
			loginService.forgotPassword(request, forgotPasswordFormVO);
			return LOGIN_CHECK_EMAIL_FORGOT_PASSWORD;
		}
	}

	@GetMapping(RESET_PASSWORD)
	public String redirectFromForgotPasswordLink(@RequestParam String token, @RequestParam String userId,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		logger.info("Redirect from forgot password for user with token: {} and userId {} requested", token, userId);
		redirectAttributes.addFlashAttribute("token", token);
		redirectAttributes.addFlashAttribute("userId", userId);
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + SET_PASSWORD;
	}

	@GetMapping(LOGIN_SET_PASSWORD)
	public String validateLinkAndSavePassword(Model model, HttpServletRequest request, String token, String userId,
			HttpSession httpsession) {
		Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
		if (inputFlashMap != null) {
			token = (String) inputFlashMap.get("token");
			userId = (String) inputFlashMap.get("userId");
		}

		if (null == token || null == userId) {
			return LOGIN_PAGE_NOT_AVAILABLE;
		}
		logger.info("Check Advisor Details for user with token: {} and userId {} requested", token, userId);

		Response result = loginService.forgottenPassword(token, userId);
		AdvisorResultVO advisor = (AdvisorResultVO) result.getModel();
		CreatePasswordFormVO createPasswordFormVO = new CreatePasswordFormVO();
		createPasswordFormVO.setEmail(advisor.getEmail());
		createPasswordFormVO.setUserId(userId);
		model.addAttribute("createPasswordFormVO", createPasswordFormVO);

		switch (result.getResponseCode()) {
		case RESET_TOKEN_INVALID:
			return LOGIN_PAGE_NOT_AVAILABLE;
		default:
			return LOGIN_CREATE_NEW_PASSWORD;
		}
	}

	@PostMapping(path = { SAVE_NEW_PASSWORD })
	public String saveNewPassword(HttpServletRequest request, HttpServletResponse response, Model model,
			CreatePasswordFormVO createPasswordFormVO, BindingResult bindingResult) {
		logger.info("DSAO createPasswordFormVO", createPasswordFormVO);

		PasswordValidator passwordValidator = new PasswordValidator(advisorLoginService);
		passwordValidator.validate(createPasswordFormVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return LOGIN_CREATE_NEW_PASSWORD;
		} else {
			loginService.saveNewPassword(createPasswordFormVO);
			return LOGIN_PASSWORD_RESET;
		}
	}

	@GetMapping(path = { RESEND_LOGIN_LINK })
	public String resendActivationLink(HttpServletRequest request, HttpServletResponse httpResponse,
			HttpSession httpsession, Model model) {
		logger.info("DSAO Resend activation link {}", httpsession.getAttribute(LOGIN_FORM_VO));

		LoginFormVO loginFormVO = (LoginFormVO) httpsession.getAttribute(LOGIN_FORM_VO);
		loginFormVO.setResendLoginLink(true);

		Response response = loginService.requestEmailVerification(request, loginFormVO.getEmailAddress());

		switch (response.getResponseCode()) {

		case DEVICE_TOKEN_LIMIT_EXCEEDED:
			logger.info("DSAO activation link request exceeded for {}", httpsession.getAttribute(LOGIN_FORM_VO));
			model.addAttribute(ADVISOR, loginFormVO.getEmailAddress());
			return LOGIN_EXCEEDED_NOTIFICATION;
		case ACCOUNT_ALREADY_ACTIVATED:
			logger.info("DSAO account already activated for {}", httpsession.getAttribute(LOGIN_FORM_VO));
			model.addAttribute(ADVISOR, loginFormVO.getEmailAddress());
			return LOGIN_ACCOUNT_ALREADY_VERIFIED;
		case DEVICE_TOKEN_EXPIRED:
			logger.info("DSAO account expired for {}", httpsession.getAttribute(LOGIN_FORM_VO));
			model.addAttribute(ADVISOR, loginFormVO.getEmailAddress());
			return LOGIN_LINK_EXPIRED;
		default:
			logger.info("DSAO Resend activation link request for {}", httpsession.getAttribute(LOGIN_FORM_VO));
			model.addAttribute(LOGIN_FORM_VO, httpsession.getAttribute(LOGIN_FORM_VO));
			httpsession.setAttribute(LOGIN_FORM_VO, loginFormVO);
			return CONFIRM_EMAIL;
		}
	}

	@GetMapping(LOAD_SAVE_DEVICE)
	public String redirectLoadSaveDevice(@RequestParam String token, @RequestParam String userId,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		logger.info(
				"Landing page from email link, redirect to load save device page for user with token: {} and userId {} requested",
				token, userId);
		redirectAttributes.addFlashAttribute("token", token);
		redirectAttributes.addFlashAttribute("userId", userId);
		request.setAttribute(RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
		return REDIRECT + LOGIN_LOAD_SAVE_DEVICE;
	}

	@GetMapping(LOGIN_LOAD_SAVE_DEVICE)
	public String loadSaveDevice(Model model, HttpServletRequest request, String token, String userId,
			HttpSession httpsession) {
		Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
		if (inputFlashMap != null) {
			token = (String) inputFlashMap.get("token");
			userId = (String) inputFlashMap.get("userId");
		}

		if (null == token || null == userId) {
			return LOGIN_PAGE_NOT_AVAILABLE;
		}
		logger.info("Redirected, check Advisor Details for user with token: {} and userId {} requested", token, userId);

		Response result = loginService.forgottenPassword(token, userId);

		AdvisorResultVO advisor = (AdvisorResultVO) result.getModel();
		CreatePasswordFormVO createPasswordFormVO = new CreatePasswordFormVO();
		createPasswordFormVO.setEmail(advisor.getEmail());
		createPasswordFormVO.setUserId(userId);
		model.addAttribute("createPasswordFormVO", createPasswordFormVO);

		logger.info("Redirect to Load device for user with {}", model);

		switch (result.getResponseCode()) {
		case RESET_TOKEN_INVALID:
			return LOGIN_PAGE_NOT_AVAILABLE;
		default:
			SaveDeviceFormVO saveDeviceFormVO = new SaveDeviceFormVO();
			model.addAttribute(ADVISOR, advisor);
			model.addAttribute("saveDeviceFormVO", saveDeviceFormVO);
			return LOGIN_SAVE_DEVICE;
		}
	}

	@GetMapping(path = { LOGOUT })
	public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO sign out page");

		LoginFormVO loginFormVO = new LoginFormVO();
		model.addAttribute(LOGIN_FORM_VO, loginFormVO);
		SecurityContextHolder.clearContext();
		clearAuthCookie(response);

		HttpSession session = request.getSession(false);

		if (session != null) {
			logger.info("DSAO sign out page with active session {} ", session.getId());
			session.invalidate();
		}
		return REDIRECT + "login";
	}

	@GetMapping(path = { "/studentLogout" })
	public String studentLogout(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("DSAO student sign out page");
		SecurityContext securityContext = securityContext();

		if (securityContext != null) {
			String user = (String) securityContext.getAuthentication().getPrincipal();
			loginService.resetStudentAuthDetails(user);
		}

		LoginFormVO loginFormVO = new LoginFormVO();
		model.addAttribute(LOGIN_FORM_VO, loginFormVO);
		SecurityContextHolder.clearContext();
		clearAuthCookie(response);

		HttpSession session = request.getSession(false);

		if (session != null) {
			logger.info("DSAO sign out page with active session {} ", session.getId());
			session.invalidate();
		}

		return "redirect:" + olsLogoutUrl;
	}

	private void clearAuthCookie(HttpServletResponse response) {
		Cookie userCookie = new Cookie("saas-dsa-user", null);
		userCookie.setPath("/");
		userCookie.setDomain("saas.gov.uk");
		userCookie.setMaxAge(0);
		userCookie.setHttpOnly(true);
		response.addCookie(userCookie);
	}

	private void setSecurityContext(HttpServletRequest request, LoginFormVO loginFormVO, Model model) {
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				loginFormVO.getEmailAddress(), loginFormVO.getPassword());
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(dsaAuthenticationProvider.authenticate(authRequest));
		HttpSession session = request.getSession(true);
		session.setAttribute("emailAddress", loginFormVO.getEmailAddress());
		session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		LoggedinUserUtil.setLoggedinUserInToModel(model);
	}

	private void addAuthCookie(HttpServletResponse response) {
		Cookie userCookie = new Cookie("saas-dsa-user", UUID.randomUUID().toString());
		userCookie.setPath("/");
		userCookie.setDomain("saas.gov.uk");
		userCookie.setHttpOnly(true);
		response.addCookie(userCookie);
	}
}
