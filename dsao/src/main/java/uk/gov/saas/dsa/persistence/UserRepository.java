package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.UserPersonalDetails;

@Repository("userRepo")
public interface UserRepository extends CrudRepository<UserPersonalDetails, String> {

	UserPersonalDetails findByEmailAddress(String emailAddress);

	UserPersonalDetails findByUserId(String userId);

	List<UserPersonalDetails> findByForenameLikeAndSurnameAllIgnoreCase(String partialForename, String surname);

}
