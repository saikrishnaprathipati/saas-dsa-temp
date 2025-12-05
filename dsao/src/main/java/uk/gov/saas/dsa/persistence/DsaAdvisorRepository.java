package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DsaAdvisor;

@Repository("dsaAdvisorRepository")
public interface DsaAdvisorRepository extends CrudRepository<DsaAdvisor, String> {

	DsaAdvisor findByEmailIgnoreCase(String email);

}
