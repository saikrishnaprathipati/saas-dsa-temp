package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationPDF;

/**
 * Application PDF repository
 */
@Repository("dsaApplicationPDFRepo")
public interface DSAApplicationPDFRepository extends CrudRepository<DSAApplicationPDF, Long> {
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
 
	DSAApplicationPDF findByStudentReferenceNumber(long studentReferenceNumber); 
}