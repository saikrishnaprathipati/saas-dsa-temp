package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAAppAdditionalInformation;
 
@Repository("dsaAdditionalInfoRepository")
public interface DSAAdditionalInfoRepository extends CrudRepository<DSAAppAdditionalInformation, Long> {

	DSAAppAdditionalInformation findByDsaApplicationNumber(long dsaApplicationNumber);

}