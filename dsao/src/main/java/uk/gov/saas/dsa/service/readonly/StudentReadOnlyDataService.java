package uk.gov.saas.dsa.service.readonly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.UserPersonalDetails;
import uk.gov.saas.dsa.domain.readonly.Learner;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.persistence.UserRepository;
import uk.gov.saas.dsa.persistence.readonly.LearnerRepository;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;

/**
 * To find the user details using the read-only databases Stud and Learner
 * materialised views.
 * 
 * @author Siva Chimpiri
 *
 */
@Service
public class StudentReadOnlyDataService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private StudRepository studRepo;
	private LearnerRepository learnerRepo;
	private UserRepository userRepo;

	@Autowired
	public StudentReadOnlyDataService(StudRepository studRepo, UserRepository userRepository, LearnerRepository learnerRepo) {
		this.studRepo = studRepo;
		this.userRepo = userRepository;
		this.learnerRepo = learnerRepo;
	}

	/**
	 * To load the Stud information for the user id
	 * 
	 * @param userId the user id
	 * @return STUD details from the materialised views
	 */
	public Stud getStudCheckStudentRefAlso(String userId) {
		logger.info("user id: {} ", userId);
		Stud stud = studRepo.findByWebUserId(userId);
		logger.info("Stud is: {}", stud);
		if (stud == null) {
			UserPersonalDetails user = userRepo.findByUserId(userId);
			logger.info("user is: {}", user);
			long studentRefNumber = user.getStudentPersonalDetails().getStudentRefNumber();
			logger.info("studentRefNumber is: {}", studentRefNumber);
			if (studentRefNumber != 0) {
				logger.info("Loding the stud using student reference number: {}", user);
				stud = studRepo.findById(studentRefNumber);
			}
		} 
		return stud;
	}

	/**
	 * To load the Learner information for the user id
	 * 
	 * @param userId the user id
	 * @return learner details from the materialised views
	 */
	public Learner getLearnerCheckLearnerIdAlso(String userId) {
		logger.info("user id: {} ", userId);
		Learner learner = learnerRepo.findByWebUserId(userId);
		if (learner == null) {
			UserPersonalDetails user = userRepo.findByUserId(userId);
			logger.info("user is: {}", user);
			String learnerId = user.getStudentPersonalDetails().getLearnerId();
			logger.info("learner id : {}", learnerId);
			if (learnerId != null) {
				learner = learnerRepo.findByLearnerId(learnerId);
			}
		}
		logger.info("Learner data is: {}", learner); 
		return learner;
	}

}
