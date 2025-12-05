package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationStudEquipment;

import java.util.List;

@Repository("applicationStudEquipmentRepository")
public interface DSAApplicationStudEquipmentRepository extends CrudRepository<DSAApplicationStudEquipment, Long> {

    /**
     * To get the Equipment allowances by the application number
     *
     * @param dsaApplicationNumber Application Number
     * @return List of DSAApplicationStudEquipment allowances
     */
    List<DSAApplicationStudEquipment> findByDsaApplicationNumber(long dsaApplicationNumber);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
}
