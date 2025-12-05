package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DisabilityType;

/**
 * DisabilityTypeRepository
 */
@Repository("disabilitiesRepository")
public interface DisabilityTypeRepository extends CrudRepository<DisabilityType, Long> {
 
	List<DisabilityType> findAllByDisabilityTypeCodeIn(List<String> disabilityCodes);
	
	 DisabilityType  findByDisabilityTypeCode(String disabilityCode);
	
	List<DisabilityType> findByIsActiveIgnoreCase(String isActive);

}