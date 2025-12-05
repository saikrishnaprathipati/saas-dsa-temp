package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class CourseDetailsServiceTest {
	private static final String INSTITUTION_NAME = "Institution Name";
	private static final String COURSENAME = "coursename";
	private static final String ACADEMIC_YEAR = "academic year";
	private static final long STUDENT_REFERENCE_NUMBER = 222L;
	CourseDetailsService subject;
	@MockitoBean
	private FindStudentService findStudentService;

	@BeforeEach
	public void setUp() throws Exception {
		subject = new CourseDetailsService(findStudentService);

	}

	@Test
	void shouldLoadSessionConfigDataCorrectly() throws IllegalAccessException {

		StudentResultVO studResult = new StudentResultVO();
		StudentCourseYearVO courseYear = new StudentCourseYearVO();
		courseYear.setAcademicYear(ACADEMIC_YEAR);
		courseYear.setAcademicYearFull(ACADEMIC_YEAR);
		courseYear.setCourseName(COURSENAME);
		courseYear.setInstitutionName(INSTITUTION_NAME);
		courseYear.setSessionCode(2122);
		studResult.setStudentCourseYear(courseYear);

		Mockito.when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studResult);
		CourseDetailsVO courseDetailsVO = subject.findCourseDetailsFromDB(STUDENT_REFERENCE_NUMBER);
		assertEquals(ACADEMIC_YEAR, courseDetailsVO.getAcademicYear());
		assertEquals(COURSENAME, courseDetailsVO.getCourseName());
		assertEquals(INSTITUTION_NAME, courseDetailsVO.getInstitutionName());

	}
}
