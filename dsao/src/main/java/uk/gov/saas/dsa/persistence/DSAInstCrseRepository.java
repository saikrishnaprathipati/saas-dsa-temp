package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;

import uk.gov.saas.dsa.domain.DSAInstCrse;

public interface DSAInstCrseRepository extends CrudRepository<DSAInstCrse, Long>{
	
	DSAInstCrse findByApplicationId(Long applicationId);

}
