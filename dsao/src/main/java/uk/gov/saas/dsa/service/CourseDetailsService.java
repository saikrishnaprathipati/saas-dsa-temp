package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

import static uk.gov.saas.dsa.service.ServiceUtil.capitalizeFully;

/**
 * The course details service
 */
@Service
public class CourseDetailsService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final FindStudentService findStudentService;

	public CourseDetailsService(FindStudentService findStudentService) {
		this.findStudentService = findStudentService;
	}

	/**
	 * Find course details by student reference number,
	 * session code optional
	 */
	public CourseDetailsVO findCourseDetailsFromDB(long studentReferenceNumber, Integer... sessionCode) throws IllegalAccessException {
		logger.info("findCourseDetailsFromDB student reference number {}, sessionCode {}", studentReferenceNumber, sessionCode);
		StudentResultVO student;
		if (sessionCode.length > 0) {
			student = findStudentService.findByStudReferenceNumber(studentReferenceNumber, sessionCode[0]);
		} else {
			student = findStudentService.findByStudReferenceNumber(studentReferenceNumber);
		}

 
		return setCourseDetails(student);
	}
	
	private CourseDetailsVO setCourseDetails(StudentResultVO student) {
		CourseDetailsVO courseDetailsVO = new CourseDetailsVO();
		String institutionName = student.getStudentCourseYear().getInstitutionName();
		courseDetailsVO.setInstitutionName(capitalizeFully(institutionName));
		courseDetailsVO.setCourseName(student.getStudentCourseYear().getCourseName());
		courseDetailsVO.setAcademicYear(student.getStudentCourseYear().getAcademicYearFull());
		courseDetailsVO.setAcademicYearFull(student.getStudentCourseYear().getAcademicYearFull());
		return courseDetailsVO;
	}
	public CourseDetailsVO findCourseDetailsByStudRefAndSessionCode(long studentReferenceNumber, Integer sessionCode)
			throws IllegalAccessException {
		logger.info("findCourseDetailsByStudRefAndSessionCode - student reference number {} and session code {}", studentReferenceNumber, sessionCode);
		StudentResultVO student = findStudentService.findByStudReferenceNumber(studentReferenceNumber, sessionCode);
		return setCourseDetails(student);
	}
	
}
