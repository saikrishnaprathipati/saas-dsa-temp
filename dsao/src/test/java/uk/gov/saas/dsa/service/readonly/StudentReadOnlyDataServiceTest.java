package uk.gov.saas.dsa.service.readonly;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.domain.UserPersonalDetails;
import uk.gov.saas.dsa.domain.readonly.Learner;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.persistence.UserRepository;
import uk.gov.saas.dsa.persistence.readonly.LearnerRepository;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;

@ExtendWith(SpringExtension.class)
class StudentReadOnlyDataServiceTest {
	private static final long STUD_REF_NUMBER_0 = 0;
	private static final long STUD_REF_NUMBER_1 = 1;
	private static final String LEARNER_ID = "learnerId";
	@MockitoBean
	private StudRepository studRepo;
	@MockitoBean
	private LearnerRepository learnerRepo;
	@MockitoBean
	private UserRepository userRepo;

	private static final String USER_ID = "UID_123456";

	private StudentReadOnlyDataService subject;

	@BeforeEach
	void setUp() throws Exception {
		subject = new StudentReadOnlyDataService(studRepo, userRepo, learnerRepo);
	}

	@Test
	void shouldGetStudDetailsUsingWebUserId() {
		Stud stud = new Stud();
		when(studRepo.findByWebUserId(USER_ID)).thenReturn(stud);
		Stud studResponse = subject.getStudCheckStudentRefAlso(USER_ID);
		assertSame(stud, studResponse);
		verify(studRepo, times(1)).findByWebUserId(USER_ID);
		verify(userRepo, times(0)).findById(USER_ID);
		verify(studRepo, times(0)).findById(STUD_REF_NUMBER_0);
	}

	@Test
	void shouldGetStudDetailsUsingUserPersonalDetails() {
		Stud stud = new Stud();
		mockUserPersonalDetails(LEARNER_ID, STUD_REF_NUMBER_1);

		when(studRepo.findById(STUD_REF_NUMBER_1)).thenReturn(stud);

		Stud studResponse = subject.getStudCheckStudentRefAlso(USER_ID);
		assertSame(stud, studResponse);
		verify(studRepo, times(1)).findByWebUserId(USER_ID);
		verify(userRepo, times(0)).findById(USER_ID);
		verify(studRepo, times(0)).findById(STUD_REF_NUMBER_0);
	}

 
	@Test
	void shouldGetLearnerDetailsUsingWebUserId() {
		Learner learner = new Learner();
		when(learnerRepo.findByWebUserId(USER_ID)).thenReturn(learner);
		Learner learnerResponse = subject.getLearnerCheckLearnerIdAlso(USER_ID);
		assertSame(learner, learnerResponse);
		verify(learnerRepo, times(1)).findByWebUserId(USER_ID);
		verify(userRepo, times(0)).findById(USER_ID);
		verify(learnerRepo, times(0)).findByLearnerId(LEARNER_ID);
	}

	@Test
	void shouldGetLearnerDetailsUsingUserPersonalDetails() {
		Learner learner = new Learner();
		mockUserPersonalDetails(LEARNER_ID, STUD_REF_NUMBER_0);

		when(learnerRepo.findByLearnerId(LEARNER_ID)).thenReturn(learner);
		Learner learnerResponse = subject.getLearnerCheckLearnerIdAlso(USER_ID);
		assertSame(learner, learnerResponse);
		verify(learnerRepo, times(1)).findByWebUserId(USER_ID);
		verify(userRepo, times(1)).findByUserId(USER_ID);
		verify(learnerRepo, times(1)).findByLearnerId(LEARNER_ID);
	}
 

	private void mockUserPersonalDetails(String learnerId, long studRefNumber) {
		UserPersonalDetails user = new UserPersonalDetails();
		StudentPersonalDetails spd = new StudentPersonalDetails();
		if (StringUtils.hasText(learnerId)) {
			spd.setLearnerId(learnerId);
		}
		if (studRefNumber != 0) {
			spd.setStudentRefNumber(studRefNumber);
		}

		user.setStudentPersonalDetails(spd);
		when(userRepo.findByUserId(USER_ID)).thenReturn(user);

	}
}
