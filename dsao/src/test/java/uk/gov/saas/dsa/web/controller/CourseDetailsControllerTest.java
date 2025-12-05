package uk.gov.saas.dsa.web.controller;

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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.model.*;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.CourseDetailsVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class CourseDetailsControllerTest {
	private static final String COURSE_DETAILS2 = "/courseDetails";
	private static final long STUDENT_REFERENCE_NUMBER = -12222L;
	private static final long DSA_APPLICATION_NUMBER = -1l;
	private static final String COURSE_DETAILS = "advisor/courseDetails";
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private CourseDetailsService courseDetailsService;

	@MockitoBean
	private ApplicationService applicationService;

	@MockitoBean
	private Model model;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void shouldRedirectToCourseDetailsPageCorrectly() throws Exception {

		ApplicationKeyDataFormVO aboutDetailsFormVO = new ApplicationKeyDataFormVO();
		aboutDetailsFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		aboutDetailsFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		ApplicationResponse applicationResponse = mockApplicationResponse();
		applicationResponse.setSessionCode(0);
		when(applicationService.findApplication(DSA_APPLICATION_NUMBER,STUDENT_REFERENCE_NUMBER)).thenReturn(applicationResponse);

		CourseDetailsVO courseDetails = new CourseDetailsVO();
		when(courseDetailsService.findCourseDetailsFromDB(STUDENT_REFERENCE_NUMBER,0)).thenReturn(courseDetails);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(COURSE_DETAILS2);
		builder.flashAttr("applicationKeyDataFormVO", aboutDetailsFormVO);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(COURSE_DETAILS, modelAndView.getViewName());
	}

	@Test
	void shouldThrowExceptionIfNoStudentDetailsFound() throws Exception {

		ApplicationKeyDataFormVO aboutDetailsFormVO = new ApplicationKeyDataFormVO();
		aboutDetailsFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		aboutDetailsFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ApplicationResponse applicationResponse = new ApplicationResponse();
		applicationResponse.setSessionCode(0);
		when(applicationService.findApplication(DSA_APPLICATION_NUMBER,STUDENT_REFERENCE_NUMBER))
				.thenReturn(applicationResponse);
		when(courseDetailsService.findCourseDetailsFromDB(STUDENT_REFERENCE_NUMBER, 0))
				.thenThrow(new IllegalAccessException("Some Exception"));

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(COURSE_DETAILS2);
		builder.flashAttr("applicationKeyDataFormVO", aboutDetailsFormVO);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(ERROR_PAGE, modelAndView.getViewName());
	}
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private ApplicationResponse mockApplicationResponse() {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		ApplicationSectionResponse sectionResponse = new ApplicationSectionResponse();
		SectionStatusResponse someSectionData = new SectionStatusResponse();
		someSectionData.setSection(Section.ABOUT_COURSE);
		someSectionData.setSectionStatus(SectionStatus.COMPLETED);

		sectionResponse.setAboutCourseSectionData(someSectionData);
		sectionResponse.setAboutStudentSectionData(someSectionData);
		sectionResponse.setDisabilitySectionData(someSectionData);
		sectionResponse.setAllowanceSectionData(someSectionData);
		sectionResponse.setNeedsAssessmentFeeSectionData(someSectionData);
		sectionResponse.setStudentDeclarationSectionData(someSectionData);
		sectionResponse.setAdvisorDeclarationSectionData(someSectionData);

		applicationResponse.setSectionStatusData(sectionResponse);
		applicationResponse.setApplicationStatus(ApplicationSummaryStatus.COMPLETED);
		applicationResponse.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		return applicationResponse;
	}
}
