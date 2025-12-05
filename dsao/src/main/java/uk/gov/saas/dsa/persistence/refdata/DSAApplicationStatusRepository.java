package uk.gov.saas.dsa.persistence.refdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.DSAApplicationStatus;

@Repository("dsaApplicationStatusRepository")
public interface DSAApplicationStatusRepository extends JpaRepository<DSAApplicationStatus, Long> {

}