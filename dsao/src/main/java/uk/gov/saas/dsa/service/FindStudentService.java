package uk.gov.saas.dsa.service;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.saas.dsa.service.ServiceUtil.capitalizeFully;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.InstituteGrouping;
import uk.gov.saas.dsa.domain.StudentPersonalDetails;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.domain.readonly.StudSession;
import uk.gov.saas.dsa.domain.refdata.Institution;
import uk.gov.saas.dsa.model.FundingEligibilityStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationsMadeRepository;
import uk.gov.saas.dsa.persistence.InstituteGroupingRepository;
import uk.gov.saas.dsa.persistence.StudentPersonalDetailsRepository;
import uk.gov.saas.dsa.persistence.readonly.StudCourseYearRepository;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;
import uk.gov.saas.dsa.persistence.readonly.StudSessionRepository;
import uk.gov.saas.dsa.persistence.refdata.InstitutionRepository;
import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

/**
 * Find student service
 */
@Service
public class FindStudentService {
	private static final String EXCLUDE_Z_NAMED_INSTITUTION_NAME = "Z-%";
	private static final String SESSION_TO_FETCH_TO = "SESSION_TO_FETCH_TO";
	private static final String SESSION_TO_FETCH_FROM = "SESSION_TO_FETCH_FROM";
	public static final String LATEST_CODE_INDICATOR_YES = "Y";
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final StudRepository studRepo;
	private final InstituteGroupingRepository instituteGroupingRepository;
	private final InstitutionRepository institutionRepository;
	private final StudentPersonalDetailsRepository studentPersonalDetailsRepository;
	private final StudSessionRepository studSessionRepository;
	private final StudCourseYearRepository studCourseYearRepository;
	private final DSAApplicationsMadeRepository dsaApplicationsMadeRepository;

	@Value("${dsa.findstudent.include.years:0}")
	private int numberOfYears;

	@Autowired
	public FindStudentService(StudRepository studRepo, InstituteGroupingRepository instituteGroupingRepository,
			InstitutionRepository institutionRepository,
			StudentPersonalDetailsRepository studentPersonalDetailsRepository,
			StudSessionRepository studSessionRepository, StudCourseYearRepository studCourseYearRepository,
			DSAApplicationsMadeRepository dsaApplicationsMadeRepository) {

		this.studRepo = studRepo;
		this.instituteGroupingRepository = instituteGroupingRepository;
		this.institutionRepository = institutionRepository;
		this.studentPersonalDetailsRepository = studentPersonalDetailsRepository;
		this.studSessionRepository = studSessionRepository;
		this.studCourseYearRepository = studCourseYearRepository;
		this.dsaApplicationsMadeRepository = dsaApplicationsMadeRepository;
	}

	/**
	 * Find student by student reference number
	 *
	 * @param studentReferenceNumber Student Reference Number
	 * @return Student results
	 * @throws IllegalAccessException Illegal Exception
	 */
	public StudentResultVO findByStudReferenceNumber(long studentReferenceNumber, int... sessionCode)
			throws IllegalAccessException {
		logger.info("finding students for studentReferenceNumber: {}, session code {}", studentReferenceNumber,
				sessionCode);
		StudentResultVO resultsVO = new StudentResultVO();
		Stud stud = findStud(studentReferenceNumber);

		if (stud != null) {
			Optional<StudCourseYear> studCourseYear;
			if (sessionCode.length > 0) {
				studCourseYear = filterStudCourseYearPrevious(sessionCode[0], stud);
			} else {
				studCourseYear = filterStudCourseYear(sessionsToFetch(), stud);
			}

			if (studCourseYear.isPresent()) {
				resultsVO = populateStudentResultVO(stud, studCourseYear);
			}
		} else {
			throw new IllegalAccessException("No Student found for studentReferenceNumber:" + studentReferenceNumber);
		}
		return resultsVO;
	}

	public Stud findStud(long studentReferenceNumber) {
		return studRepo.findByStudentReferenceNumber(studentReferenceNumber);
	}

