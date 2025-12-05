package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;

@Repository("dsaAdvisorAuthRepository")
public interface DsaAdvisorAuthRepository extends CrudRepository<DsaAdvisorAuthDetails, String> {

	DsaAdvisorAuthDetails findByEmailIgnoreCase(String email);
	
	DsaAdvisorAuthDetails findByActivationToken(String activationToken);
	
	DsaAdvisorAuthDetails findByPasswordResetToken(String passwordResetToken);
}
