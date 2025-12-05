package uk.gov.saas.dsa.vo;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.saas.dsa.domain.DSAInstCrse;
import uk.gov.saas.dsa.domain.refdata.QualificationType;

public class CourseDetailsFormVOMapper {

	protected final Logger logger = LogManager.getLogger(this.getClass());
	
	private static final String DATE_SEPERATOR = "/";
	
	public static CourseDetailsFormVO emptyForm() {
		return new CourseDetailsFormVO();
		
	}
	
	/**
	 * Map the entity to the form
	 * @param dsaInstCrseEntity the {@link DSAInstCrse}
	 * @param formVO the {@link CourseDetailsFormVO}
	 */
	public static void mapDSAInstCrseEntityToCourseDetailsFormVO(DSAInstCrse dsaInstCrseEntity,
			CourseDetailsFormVO formVO) {
		
		formVO.setCourseName(dsaInstCrseEntity.getCourseName());
		String startDate = dsaInstCrseEntity.getDsaCourseStartDate();
		if (!Objects.equals(startDate, null)) {
			formVO.setStartMonth(startDate.substring(0, 2));
			formVO.setStartYear(startDate.substring(3));
		}
		formVO.setDsaOnlyCourseName(dsaInstCrseEntity.getDsaOnlyCourseName());
		String dbQualType = dsaInstCrseEntity.getDsaQualificationType();
		if (Arrays.stream(QualificationType.values()).anyMatch(t -> t.name().equals(dbQualType) ||
				Objects.equals(dbQualType, null))) {
			// If db value matches any enum then it's not a custom entry
			formVO.setDsaQualificationType(dbQualType);
			formVO.setCustomQualType(StringUtils.EMPTY);
		} else {
			formVO.setDsaQualificationType(QualificationType.NONE.name());
			formVO.setCustomQualType(dbQualType);
		}
		
		formVO.setInstCode(dsaInstCrseEntity.getInstCode());
		formVO.setInstName(dsaInstCrseEntity.getInstName());
		formVO.setCourseMode(dsaInstCrseEntity.getCourseMode());
		formVO.setCurrentYear(Objects.equals(dsaInstCrseEntity.getCurrentYear(), 0) ? StringUtils.EMPTY : 
			Integer.toString(dsaInstCrseEntity.getCurrentYear()));
		formVO.setYearsToCompleteCourse(Objects.equals(dsaInstCrseEntity.getYearsToCompleteCourse(), 0) ? StringUtils.EMPTY : 
			Integer.toString(dsaInstCrseEntity.getYearsToCompleteCourse()));
	}
	
	/**
	 * Map the form to the entity
	 * @param formVO the {@link CourseDetailsFormVO}
	 * @param dsaInstCrseEntity the {@link DSAInstCrse}
	 */
	public static void mapCourseDetailsFormVOToDSAInstCrseEntity(CourseDetailsFormVO formVO,
			DSAInstCrse dsaInstCrseEntity) {
		
		dsaInstCrseEntity.setCourseName(formVO.getCourseName());
		dsaInstCrseEntity.setCurrentYear(NumberUtils.toInt(formVO.getCurrentYear()));
		dsaInstCrseEntity.setDsaCourseStartDate(Objects.equals(formVO.getStartMonth(), null) ? null : 
			formVO.getStartMonth() + DATE_SEPERATOR + formVO.getStartYear());
		dsaInstCrseEntity.setDsaOnlyCourseName(formVO.getDsaOnlyCourseName());
		dsaInstCrseEntity.setDsaQualificationType(Objects.equals(formVO.getDsaQualificationType(),QualificationType.NONE.name()) 
				? formVO.getCustomQualType() : formVO.getDsaQualificationType());
		dsaInstCrseEntity.setInstCode(formVO.getInstCode());
		dsaInstCrseEntity.setInstName(formVO.getInstName());
		dsaInstCrseEntity.setCourseMode(formVO.getCourseMode());
		dsaInstCrseEntity.setYearsToCompleteCourse(NumberUtils.toInt(formVO.getYearsToCompleteCourse()));
	}
	
}
