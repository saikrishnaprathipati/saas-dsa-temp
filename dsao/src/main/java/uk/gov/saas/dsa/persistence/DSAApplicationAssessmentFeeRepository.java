package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationAssessmentFee;

/**
 * DSA Application Assessment Fee Repository
 */
@Repository("applicationAssessmentFeeRepository")
public interface DSAApplicationAssessmentFeeRepository extends CrudRepository<DSAApplicationAssessmentFee, Long> {

	List<DSAApplicationAssessmentFee> findByDsaApplicationNumber(long dsaApplicationNumber);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
}