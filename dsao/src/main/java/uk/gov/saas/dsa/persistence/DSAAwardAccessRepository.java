package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAAwardAccess;

/**
 * Bank account repo
 */
@Repository("dsaAwardAccessRepository")
public interface DSAAwardAccessRepository extends CrudRepository<DSAAwardAccess, Long> {

	DSAAwardAccess findByDsaApplicationNumber(long dsaApplicationNumber);

}