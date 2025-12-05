package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationTravelExp;
import uk.gov.saas.dsa.domain.DSAApplicationTravelProvider;

/**
 * DSA Application Travel Provider Repository
 */
@Repository("applicationStudTravelProviderRepository")
public interface DSAApplicationStudTravelProviderRepository extends CrudRepository<DSAApplicationTravelProvider, Long> {

	void deleteByTravelExpIn(List<DSAApplicationTravelExp> d);
}