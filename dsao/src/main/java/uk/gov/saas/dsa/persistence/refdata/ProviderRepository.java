package uk.gov.saas.dsa.persistence.refdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.refdata.Provider;

@Repository("providerRepo")
public interface ProviderRepository extends JpaRepository<Provider, Long> {

}