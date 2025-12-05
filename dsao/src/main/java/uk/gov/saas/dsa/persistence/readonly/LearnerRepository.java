package uk.gov.saas.dsa.persistence.readonly;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.Learner;

@Repository
public interface LearnerRepository extends ReadOnlyRepository<Learner, Long> {
	Learner findByWebUserId(String webUserId);

	Learner findByLearnerId(String learnerId);

	List<Learner > findByForenameStartsWithAndSurnameStartsWithAndDobAndLearnerApplicationSessionYearBetweenAllIgnoreCase(

			String foreName, String surname, Date dob, String sessionStart, String sessionEnd);
}
