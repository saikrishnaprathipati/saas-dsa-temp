package uk.gov.saas.dsa.persistence.readonly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSAApplicationComplete;
import uk.gov.saas.dsa.domain.readonly.Learner;

@Repository
public interface DSAApplicationCompleteRepository extends JpaRepository<DSAApplicationComplete, Long> {

	DSAApplicationComplete findByDsaApplicationNumber(long dsaApplicationNumber);
	void deleteByDsaApplicationNumber(long dsaApplicationNumber);
}