	public List<StudentResultVO> findStudentWithStudRefNo(long studRef) {
		Map<String, Integer> sessionsToFetch = sessionsToFetch();
		List<Stud> dbResults = searchWithStudRefNo(studRef, sessionsToFetch);
		return filterStudents(sessionsToFetch, dbResults);
	}

	/**
	 * Find student
	 *
	 * @param forename First Name
	 * @param lastname Last Name
	 * @param dob      Date of Birth
	 * @return Student results list
	 */
	public List<StudentResultVO> findByForenamesAndSurnameAndDobStud(String forename, String lastname, Date dob) {
		logger.info("finding students with forename: {},  lastname {}, dob: {} ", forename, lastname, dob);

		Map<String, Integer> sessionsToFetch = sessionsToFetch();

		return findStudentsFromStudTable(forename, lastname, dob, sessionsToFetch);
	}

	public Institution findInstitutionByInstCode(String institutionCode) {
		return institutionRepository.findByInstCode(institutionCode);
	}

	public StudentPersonalDetails findStudentPersonDetailsStudByRefNumber(long studentRefNumber) {
		return studentPersonalDetailsRepository.findByStudentRefNumber(studentRefNumber);
	}

	public StudSession findStudSessionByRefNumberAndSessionCode(long studentRefNumber, Integer sessionCode) {
		return studSessionRepository.findByStudentReferenceNumberAndSessionCode(studentRefNumber, sessionCode);
	}

	public StudCourseYear findStudCourseYearByStudentReferenceNumberAndSessionCode(long studentRefNumber,
			Integer sessionCode) {
		return studCourseYearRepository.findByStudentReferenceNumberAndSessionCodeAndLatestCourseIndicator(
				studentRefNumber, sessionCode, FindStudentService.LATEST_CODE_INDICATOR_YES);
	}

	private List<StudentResultVO> findStudentsFromStudTable(String forename, String lastname, Date dob,
			Map<String, Integer> sessionsToFetch) {

		List<Stud> dbResults = isNotEmpty(dob) ? searchWithDOB(forename, lastname, dob, sessionsToFetch)
				: searchWithOutDOB(forename, lastname, sessionsToFetch);

		return filterStudents(sessionsToFetch, dbResults);
	}

	private List<StudentResultVO> filterStudents(Map<String, Integer> sessionsToFetch, List<Stud> dbResults) {
		List<StudentResultVO> studentResultsVo = new ArrayList<>();
		dbResults = dbResults.stream().filter(distinctByKey(Stud::getStudentReferenceNumber))
				.collect(Collectors.toList());

		logger.info("Student results in DB: {}", dbResults);

		for (Stud stud : dbResults) {

			Optional<StudCourseYear> studCourseYear = filterStudCourseYear(sessionsToFetch, stud);
			if (studCourseYear.isPresent()) {

				StudentResultVO studentResultVO = populateStudentResultVO(stud, studCourseYear);

				studentResultsVo.add(studentResultVO);
			}
		}
		logger.info("found {} results from STUD table: {} ", studentResultsVo.size(), studentResultsVo);
		return studentResultsVo;
	}

	private StudentResultVO populateStudentResultVO(Stud stud, Optional<StudCourseYear> studCourseYear) {
		StudCourseYear studCourseYearData = studCourseYear.get();
		return populateStudentResultsVO(stud, studCourseYearData);
	}

	private Optional<StudCourseYear> filterStudCourseYear(Map<String, Integer> sessionsToFetch, Stud stud) {
		List<StudCourseYear> studCourseYears = stud.getStudCourseYear();
		Stream<StudCourseYear> filter = studCourseYears.stream().filter(studCrsYear -> studCrsYear
				.getLatestCourseIndicator().equalsIgnoreCase(LATEST_CODE_INDICATOR_YES)
				&& (studCrsYear.getSessionCode().intValue() == sessionsToFetch.get(SESSION_TO_FETCH_TO).intValue()
						|| studCrsYear.getSessionCode().intValue() == sessionsToFetch.get(SESSION_TO_FETCH_FROM)
								.intValue())
				&& !studCrsYear.getInstituteName().startsWith("Z-"));
		List<StudCourseYear> studCrseList = filter.collect(Collectors.toList());

		logger.info("studCrseList {}", studCrseList);
 
		List<StudCourseYear> sortedByYear = studCrseList.stream()
				.sorted(Comparator.comparingInt(StudCourseYear::getSessionCode).reversed())
				.collect(Collectors.toList());
		logger.info("sortedByYear {}", sortedByYear);

 
		Optional<StudCourseYear> firstStudCrse = sortedByYear.stream().distinct().findFirst();
		logger.info("First Stud {}", firstStudCrse);
		return firstStudCrse;
	}

