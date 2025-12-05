package uk.gov.saas.dsa.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DisabilitiesService;
import uk.gov.saas.dsa.service.StudentDetailsService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DisabilitiesFormVO;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class DisabilitiesControllerTest {
	private static final long DSA_APPLICATION_NUMBER = -1l;
	private static final long STUDENT_REFERENCE_NUMBER = -12222L;
	@MockitoBean
	private StudentDetailsService studentDetailsService;
	@MockitoBean
	private DisabilitiesService disabilitiesService;
	@MockitoBean
	private ApplicationService applicationService;
	@Autowired
	private WebApplicationContext webAppContext;
	private MockMvc mockMvc;
	DisabilitiesController subject;

	@BeforeEach
	void setUp() throws Exception {
		subject = new DisabilitiesController(studentDetailsService, disabilitiesService, applicationService);
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);
		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void shouldLoadActiveDisabilitiesCorrectly() throws Exception {
		List<DisabilityTypeVO> activeDisabilityTypes = new ArrayList<>();

		Mockito.when(disabilitiesService.getActiveDisabilityTypes()).thenReturn(activeDisabilityTypes);

		List<DisabilityTypeVO> disabilityTypes = subject.getDisabilityTypes();
		Assertions.assertSame(activeDisabilityTypes, disabilityTypes);
	}

	@Test
	void shouldInitDisabilitiesDetailsPageCorrectly() throws Exception {

		ApplicationKeyDataFormVO keyDataFormVo = new ApplicationKeyDataFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		List<DisabilityTypeVO> list = new ArrayList<>();
		DisabilityTypeVO disabilityTypeVO = new DisabilityTypeVO();
		disabilityTypeVO.setDisabilityCode(DisabilityTypeVO.DISABILITY_NOT_LISTED);
		disabilityTypeVO.setDisabilityNotlistedText("blah");
		list.add(disabilityTypeVO);

		Mockito.when(disabilitiesService.populateApplicationDisabilities(DSA_APPLICATION_NUMBER)).thenReturn(list);

		StudentResultVO studentResultVO = new StudentResultVO();
		when(studentDetailsService.findStudentDetailsFromDB(STUDENT_REFERENCE_NUMBER)).thenReturn(studentResultVO);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilityDetails");
		builder.flashAttr("applicationKeyDataFormVO", keyDataFormVo);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals("advisor/disabilities", modelAndView.getViewName());
	}

	@Test
	void shouldValidateDisabilitiesCodesCorrectly() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", DSAConstants.SAVE_AND_CONTINUE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.disabilitiesFormVO");
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<String> messages = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
		assertTrue(messages.containsAll(Arrays.asList("You must select at least 1 disability to continue.")));
		assertEquals("advisor/disabilities", modelAndView.getViewName());
	}

	@Test
	void shouldValidateNotListedTextIsEmpty() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		keyDataFormVo.setDisabilityCodes(Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", DSAConstants.SAVE_AND_CONTINUE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.disabilitiesFormVO");
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getCode).collect(Collectors.toList());
		assertTrue(messageCodes.containsAll(Arrays.asList("disability.notListedText.required")));

		assertEquals("advisor/disabilities", modelAndView.getViewName());
	}

	@Test
	void shouldValidateNotListedTextDataHavingNonASCIICharacters() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		keyDataFormVo.setDisabilityCodes(Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED));
		keyDataFormVo.setNotListedText("£££££");
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", DSAConstants.SAVE_AND_CONTINUE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model
				.get("org.springframework.validation.BindingResult.disabilitiesFormVO");
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getCode).collect(Collectors.toList());
		assertTrue(messageCodes.containsAll(Arrays.asList("disability.notListedText.invalid")));

		assertEquals("advisor/disabilities", modelAndView.getViewName());
	}

	@Test
	void shouldSaveDisabilitiesCorrectly() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		keyDataFormVo.setDisabilityCodes(Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED, "BLAH"));
		keyDataFormVo.setNotListedText("ABC");
		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", DSAConstants.SAVE_AND_CONTINUE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals("redirect:/disabilitiesSummary", modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToConsumablesSummaryIfExits() throws Exception {
		mokConsumableItems();
		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();
		keyDataFormVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		keyDataFormVo.setDisabilityCodes(Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED, "BLAH"));
		keyDataFormVo.setNotListedText("ABC");
		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", DSAConstants.SAVE_AND_CONTINUE_ACTION);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals("redirect:/disabilitiesSummary", modelAndView.getViewName());
	}

	private void mokConsumableItems() throws IllegalAccessException {

		ApplicationResponse applicationResponse = new ApplicationResponse();
		Mockito.when(applicationService.findApplication(DSA_APPLICATION_NUMBER, STUDENT_REFERENCE_NUMBER))
				.thenReturn(applicationResponse);

	}

	@Test
	void shouldProcessBackActionCorrectly() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();

		ResultMatcher redirection = MockMvcResultMatchers.status().is3xxRedirection();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", "BACK");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals("redirect:/applicationDashboard", modelAndView.getViewName());
	}

	@Test
	void shouldProcessInvalidActionCorrectly() throws Exception {

		DisabilitiesFormVO keyDataFormVo = new DisabilitiesFormVO();

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/disabilities");
		builder.flashAttr("disabilitiesFormVO", keyDataFormVo);
		builder.param("action", "BLAHH");
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();

		assertEquals("error", modelAndView.getViewName());
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
