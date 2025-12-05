package uk.gov.saas.dsa.web.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.service.AdvisorLoginService;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.service.LoginService;
import uk.gov.saas.dsa.service.RegistrationService;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;
import uk.gov.saas.dsa.vo.LoginFormVO;
import uk.gov.saas.dsa.vo.SaveDeviceFormVO;
import uk.gov.saas.dsa.web.config.DsaAuthenticationProvider;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private AdvisorLookupService advisorLookupService;

	@MockitoBean
	private AdvisorLoginService advisorLoginService;
	@MockitoBean
	private LoginService loginService;

	@MockitoBean
	private RegistrationService registrationService;

	@MockitoBean
	private DsaAuthenticationProvider dsaAuthenticationProvider;
	@MockitoBean
	private DSAEmailConfigProperties emailConfigProperties;
	@MockitoBean
	private Model model;

	@MockitoBean
	HttpSession mockHttpSession;

	private static final String VALIDATION_BINDING = "org.springframework.validation.BindingResult.createAccountFormVO";

	@BeforeEach
	void setUp() throws Exception {
		LoginController controller = new LoginController(advisorLoginService, advisorLookupService, loginService,
				dsaAuthenticationProvider, emailConfigProperties);
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}
 

	@Test
	void shouldRedirectToLoginPage_Successfully() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/login");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/login", modelAndView.getViewName());
	}

	@Test
	void testLogin_withErrors() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/login");
		builder.flashAttr("loginFormVO", loginFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/login", modelAndView.getViewName());
	}

	@Test
	void testLogin_deviceVerifiedSuccess() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("passwordpassword");

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
		dsaAdvisorLoginDetails.setSecretKey("BFCvvVVNNXXXxOQx4bnj46Jf4D/dqDromgISjm83zqQ=");
		dsaAdvisorLoginDetails.setPassword("EStURKn2tmK0b7IsI0WrM/Fk/XltfNc3oAhM8uBr+LdZRLvi9nfSnj8BXTvf9ZA2");
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		when(advisorLoginService.findByEmail(any())).thenReturn(dsaAdvisorLoginDetails);

		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		when(loginService.verifyDevice(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_VERIFIED, advisorResultVO));

		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/login");
		builder.flashAttr("loginFormVO", loginFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("redirect:/findStudents", modelAndView.getViewName());
	}

	@Test
	void testLogin_success() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("passwordpassword");

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
		dsaAdvisorLoginDetails.setSecretKey("BFCvvVVNNXXXxOQx4bnj46Jf4D/dqDromgISjm83zqQ=");
		dsaAdvisorLoginDetails.setPassword("EStURKn2tmK0b7IsI0WrM/Fk/XltfNc3oAhM8uBr+LdZRLvi9nfSnj8BXTvf9ZA2");
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		when(advisorLoginService.findByEmail(any())).thenReturn(dsaAdvisorLoginDetails);

		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		when(loginService.verifyDevice(any(), any()))
				.thenReturn(new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, advisorResultVO));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/login");
		builder.flashAttr("loginFormVO", loginFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/confirmEmail", modelAndView.getViewName());
	}

	@Test
	void testLogin_failedPassword() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcdefghijklmnopqrstuvwxyz");
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/login");
		builder.flashAttr("loginFormVO", loginFormVO);
		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
		dsaAdvisorLoginDetails.setFailedPasswordCount(0);
		dsaAdvisorLoginDetails.setSecretKey("abcd");
		dsaAdvisorLoginDetails.setPassword("abcdefghijklmnopqrstuvwxyz");
		when(advisorLoginService.findByEmail(any())).thenReturn(dsaAdvisorLoginDetails);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/login", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToCreateAccountPage_Successfully() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/createAccount");
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/createAccount", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToConfirmEmailPage_Successfully() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");

		DsaAdvisor advisor = new DsaAdvisor();
		when(advisorLookupService.findByEmail(any())).thenReturn(advisor);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/lookupAccount");
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/confirmEmail", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToCreateAccount_whenLookupAccountHasErrors() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test");

		when(advisorLookupService.findByEmail(any())).thenReturn(null);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/lookupAccount");
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model.get(VALIDATION_BINDING);
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		assertEquals("test", fieldErrors.get(0).getRejectedValue());
		assertEquals("register/createAccount", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToResendEmailPage_Successfully() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");
		createAccountFormVO.setResendEmailLink(true);

		HashMap<String, Object> sessionattr = new HashMap<>();
		sessionattr.put("createAccountFormVO", createAccountFormVO);

		when(registrationService.requestActivation(any()))
				.thenReturn(new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendEmail").sessionAttrs(sessionattr);
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/confirmEmail", modelAndView.getViewName());
	}

	@Test
	void test_saveDevice() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
				.get("/saveDevice?token=f1e3f2527ba450fc0331541d8f598de0&userId=DT0004");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("redirect:/saveMyDevice", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_Successfully() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);
		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, advisorResultVO));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/saveDevice", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_limitExceeded() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);
		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_LIMIT_EXCEEDED, advisorResultVO));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/exceededNotification", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_deviceAlreadyVerified() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);
		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_ALREADY_VERIFIED, advisorResultVO));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/deviceAlreadyVerified", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_deviceTokenVerified() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);
		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_EXPIRED, advisorResultVO));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/linkExpired", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_accountInactive() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);
		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.ACCOUNT_INACTIVE, advisorResultVO));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/accountInactive", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "1234");
		builder.flashAttr("userId", "1234");

		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_INVALID, new AdvisorResultVO()));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/pageNotAvailable", modelAndView.getViewName());
	}

	@Test
	void test_saveMyDevice_tokenIsNull() throws Exception {

		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setEmailAddress("test@gcu.co.uk");
		loginFormVO.setPassword("abcd");
		ResultMatcher successful = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/saveMyDevice");
		builder.flashAttr("token", "null");
		builder.flashAttr("userId", "null");

		when(loginService.saveDeviceDetails(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_INVALID, new AdvisorResultVO()));
		MvcResult result = this.mockMvc.perform(builder).andExpect(successful).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/pageNotAvailable", modelAndView.getViewName());
	}

	@Test
	void testSaveDeviceAndContinue() throws Exception {

		SaveDeviceFormVO saveDeviceFormVO = new SaveDeviceFormVO();
		saveDeviceFormVO.setEmail("test@gcu.co.uk");
		saveDeviceFormVO.setUserId("abcd");
		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/rememberDeviceAndContinue");
		builder.flashAttr("saveDeviceFormVO", saveDeviceFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("redirect:/findStudents", modelAndView.getViewName());
	}

	@Test
	void testForgotPassword() throws Exception {
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/forgotPassword");

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/forgotPassword", modelAndView.getViewName());
	}

	@Test
	void testResendEmailPage_tokenLimitExpired() throws Exception {
		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setResendLoginLink(true);
		loginFormVO.setPassword("");
		loginFormVO.setEmailAddress("abc@gmail.com");
		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("loginFormVO", loginFormVO);

		when(loginService.requestEmailVerification(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_LIMIT_EXCEEDED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendLoginLink")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("loginFormVO", loginFormVO);
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/exceededNotification", modelAndView.getViewName());
	}

	@Test
	void testResendEmailPage_deviceTokenExpired() throws Exception {
		LoginFormVO loginFormVO = new LoginFormVO();
		loginFormVO.setResendLoginLink(true);
		loginFormVO.setPassword("");
		loginFormVO.setEmailAddress("abc@gmail.com");
		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("loginFormVO", loginFormVO);

		when(loginService.requestEmailVerification(any(), any()))
				.thenReturn(new Response(ResponseCode.DEVICE_TOKEN_REQUESTED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendLoginLink")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("loginFormVO", loginFormVO);
		builder.flashAttr("advisor", loginFormVO.getEmailAddress());
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("login/confirmEmail", modelAndView.getViewName());
	}
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}

