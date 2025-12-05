package uk.gov.saas.dsa.persistence;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.model.OverallApplicationStatus;

/**
 * DSAApplicationsMadeRepository
 */
@Repository("dsaApplicationsMadeRepo")
public interface DSAApplicationsMadeRepository extends CrudRepository<DSAApplicationsMade, Long> {
	/**
	 * Find by DSA application number and student reference number
	 * 
	 * @param dsaApplicationNumber
	 * @param studentReferenceNumber
	 * @return DSA application
	 */
	DSAApplicationsMade findByDsaApplicationNumberAndStudentReferenceNumber(long dsaApplicationNumber,
			long studentReferenceNumber);
			
	
	DSAApplicationsMade findByStudentReferenceNumberAndSessionCode(long studentReferenceNumber, int sessionCode);
	
	/**
	 * Find by student reference number
	 * 
	 * @param studentReferenceNumber
	 * @return list of applications
	 */
	List<DSAApplicationsMade> findByStudentReferenceNumber(long studentReferenceNumber);

	@Query(value = "select * from (select * from DSA_APPLICATIONS_MADE a where a.STUD_REF_NO = :studentReferenceNumber order by a.LAST_UPDATED_DATE DESC)   WHERE ROWNUM = 1", nativeQuery = true)
	DSAApplicationsMade findLatestDSAApplciationByStudentRefNumber(
			@Param("studentReferenceNumber") long studentReferenceNumber);


	@Query(value = "select * from (select * from DSA_APPLICATIONS_MADE a where a.STUD_REF_NO = :studentReferenceNumber AND a.SESSION_CODE = :sessionCode order by a.LAST_UPDATED_DATE DESC)   WHERE ROWNUM = 1", nativeQuery = true)
	DSAApplicationsMade findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(@Param("studentReferenceNumber") long studentReferenceNumber,
																							@Param("sessionCode") int sessionCode);

	@Query(value = "select * from DSA_APPLICATIONS_MADE am where  AM.SESSION_CODE = :currentSessionCode and (AM.APP_STATUS = 'STARTED' or AM.APP_STATUS = 'WITHDRAWN' ) and AM.APP_SUMMARY_STATUS = 'APPLICATION_INCOMPLETE'	and AM.DSA_APPLICATION_NO in (select DSA_APPLICATION_NO from DSA_APPLICATION_SECTION_STATUS sec where   SEC.SECTION_CODE = 'ADVISOR_DECLARATION' and SEC.SECTION_STATUS = 'COMPLETED')", nativeQuery = true)
	@Transactional
	List<DSAApplicationsMade> findAllAdvisorDeclarationCompletedAndNotSubmittedByStudent(
			@Param("currentSessionCode") long currentSessionCode);
	
	List<DSAApplicationsMade> findByOverallApplicationStatusAndSessionCode(OverallApplicationStatus status, int sessionCode);
}