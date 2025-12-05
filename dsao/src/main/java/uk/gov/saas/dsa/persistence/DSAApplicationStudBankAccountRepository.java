package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationBankAccount;

/**
 * Bank account repo
 */
@Repository("applicationBankAccountRepository")
public interface DSAApplicationStudBankAccountRepository extends CrudRepository<DSAApplicationBankAccount, Long> {

	DSAApplicationBankAccount findByDsaApplicationNumber(long dsaApplicationNumber);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
	
}