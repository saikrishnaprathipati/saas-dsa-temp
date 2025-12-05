package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationSectionStatus;
import uk.gov.saas.dsa.model.Section;

/**
 * DSA application section status repository
 */
@Repository("dsaApplicationSectionStatusRepository")
public interface DSAApplicationSectionStatusRepository extends CrudRepository<DSAApplicationSectionStatus, Long> {

	/**
	 * Find by application number
	 * 
	 * @param dsaApplicationNumber
	 * @return list of DSA Application sections
	 */
	List<DSAApplicationSectionStatus> findByDsaApplicationNumber(long dsaApplicationNumber);
	
	DSAApplicationSectionStatus findByDsaApplicationNumberAndSectionCode(long dsaApplicationNumber, Section sectionCode);
	
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
}