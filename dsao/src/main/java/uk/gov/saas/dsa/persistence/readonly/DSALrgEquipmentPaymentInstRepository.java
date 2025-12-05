package uk.gov.saas.dsa.persistence.readonly;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.DSALrgEquipmentPaymentInst;

@Repository("dsaLrgEquipmentPaymentInstRepository")
public interface DSALrgEquipmentPaymentInstRepository extends CrudRepository<DSALrgEquipmentPaymentInst, Long> {

	DSALrgEquipmentPaymentInst findByInstituteNameIgnoreCase(String instituteName);
}
