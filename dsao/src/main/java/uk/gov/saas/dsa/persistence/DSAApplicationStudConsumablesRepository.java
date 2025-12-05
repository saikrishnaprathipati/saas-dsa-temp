package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationStudConsumables;

/**
 * DSA Application StudConsumables Repository
 */
@Repository("applicationStudConsumablesRepository")
public interface DSAApplicationStudConsumablesRepository extends CrudRepository<DSAApplicationStudConsumables, Long> {

	List<DSAApplicationStudConsumables> findByDsaApplicationNumber(long dsaApplicationNumber);

	void deleteByDsaApplicationNumber(long dsaApplicationNumber);

}