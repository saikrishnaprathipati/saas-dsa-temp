package uk.gov.saas.dsa.persistence.readonly;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.Stud;

@Repository("studRepository")
public interface StudRepository extends ReadOnlyRepository<Stud, Long> {
	Stud findByWebUserId(String webUserId);

	List<Stud> findByForenamesStartsWithAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(

			String partialForename, String surname, Date dob, String instituteName, String latestCourseIndicator,
			Integer sessionStart, Integer sessionEnd);

	List<Stud> findByForenamesAndSurnameAndDobAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(

			String partialForename, String surname, Date dob, String instituteName, String latestCourseIndicator,
			Integer sessionStart, Integer sessionEnd);

	List<Stud> findByForenamesStartsWithAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(

			String partialForename, String surname, String instituteName, String latestCourseIndicator,
			Integer sessionStart, Integer sessionEnd);

	List<Stud> findByForenamesAndSurnameAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(

			String partialForename, String surname, String instituteName, String latestCourseIndicator,
			Integer sessionStart, Integer sessionEnd);

	List<Stud> findByStudentReferenceNumberAndStudCourseYearInstituteNameNotLikeAndStudCourseYearLatestCourseIndicatorLikeAndStudCourseYearSessionCodeBetweenAllIgnoreCaseOrderByStudCourseYearSessionCodeDesc(
			long studRefno, String instituteName, String latestCourseIndicator, Integer sessionStart,
			Integer sessionEnd);

	/**
	 * @param studentReferenceNumber the student reference number
	 * @return STUD details
	 */
	Stud findByStudentReferenceNumber(long studentReferenceNumber);
}
