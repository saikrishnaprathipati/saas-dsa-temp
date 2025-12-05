package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.StudentPersonalDetails;

@Repository("studentPersonalDetailsRepo")
public interface StudentPersonalDetailsRepository extends CrudRepository<StudentPersonalDetails, String> {

	StudentPersonalDetails findByStudentRefNumber( long studentRefNumber);

	StudentPersonalDetails findByLearnerId(String learnerId);
	
	StudentPersonalDetails findByUserId(String userId);
	
		
}
