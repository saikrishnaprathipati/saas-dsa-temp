package uk.gov.saas.dsa.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.saas.dsa.model.FundingEligibilityStatus;
import uk.gov.saas.dsa.service.StudentDetailsService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class StudentDetailsControllerTest {
	private static final long STUDENT_REFERENCE_NUMBER = -12222L;
	private static final String ADVISOR_ABOUT_DETAILS = "advisor/studentDetails";
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private StudentDetailsService aboutYourDetailsService;

	@MockitoBean
	private Model model;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void shouldRedirectToStudentDetailsPageCorrectly() throws Exception {

		ApplicationKeyDataFormVO aboutDetailsFormVO = new ApplicationKeyDataFormVO();
		aboutDetailsFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		StudentResultVO studentResultVO = new StudentResultVO();
		studentResultVO.setFundingEligibilityStatus(FundingEligibilityStatus.CONFIRMED);
		when(aboutYourDetailsService.findStudentDetailsFromDB(STUDENT_REFERENCE_NUMBER)).thenReturn(studentResultVO);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/studentDetails");
		builder.flashAttr("applicationKeyDataFormVO", aboutDetailsFormVO);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(ADVISOR_ABOUT_DETAILS, modelAndView.getViewName());
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
