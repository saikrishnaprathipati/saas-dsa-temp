package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.UserAuthDetails;

@Repository("userAuthDetailsRepo")
public interface UserAuthDetailsRepository extends CrudRepository<UserAuthDetails, String> {

    UserAuthDetails findByUserId(String userId);
    
    UserAuthDetails findByLastLoginToken(String lastLoginToken);



}
