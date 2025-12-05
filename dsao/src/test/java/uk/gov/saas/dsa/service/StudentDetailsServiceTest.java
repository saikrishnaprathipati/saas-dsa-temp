package uk.gov.saas.dsa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.saas.dsa.vo.StudentResultVO;

@ExtendWith(SpringExtension.class)
class StudentDetailsServiceTest {
	@MockitoBean
	private FindStudentService findStudentService;
	private StudentDetailsService subject;
	private long REF_NUMBER = 77000218;
	private String forenames = "LIVING";
	private String surname = "WAGE";

	@BeforeEach
	public void setUp() {
		subject = new StudentDetailsService(findStudentService);
	}

	@Test
	public void testFindStudentDetailsFromDB() throws Exception {
		StudentResultVO mockStud = new StudentResultVO();
		mockStud.setStudentReferenceNumber(REF_NUMBER);
		mockStud.setFirstName(forenames);
		mockStud.setLastName(surname);
		mockStud.setDob("2004-09-22");
		when(findStudentService.findByStudReferenceNumber(REF_NUMBER)).thenReturn(mockStud);
		StudentResultVO resultVO = subject.findStudentDetailsFromDB(REF_NUMBER);

		assertEquals(REF_NUMBER, resultVO.getStudentReferenceNumber());
		assertEquals(forenames, resultVO.getFirstName());
		assertEquals(surname, resultVO.getLastName());
		assertEquals("2004-09-22", resultVO.getDob());
	}
}
