package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.DSAApplicationComplete;
import uk.gov.saas.dsa.domain.DSAApplicationSectionStatus;
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.model.*;
import uk.gov.saas.dsa.persistence.DSAApplicationSectionStatusRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationsMadeRepository;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.saas.dsa.model.ApplicationSummaryStatus.APPLICATION_INCOMPLETE;
import static uk.gov.saas.dsa.model.FundingEligibilityStatus.CONFIRMED;

@ExtendWith(SpringExtension.class)
class ApplicationServiceTest {
	private static final long STUDENT_REF_NO = 12l;
	private static final long DSA_APPLICATION_NUMBER = 233l;
	@MockitoBean
	private DSAApplicationsMadeRepository applicationsMadeRepository;
	@MockitoBean
	private DSAApplicationSectionStatusRepository applicationSectionStatusRepository;

	@MockitoBean
	private DisabilitiesService disabilitiesService;
	@MockitoBean
	private ConfigDataService configDataService;
	private ApplicationService subject;
	private StudentDetailsService studentDetailsService;

	@Captor
	ArgumentCaptor<DSAApplicationsMade> statusCaptor;
	DSAApplicationDeletionServcie deleteService;
	@BeforeEach
	public void setUp() throws Exception {
		subject = new ApplicationService(  applicationsMadeRepository,
				  applicationSectionStatusRepository,
				configDataService, studentDetailsService,   deleteService);
		mockSecurityContext();
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void shouldCreateDSAApplicationSuccessfulWithTheStudentReferenceNumber() throws IllegalAccessException {

		DSAApplicationsMade applicationsMade = new DSAApplicationsMade();
		applicationsMade.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);
		applicationsMade.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		when(applicationsMadeRepository.save(Mockito.any(DSAApplicationsMade.class))).thenReturn(applicationsMade);

		List<DSAApplicationSectionStatus> list = new ArrayList<>();

		when(applicationSectionStatusRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER)).thenReturn(list);
		ApplicationResponse applicationResponse = subject.createApplication(STUDENT_REF_NO, CONFIRMED, Year.now().getValue());
		assertNotNull(applicationResponse);

	}

	@Test
	void shouldFindDSAApplicationWithTheStudentReferenceNumberAndDSAApplicationNumber() throws IllegalAccessException {
		DSAApplicationsMade data = new DSAApplicationsMade();
		data.setStudentReferenceNumber(STUDENT_REF_NO);
		data.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		data.setApplicationStudConsumables(new ArrayList<>());
		List<DSAApplicationSectionStatus> statusList = new ArrayList<>();
		data.setDsaApplicationSectionStatus(statusList);
		when(applicationsMadeRepository.findByDsaApplicationNumberAndStudentReferenceNumber(DSA_APPLICATION_NUMBER,
				STUDENT_REF_NO)).thenReturn(data);
		ApplicationResponse response = subject.findApplication(DSA_APPLICATION_NUMBER, STUDENT_REF_NO);
		assertNotNull(response);

	}

	@Test
	void shouldGetTheCompletedSectionsTextCorrectly() throws IllegalAccessException {
		DSAApplicationsMade data = new DSAApplicationsMade();
		data.setStudentReferenceNumber(STUDENT_REF_NO);
		data.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		List<DSAApplicationSectionStatus> statusList = new ArrayList<>();
		DSAApplicationSectionStatus courseStatus = new DSAApplicationSectionStatus();
		courseStatus.setSectionCode(Section.ABOUT_COURSE);
		courseStatus.setSectionPart(ApplicationSectionPart.PART1);
		courseStatus.setSectionStatus(SectionStatus.COMPLETED);

		DSAApplicationSectionStatus advisorDeclaration = new DSAApplicationSectionStatus();
		advisorDeclaration.setSectionCode(Section.ADVISOR_DECLARATION);
		advisorDeclaration.setSectionPart(ApplicationSectionPart.PART3);
		advisorDeclaration.setSectionStatus(SectionStatus.CANNOT_START_YET);

		DSAApplicationSectionStatus disabilities = new DSAApplicationSectionStatus();
		disabilities.setSectionCode(Section.DISABILITIES);
		disabilities.setSectionPart(ApplicationSectionPart.PART2);
		disabilities.setSectionStatus(SectionStatus.NOT_STARTED);

		statusList.add(courseStatus);
		statusList.add(disabilities);
		statusList.add(advisorDeclaration);

		data.setDsaApplicationSectionStatus(statusList);
		data.setApplicationStudConsumables(new ArrayList<>());
		when(applicationsMadeRepository.findByDsaApplicationNumberAndStudentReferenceNumber(DSA_APPLICATION_NUMBER,
				STUDENT_REF_NO)).thenReturn(data);
		ApplicationResponse response = subject.findApplication(DSA_APPLICATION_NUMBER, STUDENT_REF_NO);
		assertNotNull(response);
		Assertions.assertEquals("1 of 1 sections completed", response.getPart1CompletionStatusText());
		Assertions.assertEquals("0 of 1 sections completed", response.getPart2CompletionStatusText());
		Assertions.assertEquals("0 of 1 sections completed", response.getPart3CompletionStatusText());
	}

	@Test
	void shouldSetNewApplicationAsTrueIfAnCompleteApplicationAlreadyExists() {
		List<DSAApplicationsMade> list = new ArrayList<>();
		DSAApplicationsMade application1 = new DSAApplicationsMade();
		application1.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		List<DSAApplicationSectionStatus> applicationSectionStatus = new ArrayList<>();

		application1.setDsaApplicationSectionStatus(applicationSectionStatus);

		application1.setApplicationSummaryStatus(ApplicationSummaryStatus.COMPLETED);
		list.add(application1);

		DSAApplicationComplete completedApplication = new DSAApplicationComplete();
		completedApplication.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);

		when(applicationsMadeRepository.findByStudentReferenceNumber(STUDENT_REF_NO)).thenReturn(list);
		ApplicationResponse response = subject.findApplicationByStudentReferenceNumber(STUDENT_REF_NO);
		assertNotNull(response);

		Assertions.assertTrue(response.isNewApplication());
	}

	@Test
	void shouldNotUpdateOverAllApplicationStatusIfAnyNonCompletedSectionIsPresent() {
		DSAApplicationsMade value = new DSAApplicationsMade();
		DSAApplicationSectionStatus disabilities = new DSAApplicationSectionStatus();
		disabilities.setSectionCode(Section.DISABILITIES);
		disabilities.setSectionPart(ApplicationSectionPart.PART2);
		disabilities.setSectionStatus(SectionStatus.NOT_STARTED);

		value.setDsaApplicationSectionStatus(java.util.Arrays.asList(disabilities));
		value.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);
		Optional<DSAApplicationsMade> application = Optional.of(value);
		when(applicationsMadeRepository.findById(DSA_APPLICATION_NUMBER)).thenReturn(application);

		subject.updateOverallApplicationSummaryStatus(DSA_APPLICATION_NUMBER);

		verify(applicationsMadeRepository, Mockito.times(0)).save(Mockito.any(DSAApplicationsMade.class));
	}

	@Test
	void shouldUpdateOverAllApplicationSummaryStatus() {

		DSAApplicationsMade value = new DSAApplicationsMade();
		value.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);

		DSAApplicationSectionStatus disabilities = new DSAApplicationSectionStatus();
		disabilities.setSectionCode(Section.DISABILITIES);
		disabilities.setSectionPart(ApplicationSectionPart.PART2);
		disabilities.setSectionStatus(SectionStatus.COMPLETED);

		value.setDsaApplicationSectionStatus(java.util.Arrays.asList(disabilities));
		value.setApplicationSummaryStatus(APPLICATION_INCOMPLETE);
		Optional<DSAApplicationsMade> application = Optional.of(value);
		when(applicationsMadeRepository.findById(DSA_APPLICATION_NUMBER)).thenReturn(application);

		subject.updateOverallApplicationSummaryStatus(DSA_APPLICATION_NUMBER);

		verify(applicationsMadeRepository).save(statusCaptor.capture());
		DSAApplicationsMade savedData = statusCaptor.getValue();
		Assertions.assertEquals(ApplicationSummaryStatus.COMPLETED, savedData.getApplicationSummaryStatus());
		verify(applicationsMadeRepository, Mockito.times(1)).save(Mockito.any(DSAApplicationsMade.class));
	}

	@Test
	void shouldNotUpdateSectionStatusIFSameStatusAlreadyExistsInDB() throws IllegalAccessException {
		DSAApplicationSectionStatus appSection = new DSAApplicationSectionStatus();
		appSection.setSectionStatus(SectionStatus.CANNOT_START_YET);

		when(applicationSectionStatusRepository.findByDsaApplicationNumberAndSectionCode(DSA_APPLICATION_NUMBER,
				Section.ABOUT_COURSE)).thenReturn(appSection);
		subject.updateSectionStatus(DSA_APPLICATION_NUMBER, Section.ABOUT_COURSE, SectionStatus.CANNOT_START_YET);

		verify(applicationSectionStatusRepository, Mockito.times(0))
				.save(Mockito.any(DSAApplicationSectionStatus.class));

	}

	@Test
	void shouldNotUpdateSectionStatusIfStatusIsNotSameAsExistingStatusInDB() throws IllegalAccessException {
		DSAApplicationSectionStatus appSection = new DSAApplicationSectionStatus();
		appSection.setSectionStatus(SectionStatus.NOT_STARTED);
		appSection.setSectionCode(Section.ABOUT_COURSE);
		when(applicationSectionStatusRepository.findByDsaApplicationNumberAndSectionCode(DSA_APPLICATION_NUMBER,
				Section.ABOUT_COURSE)).thenReturn(appSection);
		subject.updateSectionStatus(DSA_APPLICATION_NUMBER, Section.ABOUT_COURSE, SectionStatus.CANNOT_START_YET);

		/*
		 * when(applicationSectionStatusRepository.save(Mockito.any(
		 * DSAApplicationSectionStatus.class))) .thenReturn(appSection);
		 */
		verify(applicationSectionStatusRepository, Mockito.times(0))
				.save(Mockito.any(DSAApplicationSectionStatus.class));

	}

	@Test
	void shouldThrowExceptionIfNoSectionFound() {
		IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			subject.updateSectionStatus(DSA_APPLICATION_NUMBER, Section.ABOUT_COURSE, SectionStatus.CANNOT_START_YET);
		});
		Assertions.assertEquals("No Application found for dsaApplicationNumber:233", thrown.getMessage());

	}

	@Test
	void shouldThrowExceptionIfNoApplicationFound() {
		IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			subject.updateOverallApplicationSummaryStatus(DSA_APPLICATION_NUMBER);
		});
		Assertions.assertEquals("No Application found for dsaApplicationNumber:233", thrown.getMessage());

	}
}
