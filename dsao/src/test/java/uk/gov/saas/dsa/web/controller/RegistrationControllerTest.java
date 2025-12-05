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
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.service.RegistrationService;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

	private MockMvc mockMvc;
	
	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private AdvisorLookupService advisorLookupService;
	
	@MockitoBean
	private RegistrationService	registrationService;

	@MockitoBean
	private Model model;
	
	@MockitoBean
    HttpSession mockHttpSession;
	
	private static final String VALIDATION_BINDING = "org.springframework.validation.BindingResult.createAccountFormVO";

	@BeforeEach
	void setUp() throws Exception {
		RegistrationController controller = new RegistrationController(advisorLookupService, registrationService);
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}
	
	@Test
	void shouldRedirectToStartPage_Successfully() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/start");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/start", modelAndView.getViewName());
	}
	
	@Test
	void shouldRedirectToPrivacyPolicyPage_Successfully() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/privacyPolicy");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/privacyPolicy", modelAndView.getViewName());
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
		
		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("createAccountFormVO", createAccountFormVO);

		when(registrationService.requestActivation(any())).thenReturn(new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendEmail")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/confirmEmail", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToResendEmailPage_activationLimitExceeded() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");
		createAccountFormVO.setResendEmailLink(true);

		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("createAccountFormVO", createAccountFormVO);

		when(registrationService.requestActivation(any())).thenReturn(new Response(ResponseCode.ACTIVATION_LIMIT_EXCEEDED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendEmail")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/exceededNotification", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToResendEmailPage_accountAlreadyActivated() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");
		createAccountFormVO.setResendEmailLink(true);

		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("createAccountFormVO", createAccountFormVO);

		when(registrationService.requestActivation(any())).thenReturn(new Response(ResponseCode.ACCOUNT_ALREADY_ACTIVATED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendEmail")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("createAccountFormVO", createAccountFormVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/accountAlreadyActivated", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToResendEmailPage_activationLinkExpired() throws Exception {

		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("test@gcu.co.uk");
		createAccountFormVO.setResendEmailLink(true);

		HashMap<String, Object> sessionAttribute = new HashMap<>();
		sessionAttribute.put("createAccountFormVO", createAccountFormVO);

		when(registrationService.requestActivation(any())).thenReturn(new Response(ResponseCode.ACTIVATION_LINK_EXPIRED, null));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/resendEmail")
				.sessionAttrs(sessionAttribute);
		builder.flashAttr("createAccountFormVO", createAccountFormVO);
		AdvisorResultVO advisorResultVO = new AdvisorResultVO();
		advisorResultVO.setEmail("");
		builder.flashAttr("advisor", advisorResultVO);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals("register/expiredNotification", modelAndView.getViewName());
	}
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}

