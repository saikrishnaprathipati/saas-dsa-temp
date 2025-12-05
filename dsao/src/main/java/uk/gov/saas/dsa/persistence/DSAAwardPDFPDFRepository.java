package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAAwardPDF;

/**
 * Award PDF repository
 */
@Repository("dsaAwardPDFRepo")
public interface DSAAwardPDFPDFRepository extends CrudRepository<DSAAwardPDF, Long> {

	DSAAwardPDF findByStudentReferenceNumberAndSessionCode(long studentReferenceNumber, int sessionCode);
}