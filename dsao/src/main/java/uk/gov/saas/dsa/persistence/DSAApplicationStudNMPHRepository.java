package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationStudNMPH;

/**
 * DSA Application NMPH allowances Repository
 */
@Repository("applicationStudNMPHRepository")
public interface DSAApplicationStudNMPHRepository extends CrudRepository<DSAApplicationStudNMPH, Long> {
	/**
	 * To get the NMPH allowances by the application number
	 * 
	 * @param dsaApplicationNumber
	 * @return List of DSAApplicationStudNMPH allowances
	 */
	List<DSAApplicationStudNMPH> findByDsaApplicationNumber(long dsaApplicationNumber);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);

}