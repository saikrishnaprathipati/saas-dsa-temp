package uk.gov.saas.dsa.web.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
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
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DeclarationsService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.vo.*;
import uk.gov.saas.dsa.web.controller.declaration.AdvisorDeclarationsController;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.saas.dsa.model.Section.ADVISOR_DECLARATION;
import static uk.gov.saas.dsa.web.controller.declaration.AdvisorDeclarationsController.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class AdvisorDeclarationsControllerTest {
	private static final String DECLARATION_2 = "declaration2";
	private static final String DECLARATION_1 = "declaration1";
	private static final long STUDENT_REFERENCE_NUMBER = 0;
	private static final long DSA_APPLICATION_NUMBER = 0;
	private static final int SESSION_CODE = 0;
	@Autowired
	private MessageSource messageSource;
	@MockitoBean
	private EmailSenderService emailNotificationService;
	@MockitoBean
	private FindStudentService findStudentService;
	@Autowired
	private WebApplicationContext webAppContext;
	@MockitoBean
	private DeclarationsService declarationsService;
	@MockitoBean

	private ApplicationService applicationService;

	private MockMvc mockMvc;
	private AdvisorDeclarationsController subject;

	@BeforeEach
	void setUp() throws Exception {

		subject = new AdvisorDeclarationsController(declarationsService, messageSource, emailNotificationService,

				findStudentService, applicationService);

		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockDeclarationCodes();
		mockSectionStatus(SectionStatus.CANNOT_START_YET);
		mockSecurityContext();
	}

	@Test
	void shouldLoadAdvisorDeclarationsCorrectly() {

		List<DeclarationTypeVO> declarations = subject.getAdvisorDeclarations();
		Assertions.assertEquals(2, declarations.size());
	}

	private void mockDeclarationCodes() {
		DeclarationTypeVO declaration1 = new DeclarationTypeVO();
		declaration1.setDeclarationTypeId(1);
		declaration1.setDeclarationCode(DECLARATION_1);
		declaration1.setDeclarationTypeDesc("declaration 1 desc");
		DeclarationTypeVO declaration2 = new DeclarationTypeVO();
		declaration2.setDeclarationTypeId(2);
		declaration2.setDeclarationCode(DECLARATION_2);
		declaration2.setDeclarationTypeDesc("declaration 2 desc");

		when(declarationsService.findAllActiveDeclarations(ADVISOR_ACTION))

				.thenReturn(Arrays.asList(declaration1, declaration2));
	}

	private void mockSectionStatus(SectionStatus status) {

		ApplicationSectiponStatusVO section = new ApplicationSectiponStatusVO();
		section.setSectionStatus(status);
		when(applicationService.getApplicationSectionStatus(DSA_APPLICATION_NUMBER, ADVISOR_DECLARATION))
				.thenReturn(section);

	}

	@Test
	void shouldInitialiseDeclarationsPageCorrectly() throws Exception {
		ApplicationKeyDataFormVO keyDataFormVo = new ApplicationKeyDataFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + DECLARATION_DETAILS_URI);
		builder.flashAttr("applicationKeyDataFormVO", keyDataFormVo);
		builder.param(ACTION, ADVISOR_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals(ADVISOR_DECLARATION_VIEW, modelAndView.getViewName());

	}

	@Test
	void shouldInitialiseDeclarationsPageCorrectlyWithAllDeclarationsIfTheAdvisorSectionStatusHasBeenCompleted()
			throws Exception {
		mockSectionStatus(SectionStatus.COMPLETED);
		ApplicationKeyDataFormVO keyDataFormVo = new ApplicationKeyDataFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + DECLARATION_DETAILS_URI);
		builder.flashAttr(APPLICATION_KEY_DATA_FORM_VO, keyDataFormVo);
		builder.param(ACTION, ADVISOR_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		ModelMap modelMap = modelAndView.getModelMap();
		String preselectedDeclarations = modelMap.get("advisorDeclarationTypes").toString();
		Assertions.assertNotNull(preselectedDeclarations);
		assertEquals(ADVISOR_DECLARATION_VIEW, modelAndView.getViewName());

	}

	@Test
	void shouldShowErrorPageForUnknownAction() throws Exception {
		ApplicationKeyDataFormVO keyDataFormVo = new ApplicationKeyDataFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + DECLARATION_DETAILS_URI);
		builder.flashAttr(APPLICATION_KEY_DATA_FORM_VO, keyDataFormVo);
		builder.param(ACTION, "BLAH");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals(DSAConstants.ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldValidateSelectAllDeclarationsErrorMessage() throws Exception {
		DeclarationFormVO declarationFormVO = new DeclarationFormVO();
		declarationFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		declarationFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + ADVISOR_DECLARATIONS_URI);
		builder.flashAttr(DECLARATION_FORM_VO, declarationFormVO);
		builder.param(ACTION, I_AGREE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.declarationFormVO");
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
		assertTrue(messageCodes.containsAll(Arrays.asList("You must agree to all statements.")));

		assertEquals(ADVISOR_DECLARATION_VIEW, modelAndView.getViewName());
	}

	@Test
	void shouldValidateSelectAllDeclarationsErrorMessageIfUserNotSelectedAllDeclarations() throws Exception {

		DeclarationFormVO declarationFormVO = mockDeclarationRequest(DECLARATION_1);
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + ADVISOR_DECLARATIONS_URI);
		builder.flashAttr(DECLARATION_FORM_VO, declarationFormVO);
		builder.param(ACTION, I_AGREE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.declarationFormVO");
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
		assertTrue(messageCodes.containsAll(Arrays.asList("You must agree to all statements.")));

		assertEquals(ADVISOR_DECLARATION_VIEW, modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToDashboardPageIfUserSelectsBackAction() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + ADVISOR_DECLARATIONS_URI);
		builder.param(ACTION, DSAConstants.BACK_BUTTON_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldSendToNextPageIfUserSelectedAllDeclarations() throws Exception {

		DeclarationFormVO declarationFormVO = mockDeclarationRequest(DECLARATION_1, DECLARATION_2);

		mockDSAApplicationsMade();
		mockStudentResults();

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + ADVISOR_DECLARATIONS_URI);
		builder.flashAttr(DECLARATION_FORM_VO, declarationFormVO);
		builder.param(ACTION, I_AGREE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.declarationFormVO");
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
		assertFalse(messageCodes.containsAll(Arrays.asList("You must agree to all statements")));

		assertEquals(AdvisorDeclarationsController.ADVISOR_WHAT_HAPPENS_NEXT, modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToDashboardPageIfUserSelectedBreadCrumbAction() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + CONTINUE_NEXT_URI);
		builder.param(ACTION, DSAConstants.APPLICATION_DASHBOARD_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToDeclarationsPageIfUserSelectedBackActionAction() throws Exception {

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/" + CONTINUE_NEXT_URI);
		builder.param(ACTION, DSAConstants.BACK_BUTTON_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(ADVISOR_DECLARATION_VIEW, modelAndView.getViewName());
	}

	private void mockDSAApplicationsMade(){
		DSAApplicationsMade dsaApplicationsMade = new DSAApplicationsMade();
		dsaApplicationsMade.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		dsaApplicationsMade.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		when(applicationService.findByDsaApplicationNumberAndStudentReferenceNumber(DSA_APPLICATION_NUMBER, STUDENT_REFERENCE_NUMBER))
				.thenReturn(dsaApplicationsMade);
	}

	private void mockStudentResults() throws IllegalAccessException {
		StudentResultVO studentResultVO = new StudentResultVO();
		studentResultVO.setEmailAddress("");
		StudentCourseYearVO studentCourseYear = new StudentCourseYearVO();
		studentCourseYear.setAcademicYear("ewew34");
		studentResultVO.setStudentCourseYear(studentCourseYear);
		when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studentResultVO);
		when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER,SESSION_CODE)).thenReturn(studentResultVO);
		StudentPersonalDetails studentPersonalDetails = new StudentPersonalDetails();
		when(findStudentService.findStudentPersonDetailsStudByRefNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studentPersonalDetails);
	}

	private DeclarationFormVO mockDeclarationRequest(String... declarations) {
		DeclarationFormVO declarationFormVO = new DeclarationFormVO();
		declarationFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		declarationFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		declarationFormVO.setDeclarationCodes(Arrays.asList(declarations));
		return declarationFormVO;
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
