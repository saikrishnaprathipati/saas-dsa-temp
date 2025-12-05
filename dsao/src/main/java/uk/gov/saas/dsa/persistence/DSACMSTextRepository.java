package uk.gov.saas.dsa.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DSACMSText;

@Repository
public interface DSACMSTextRepository extends JpaRepository<DSACMSText, String> {

	public DSACMSText findByIdentifier(String identifier);
}