	private Optional<StudCourseYear> filterStudCourseYearPrevious(int previousSession, Stud stud) {
		List<StudCourseYear> studCourseYears = stud.getStudCourseYear();
		Optional<StudCourseYear> first = studCourseYears.stream()
				.filter(studCrsYear -> studCrsYear.getLatestCourseIndicator()
						.equalsIgnoreCase(LATEST_CODE_INDICATOR_YES) && studCrsYear.getSessionCode() == previousSession
						&& !studCrsYear.getInstituteName().startsWith("Z-"))
				.distinct().findFirst();
		logger.info("first {}", first);
		return first;
	}

	String populateInstitutionName(StudCourseYear studCourseYearData) {
		String institutionName = studCourseYearData.getInstituteName();
		InstituteGrouping institutionGrouping = instituteGroupingRepository
				.findByInstCode(studCourseYearData.getInstCode());
		logger.info("Found the parent institution group: {}", institutionGrouping);
		if (institutionGrouping != null) {
			institutionName = institutionGrouping.getParentInstDisplayName();
		}
		return institutionName;
	}

	private List<Stud> searchWithStudRefNo(long studRefNo, Map<String, Integer> sessionsToFetch) {
		return studRepo
				.findByStudentReferenceNumberAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
						studRefNo, EXCLUDE_Z_NAMED_INSTITUTION_NAME, LATEST_CODE_INDICATOR_YES,
						sessionsToFetch.get(SESSION_TO_FETCH_FROM), sessionsToFetch.get(SESSION_TO_FETCH_TO));
	}

	private List<Stud> searchWithOutDOB(String forename, String lastname, Map<String, Integer> sessionsToFetch) {
		List<Stud> dbResults;
		if (forename.length() <= 3) {
			dbResults = studRepo
					.findByForenamesAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.toUpperCase(), lastname.toUpperCase(), EXCLUDE_Z_NAMED_INSTITUTION_NAME,
							LATEST_CODE_INDICATOR_YES, sessionsToFetch.get(SESSION_TO_FETCH_FROM),
							sessionsToFetch.get(SESSION_TO_FETCH_TO));
		} else {
			dbResults = studRepo
					.findByForenamesStartsWithAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.substring(0, 3).toUpperCase(), lastname.toUpperCase(),
							EXCLUDE_Z_NAMED_INSTITUTION_NAME, LATEST_CODE_INDICATOR_YES,
							sessionsToFetch.get(SESSION_TO_FETCH_FROM), sessionsToFetch.get(SESSION_TO_FETCH_TO));
		}
		return dbResults;
	}

	private List<Stud> searchWithDOB(String forename, String lastname, Date dob, Map<String, Integer> sessionsToFetch) {
		List<Stud> dbResults;
		if (forename.length() <= 3) {
			dbResults = studRepo
					.findByForenamesAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.toUpperCase(), lastname.toUpperCase(), dob, EXCLUDE_Z_NAMED_INSTITUTION_NAME,
							LATEST_CODE_INDICATOR_YES, sessionsToFetch.get(SESSION_TO_FETCH_FROM),
							sessionsToFetch.get(SESSION_TO_FETCH_TO));
		} else {
			dbResults = studRepo
					.findByForenamesStartsWithAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
							forename.substring(0, 3).toUpperCase(), lastname.toUpperCase(), dob,
							EXCLUDE_Z_NAMED_INSTITUTION_NAME, LATEST_CODE_INDICATOR_YES,
							sessionsToFetch.get(SESSION_TO_FETCH_FROM), sessionsToFetch.get(SESSION_TO_FETCH_TO));
		}
		return dbResults;
	}

	Map<String, Integer> sessionsToFetch() {
		Map<String, Integer> sessionFetchMap = new HashMap<>();
		int currentSessionYear = ConfigDataService.getCurrentActiveSession();
		sessionFetchMap.put(SESSION_TO_FETCH_FROM, currentSessionYear - numberOfYears);
		sessionFetchMap.put(SESSION_TO_FETCH_TO, currentSessionYear);
		logger.info("Sessions to fetch: {}", sessionFetchMap);
		return sessionFetchMap;
	}

	private StudentResultVO populateStudentResultsVO(Stud stud, StudCourseYear studCourseYearData) {

		StudentResultVO resultVO = new StudentResultVO();
		resultVO.setStudentReferenceNumber(stud.getStudentReferenceNumber());
		resultVO.setFirstName(capitalizeFully(stud.getForenames()));
		resultVO.setLastName(capitalizeFully(stud.getSurname()));
		resultVO.setDob(formatDate(stud.getDob()));
		resultVO.setEmailAddress(stud.getEmailAddress());

		// Set Student Course Year VO
		StudentCourseYearVO studentCourseYear = new StudentCourseYearVO();
		String institutionName = populateInstitutionName(studCourseYearData);
		studentCourseYear.setInstitutionName(capitalizeFully(institutionName));
		setAcademicYear(studCourseYearData, studentCourseYear);
		studentCourseYear.setCourseName(capitalizeFully(studCourseYearData.getCourseName()));
		resultVO.setStudentCourseYear(studentCourseYear);

		// Set Application the Date Updated
		List<DSAApplicationsMade> applicationsMadeList = dsaApplicationsMadeRepository
				.findByStudentReferenceNumber(stud.getStudentReferenceNumber());
		Optional<DSAApplicationsMade> optionalDSAApplicationsMade = applicationsMadeList.stream().findFirst();
		if (optionalDSAApplicationsMade.isPresent()) {
			DSAApplicationsMade applicationsMade = optionalDSAApplicationsMade.get();
			resultVO.setApplicationUpdated(
					new SimpleDateFormat("dd MMMM yyyy").format(applicationsMade.getLastUpdatedDate()));
		}

		resultVO.setFundingEligibilityStatus(fundingEligibilityStatus(studCourseYearData));

		resultVO.setAccountNumber(stud.getAccountNumber());
		resultVO.setSortCode(stud.getSortCode());

		return resultVO;
	}

	private void setAcademicYear(StudCourseYear studCourseYearData, StudentCourseYearVO studentCourseYear) {
		Integer sessionCode = studCourseYearData.getSessionCode();
		String nextSessionCode = Integer.valueOf(studCourseYearData.getSessionCode() + 1).toString();
		String nextSession = nextSessionCode.length() > 2 ? nextSessionCode.substring(nextSessionCode.length() - 2)
				: nextSessionCode;

		studentCourseYear.setSessionCode(sessionCode);

		studentCourseYear.setAcademicYear(sessionCode + " to " + nextSession);
		if (nextSessionCode.length() > 2) {
			studentCourseYear.setAcademicYearFull(sessionCode + " to " + nextSessionCode);
		} else {
			studentCourseYear.setAcademicYearFull(sessionCode + " to " + nextSession);
		}
	}

	private FundingEligibilityStatus fundingEligibilityStatus(StudCourseYear studCourseYearData) {

		FundingEligibilityStatus status;

		String applicationStatus = studCourseYearData.getApplicationStatus().toUpperCase();

		switch (applicationStatus) {
		case "C":
			status = FundingEligibilityStatus.CONFIRMED;
			break;
		case "R":
			status = FundingEligibilityStatus.REJECTED;
			break;
		case "N":
		case "A":
		case "T":
		case "W":
			status = FundingEligibilityStatus.PENDING;
			break;
		default:
			status = FundingEligibilityStatus.UNKNOWN;
			break;
		}

		return status;
	}

	private static String formatDate(Date date) {
		return (date == null) ? "" : new SimpleDateFormat("dd MMMM yyyy").format(date);
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}