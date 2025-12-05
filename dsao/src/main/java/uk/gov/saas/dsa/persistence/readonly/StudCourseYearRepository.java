package uk.gov.saas.dsa.persistence.readonly;

import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.domain.readonly.StudSession;

@Repository("studCourseYearRepository")
public interface StudCourseYearRepository extends ReadOnlyRepository<StudCourseYear, Long> {
	StudCourseYear findByStudentReferenceNumberAndSessionCodeAndLatestCourseIndicator(long studentReferenceNumber, Integer sessionCode, String latestCourseIndicator);
}