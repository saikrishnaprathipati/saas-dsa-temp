package uk.gov.saas.dsa.persistence.readonly;

import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.readonly.StudSession;

@Repository("studSessionRepository")
public interface StudSessionRepository extends ReadOnlyRepository<StudSession, Long> {
	StudSession findByStudentReferenceNumberAndSessionCode(long studentReferenceNumber, Integer sessionCode);
}