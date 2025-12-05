package uk.gov.saas.dsa.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import uk.gov.saas.dsa.model.*;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.DashboardFormVO;
import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {
	private static final String INIT_DASHBOARD = "/initApplicationDashboard";
	private static final long STUDENT_REFERENCE_NUMBER = -12222L;
	private static final long DSA_APPLICATION_NUMBER = -28267L;
	private static final String APPLICATION_DASHBOARD = "advisor/applicationDashboard";
	private static final String APPLICATION_DASHBOARD_URI = "/applicationDashboard";
	private static final String CONFIRM_RESUBMIT_APPLICATION = "advisor/confirmResubmitApplication";
	private MockMvc mockMvc;

	@MockitoBean
	private FindStudentService findStudentService;

	@MockitoBean
	private ApplicationService applicationService;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private Model model;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void showConfirmResubmitApplicationWhenNoAcademicYear() throws Exception {

		DashboardFormVO dashboardFormVO = new DashboardFormVO();
		dashboardFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		dashboardFormVO.setFundingEligibilityStatus(FundingEligibilityStatus.CONFIRMED);
		
		ApplicationResponse applicationResponse = mockApplicationResponse();
		applicationResponse.setNewApplication(true);
		applicationResponse.setDsaApplicationNumber(STUDENT_REFERENCE_NUMBER);
		when(applicationService.findApplicationByStudentReferenceNumber(STUDENT_REFERENCE_NUMBER))
				.thenReturn(applicationResponse);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(INIT_DASHBOARD);
		builder.flashAttr("dashboardFormVO", dashboardFormVO);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(CONFIRM_RESUBMIT_APPLICATION, modelAndView.getViewName());
	}

	@Test
	void shouldLoadDashboardDataCorrectlyWithApplicationStatusAndStudentInfo() throws Exception {

		ApplicationKeyDataFormVO keyDataVo = new ApplicationKeyDataFormVO();
		keyDataVo.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataVo.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		ApplicationResponse applicationResponse = mockApplicationResponse();
		applicationResponse.setNewApplication(true);
		applicationResponse.setDsaApplicationNumber(STUDENT_REFERENCE_NUMBER);
		applicationResponse.setOverallApplicationStatus(OverallApplicationStatus.STARTED);
		when(applicationService.findApplication(DSA_APPLICATION_NUMBER, STUDENT_REFERENCE_NUMBER))
				.thenReturn(applicationResponse);

		StudentResultVO studentResultVO = new StudentResultVO();
		StudentCourseYearVO courseYearVO = new StudentCourseYearVO();
		studentResultVO.setStudentCourseYear(courseYearVO);

		when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studentResultVO);

		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(APPLICATION_DASHBOARD_URI);
		builder.flashAttr("applicationKeyDataFormVO", keyDataVo);
		builder.flashAttr("dashboardData", applicationResponse);

		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(APPLICATION_DASHBOARD, modelAndView.getViewName());
		verify(applicationService, times(0)).createApplication(STUDENT_REFERENCE_NUMBER,
				FundingEligibilityStatus.CONFIRMED,0);
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
		sectionResponse.setAdditionalInfoData(someSectionData);

		applicationResponse.setSectionStatusData(sectionResponse);
		applicationResponse.setApplicationStatus(ApplicationSummaryStatus.COMPLETED);
		applicationResponse.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		return applicationResponse;
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
