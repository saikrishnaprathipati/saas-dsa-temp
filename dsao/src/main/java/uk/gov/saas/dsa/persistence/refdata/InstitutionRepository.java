package uk.gov.saas.dsa.persistence.refdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.refdata.Institution;

@Repository("institutionRepo")
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
	Institution findByInstCode(String instCode);
}