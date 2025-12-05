package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSALargeEquipemntPaymentFor;

@Repository("dsaLargeEquipemntPaymentForRepository")
public interface DSALargeEquipemntPaymentForRepository extends CrudRepository<DSALargeEquipemntPaymentFor, Long> {

	DSALargeEquipemntPaymentFor findQuotesByDsaApplicationNumber(long applicationNumber);

}
