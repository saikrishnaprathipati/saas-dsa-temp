package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.DsaStudentAuthDetails;
import uk.gov.saas.dsa.domain.UserAuthDetails;

@Repository("dsaStudentAuthDetailsRepo")
public interface DsaStudentAuthDetailsRepository extends CrudRepository<DsaStudentAuthDetails, String> {

    DsaStudentAuthDetails findBySuid(String suid);

    DsaStudentAuthDetails findByStudRefNumber(Long studRefNumber);
}
