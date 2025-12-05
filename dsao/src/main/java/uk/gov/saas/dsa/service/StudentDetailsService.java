package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.vo.StudentResultVO;

/**
 * Find Student Details Service
 */
@Service
public class StudentDetailsService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final FindStudentService findStudentService;

	public StudentDetailsService(FindStudentService findStudentService) {
		this.findStudentService = findStudentService;
	}

	/**
	 * @param studentReferenceNumber Student Reference Number
	 * @return StudentResultVO for HTML view
	 * @throws Exception For null student
	 */
	public StudentResultVO findStudentDetailsFromDB(long studentReferenceNumber) throws Exception {
		logger.info("findStudentDetailsFromDB studentReferenceNumber :{}", studentReferenceNumber);
	 
		StudentResultVO stud = findStudentService.findByStudReferenceNumber(studentReferenceNumber);

		return populateStudentResultVO(stud);
	}

	public StudentResultVO findStudentByStudRefAndSessionCode(long studentReferenceNumber, int session) throws Exception {
		logger.info("findStudentByStudRefAndSessionCode studentReferenceNumber :{}, session code: {} ", studentReferenceNumber, session);
	 
		StudentResultVO stud = findStudentService.findByStudReferenceNumber(studentReferenceNumber, session);

		return populateStudentResultVO(stud);
	}
	/**
	 * @param stud STUD details
	 * @return StudentResultVO for HTML view
	 */
	private StudentResultVO populateStudentResultVO(StudentResultVO stud) {
		StudentResultVO studentResultVO = new StudentResultVO();
		studentResultVO.setStudentReferenceNumber(stud.getStudentReferenceNumber());
		studentResultVO.setFirstName(stud.getFirstName());
		studentResultVO.setLastName(stud.getLastName());
		studentResultVO.setDob(stud.getDob());
		studentResultVO.setFundingEligibilityStatus(stud.getFundingEligibilityStatus());
		logger.info("studentResultVO :{}", studentResultVO);
		return studentResultVO;
	}
}
