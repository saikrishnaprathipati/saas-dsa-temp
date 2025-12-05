package uk.gov.saas.dsa.persistence.readonly;

import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.DSAAwardNotice;

@Repository("dsaAwardNoticeRepository")
public interface DSAAwardNoticeRepository extends ReadOnlyRepository<DSAAwardNotice, Long> {

	DSAAwardNotice findByStudentReferenceNumberAndSessionCode(long studentReferenceNumber, int sessionCode);
}
