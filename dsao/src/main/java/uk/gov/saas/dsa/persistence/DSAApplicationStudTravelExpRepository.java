package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationTravelExp;
import uk.gov.saas.dsa.model.TravelExpType;

/**
 * DSA Application Travel Exp allowances Repository
 */
@Repository("applicationStudTravelExpRepository")
public interface DSAApplicationStudTravelExpRepository extends CrudRepository<DSAApplicationTravelExp, Long> {

	List<DSAApplicationTravelExp> findByDsaApplicationNumber(long dsaApplicationNumber);

	DSAApplicationTravelExp findByTravelExpType(TravelExpType travelExpType);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
	
}