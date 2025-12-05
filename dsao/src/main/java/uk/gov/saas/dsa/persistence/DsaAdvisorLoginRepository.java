package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;

@Repository("dsaAdvisorLoginRepository")
public interface DsaAdvisorLoginRepository extends CrudRepository<DsaAdvisorLoginDetails, String> {

	DsaAdvisorLoginDetails findByUserNameIgnoreCase(String email);
	
	DsaAdvisorLoginDetails findByUserId(String userId);
}
