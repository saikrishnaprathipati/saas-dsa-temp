package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationStudAccommodation;
import uk.gov.saas.dsa.model.AccommodationType;

/**
 * DSA Application accommodation Repository
 */
@Repository("applicationStudAccommodationRepository")
public interface DSAApplicationAccommodationRepository extends CrudRepository<DSAApplicationStudAccommodation, Long> {

	List<DSAApplicationStudAccommodation> findByDsaApplicationNumber(long dsaApplicationNumber);

	DSAApplicationStudAccommodation findByDsaApplicationNumberAndAccommodationTypeAndEnhancedCostIsNullAndStandardCostIsNull(long dsaApplicationNumber,
			AccommodationType type);
	DSAApplicationStudAccommodation findByDsaApplicationNumberAndAccommodationType(long dsaApplicationNumber,
			AccommodationType type);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);

}