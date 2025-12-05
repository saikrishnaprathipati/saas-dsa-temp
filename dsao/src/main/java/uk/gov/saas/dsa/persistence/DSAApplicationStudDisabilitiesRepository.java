package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.DSAApplicationStudDisabilities;

import java.util.List;

/**
 * DSAApplicationsMadeRepository
 */
@Repository("applicationStudDisabilitiesRepository")
public interface DSAApplicationStudDisabilitiesRepository extends CrudRepository<DSAApplicationStudDisabilities, Long> {

	List<DSAApplicationStudDisabilities> findByDsaApplicationNumber(long dsaApplicationNumber);

	List<DSAApplicationStudDisabilities> findByDsaApplicationNumberAndStudentReferenceNumber(long dsaApplicationNumber, long studentReferenceNumber);

	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
}
