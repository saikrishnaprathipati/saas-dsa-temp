package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.InstituteGrouping;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.persistence.ConfigDataRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationsMadeRepository;
import uk.gov.saas.dsa.persistence.InstituteGroupingRepository;
import uk.gov.saas.dsa.persistence.StudentPersonalDetailsRepository;
import uk.gov.saas.dsa.persistence.readonly.StudCourseYearRepository;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;
import uk.gov.saas.dsa.persistence.readonly.StudSessionRepository;
import uk.gov.saas.dsa.persistence.refdata.InstitutionRepository;
import uk.gov.saas.dsa.vo.StudentResultVO;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FindStudentServiceTest {

	private static final long STUDENT_REF_NO = 1l;
	@MockitoBean
	private StudRepository studRepo;
	@MockitoBean
	private ConfigDataRepository configDataRepository;
	@MockitoBean
	private InstituteGroupingRepository instituteGroupingRepository;
	@MockitoBean
	private InstitutionRepository institutionRepository;
	@MockitoBean
	private StudentPersonalDetailsRepository studentPersonalDetailsRepository;
	@MockitoBean
	private StudSessionRepository studSessionRepository;
	@MockitoBean
	private StudCourseYearRepository studCourseYearRepository;
	@MockitoBean
	private DSAApplicationsMadeRepository dsaApplicationsMadeRepository;

	private FindStudentService subject;
	String CURRENT_SESSION = "CURRENT_SESSION";
	private static final String SESSION_TO_FETCH_TO = "SESSION_TO_FETCH_TO";
	private static final String SESSION_TO_FETCH_FROM = "SESSION_TO_FETCH_FROM";
	private static final String EXCLUDE_Z_NAMED_INSTITUTION_NAME = "Z-%";
	private static final String LATEST_CODE_INDICATOR_YES = "Y";

	@BeforeEach
	public void setUp() throws Exception {
		subject = new FindStudentService(studRepo, instituteGroupingRepository, institutionRepository,
				studentPersonalDetailsRepository, studSessionRepository, studCourseYearRepository,
				dsaApplicationsMadeRepository);
	}

	@Test
	void shouldLoadSessionConfigDataCorrectly() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);

			Map<String, Integer> sessionsToFetch = subject.sessionsToFetch();
			assertEquals(sessionsToFetch.get(SESSION_TO_FETCH_TO), 2000);
			assertEquals(sessionsToFetch.get(SESSION_TO_FETCH_FROM), 2000);
		}
	}

	@Test
	void shouldLoadStudentDetailsWithFirstThreeLettersOfFirstNameWithOutDOB() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			String forename = "foreName";
			String lastname = "lastname";
			List<StudentResultVO> list = subject.findByForenamesAndSurnameAndDobStud(forename, lastname, null);

			verify(studRepo, times(1))
					.findByForenamesStartsWithAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							"FOR", lastname.toUpperCase(), EXCLUDE_Z_NAMED_INSTITUTION_NAME, LATEST_CODE_INDICATOR_YES,
							2000, 2000);

			assertTrue(list.isEmpty());
		}
	}

	@Test
	void shouldLoadStudentDetailsWithFirstThreeLettersOfFirstNameWithDOB() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			String forename = "foreName";
			String lastname = "lastname";
			Date dob = new Date(122121);
			List<StudentResultVO> list = subject.findByForenamesAndSurnameAndDobStud(forename, lastname, dob);

			verify(studRepo, times(1))
					.findByForenamesStartsWithAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							"FOR", lastname.toUpperCase(), dob, EXCLUDE_Z_NAMED_INSTITUTION_NAME, LATEST_CODE_INDICATOR_YES,
							2000, 2000);

			assertTrue(list.isEmpty());
		}
	}

	@Test
	void shouldLoadStudentDetailsWithGivenFirstNameWithOutDOB() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			String forename = "fo";
			String lastname = "lastname";
			List<StudentResultVO> list = subject.findByForenamesAndSurnameAndDobStud(forename, lastname, null);

			verify(studRepo, times(1))
					.findByForenamesAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.toUpperCase(), lastname.toUpperCase(), EXCLUDE_Z_NAMED_INSTITUTION_NAME,
							LATEST_CODE_INDICATOR_YES, 2000, 2000);

			assertTrue(list.isEmpty());
		}
	}

	@Test
	void shouldLoadStudentDetailsWithGivenFirstNameWithDOB() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			String forename = "fo";
			String lastname = "lastname";
			Date dob = new Date(122121);
			List<StudentResultVO> list = subject.findByForenamesAndSurnameAndDobStud(forename, lastname, dob);

			verify(studRepo, times(1))
					.findByForenamesAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.toUpperCase(), lastname.toUpperCase(), dob, EXCLUDE_Z_NAMED_INSTITUTION_NAME,
							LATEST_CODE_INDICATOR_YES, 2000, 2000);

			assertTrue(list.isEmpty());
		}
	}

	@Test
	void shouldLoadStudentDetailsWithGivenFirstNameAndDOB() {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			String forename = "fo";
			String lastname = "lastname";
			Date dob = new Date(122121);
			List<Stud> studDBList = new ArrayList<>();
			Stud stud1 = new Stud();
			Stud stud2 = new Stud();
			ArrayList<StudCourseYear> studCourseYear = new ArrayList<>();
			StudCourseYear courseYear = new StudCourseYear();
			courseYear.setSessionCode(2000);
			courseYear.setInstituteName("ABC");
			courseYear.setLatestCourseIndicator(LATEST_CODE_INDICATOR_YES);
			courseYear.setApplicationStatus("W");
			studCourseYear.add(courseYear);
			stud1.setStudCourseYear(studCourseYear);
			stud1.setForenames(forename);
			stud1.setSurname(lastname);
			stud1.setDob(dob);
			stud2.setDob(dob);
			stud2.setForenames(forename);
			stud2.setSurname(lastname);
			stud2.setStudCourseYear(new ArrayList<>());
			studDBList.add(stud1);
			studDBList.add(stud2);
			Mockito.when(studRepo
							.findByForenamesAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
									forename.toUpperCase(), lastname.toUpperCase(), dob, EXCLUDE_Z_NAMED_INSTITUTION_NAME,
									LATEST_CODE_INDICATOR_YES, 2000, 2000))
					.thenReturn(studDBList);
			List<StudentResultVO> listData = subject.findByForenamesAndSurnameAndDobStud(forename, lastname, dob);

			verify(studRepo, times(1))
					.findByForenamesAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.toUpperCase(), lastname.toUpperCase(), dob, EXCLUDE_Z_NAMED_INSTITUTION_NAME,
							LATEST_CODE_INDICATOR_YES, 2000, 2000);

			assertSame(1, listData.size());
		}
	}

	@Test
	void shouldPopulateParentInstituteNameCorrectlyFromGroupTable() {
		StudCourseYear studCourseYearData = new StudCourseYear();
		studCourseYearData.setInstituteName("ABC Inst (CD)");
		studCourseYearData.setInstCode("AB1");
		InstituteGrouping institutionGrouping = new InstituteGrouping();
		institutionGrouping.setInstCode(CURRENT_SESSION);
		institutionGrouping.setParentInstCode("AB");
		institutionGrouping.setParentInstDisplayName("ABC Inst");
		when(instituteGroupingRepository.findByInstCode(studCourseYearData.getInstCode()))
				.thenReturn(institutionGrouping);
		String parentInstName = subject.populateInstitutionName(studCourseYearData);
		assertEquals("ABC Inst", parentInstName);
	}

	@Test
	void shouldPopulateInstituteNameCorrectlyFromStudCourseYearTable() {
		StudCourseYear studCourseYearData = new StudCourseYear();
		studCourseYearData.setInstituteName("ABC Inst (CD)");
		studCourseYearData.setInstCode("AB1");

		when(instituteGroupingRepository.findByInstCode(studCourseYearData.getInstCode())).thenReturn(null);
		String parentInstName = subject.populateInstitutionName(studCourseYearData);
		assertEquals("ABC Inst (CD)", parentInstName);
	}

	@Test
	void shouldLoadStudentDetailsUsingStudentReferenceNo() throws IllegalAccessException {
		try (MockedStatic<ConfigDataService> configDataServiceMockedStatic = Mockito.mockStatic(ConfigDataService.class)) {
			configDataServiceMockedStatic.when(ConfigDataService::getCurrentActiveSession).thenReturn(2000);
			mockStudData();
			StudentResultVO studentResultVO = subject.findByStudReferenceNumber(STUDENT_REF_NO);

			verify(studRepo, times(1)).findByStudentReferenceNumber(STUDENT_REF_NO);
			assertEquals("Forename", studentResultVO.getFirstName());
		}
	}

	@Test
	void shouldThrowExceptionIfNoStudentFound() {
		IllegalAccessException thrown = Assertions.assertThrows(IllegalAccessException.class,
				() -> subject.findByStudReferenceNumber(STUDENT_REF_NO));
		Assertions.assertEquals("No Student found for studentReferenceNumber:1", thrown.getMessage());
		verify(studRepo, times(1)).findByStudentReferenceNumber(STUDENT_REF_NO);
	}

	private void mockStudData() {
		Stud stud = new Stud();
		ArrayList<StudCourseYear> studCourseYear = new ArrayList<>();
		StudCourseYear courseYear = new StudCourseYear();
		courseYear.setSessionCode(2000);
		courseYear.setLatestCourseIndicator("Y");
		courseYear.setInstituteName("ABC inst");
		courseYear.setApplicationStatus("W");
		studCourseYear.add(courseYear);
		stud.setStudCourseYear(studCourseYear);
		stud.setForenames("forename");
		Mockito.when(studRepo.findByStudentReferenceNumber(STUDENT_REF_NO)).thenReturn(stud);
	}
}