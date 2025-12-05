package uk.gov.saas.dsa.web.helper;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.FindStudentFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;

import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;
import static uk.gov.saas.dsa.service.ServiceUtil.capitalizeFully;
import static uk.gov.saas.dsa.web.helper.DSAConstants.INVALID;

public class FindStudentHelper {
	private static final String DATE_OF_BIRTH_FIFTEEN_YEARS = "dateOfBirth.fifteenYears";
	private static final String DATE_OF_BIRTH_HUNDRED_YEARS = "dateOfBirth.hundredYears";
	private static final String DATE_OF_BIRTH_PAST = "dateOfBirth.past";
	private static final Logger logger = LogManager.getLogger(FindStudentHelper.class);
	private static final String DOB_MONTH = "dobMonth";

	private static final String DOB_DAY = "dobDay";

	private static final String DOB_YEAR = "dobYear";

	private static final String DATE_OF_BIRTH_YEAR = "dateOfBirthYear";

	private static final String DATE_OF_BIRTH_MONTH = "dateOfBirthMonth";

	private static final String DATE_OF_BIRTH_DAY = "dateOfBirthDay";

	private static final String REQUIRED = ".required";

	private static final String DATE_OF_BIRTH_FIELD = "dateOfBirth";
	private static final String[] tensNames = {"", " ten", " twenty", " thirty", " forty", " fifty", " sixty",
			" seventy", " eighty", " ninety"};

	private static final String[] numNames = {"", " one", " two", " three", " four", " five", " six", " seven",
			" eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen",
			" seventeen", " eighteen", " nineteen"};

	public static Date populateDOB(FindStudentFormVO findStudentFormVO, BindingResult bindingResult) {
		String dayString = findStudentFormVO.getDobDay();
		String monthString = findStudentFormVO.getDobMonth();
		String yearString = findStudentFormVO.getDobYear();
		Date date = null;
		// To check the date fields as left optional or not
		boolean enteredAnyDateFields = hasText(dayString) || hasText(monthString) || hasText(yearString);

		if (enteredAnyDateFields) {

			int day = sanitizeDayValue(bindingResult, dayString);
			int month = sanitizeMonthValue(bindingResult, monthString);
			int year = sanitizeYearValue(bindingResult, yearString);
			if (noErrorsFor("Day", bindingResult) && noErrorsFor("Month", bindingResult)
					&& noErrorsFor("Year", bindingResult)) {

				date = Date.valueOf(LocalDate.of(year, month, day));
			}

		}
		return date;
	}

	private static boolean noErrorsFor(String field, BindingResult bindingResult) {
		List<ObjectError> allErrors = bindingResult.getAllErrors();
		List<String> errorCodes = allErrors.stream().map(ObjectError::getCode).filter(Objects::nonNull)
				.map(String::toUpperCase).collect(Collectors.toList());
		String invalid = "dateOfBirth" + field + ".invalid";
		String required = "dateOfBirth" + field + ".required";
		return !errorCodes.contains(invalid.toUpperCase()) && !errorCodes.contains(required.toUpperCase());

	}

	private static int sanitizeYearValue(BindingResult bindingResult, String yearString) {
		logger.info("yearString: {}", yearString);
		int year = 0;
		if (hasText(yearString)) {
			try {
				year = Integer.parseInt(yearString);
				if (yearString.length() != 4 || year < 1000) {
					bindingResult.rejectValue(DOB_YEAR, DATE_OF_BIRTH_YEAR + INVALID);
				}
			} catch (NumberFormatException e) {
				logger.info("exception for year: {}", e.getMessage());
				bindingResult.rejectValue(DOB_YEAR, DATE_OF_BIRTH_YEAR + INVALID);
			}
		} else {
			bindingResult.rejectValue(DOB_YEAR, DATE_OF_BIRTH_YEAR + REQUIRED);
		}
		return year;
	}

	private static int sanitizeMonthValue(BindingResult bindingResult, String monthString) {
		logger.info("monthString: {}", monthString);

		int month = 0;
		if (hasText(monthString)) {
			try {
				month = Integer.parseInt(monthString.trim());
				if (month <= 0 || month > 12) {
					bindingResult.rejectValue(DOB_MONTH, DATE_OF_BIRTH_MONTH + INVALID);
				}
			} catch (NumberFormatException e) {
				logger.info("exception for month: {}", e.getMessage());
				bindingResult.rejectValue(DOB_MONTH, DATE_OF_BIRTH_MONTH + INVALID);
			}
		} else {
			bindingResult.rejectValue(DOB_MONTH, DATE_OF_BIRTH_MONTH + REQUIRED);
		}
		return month;
	}

	private static int sanitizeDayValue(BindingResult bindingResult, String dateString) {
		logger.info("dateString: {}", dateString);
		int day = 0;
		if (hasText(dateString)) {
			try {
				day = Integer.parseInt(dateString.trim());
				if (day <= 0 || day > 31) {
					bindingResult.rejectValue(DOB_DAY, DATE_OF_BIRTH_DAY + INVALID);
				}
			} catch (NumberFormatException e) {
				logger.info("exception for day: {}", e.getMessage());
				bindingResult.rejectValue(DOB_DAY, DATE_OF_BIRTH_DAY + INVALID);
			}
		} else {
			bindingResult.rejectValue(DOB_DAY, DATE_OF_BIRTH_DAY + REQUIRED);
		}
		return day;
	}

	public static void validateDateOfBirth(FindStudentFormVO findStudentFormVO, BindingResult bindingResult,
										   MessageSource messageSource) {

		Date dateOfBirth = findStudentFormVO.getDateOfBirth();
		if (dateOfBirth != null) {

			java.util.Date userEnteredDOB = new java.util.Date(dateOfBirth.getTime());
			logger.info("userEnteredDOB: {}", userEnteredDOB);
			java.util.Date currentDateMinus15years = DateUtils.addYears(new java.util.Date(), -15);
			java.util.Date currentDateMinus100years = DateUtils.addYears(new java.util.Date(), -100);

			String errorMessage = null;
			if (userEnteredDOB.after(new java.util.Date())) {
				errorMessage = messageSource.getMessage(DATE_OF_BIRTH_PAST, null, Locale.UK);
			} else if (userEnteredDOB.before(currentDateMinus100years)) {

				errorMessage = messageSource.getMessage(DATE_OF_BIRTH_HUNDRED_YEARS, new Object[]{100},
						Locale.UK);
			} else if (userEnteredDOB.after(currentDateMinus15years)) {
				errorMessage = messageSource.getMessage(DATE_OF_BIRTH_FIFTEEN_YEARS, new Object[]{15},
						Locale.UK);
			}
			if (hasText(errorMessage)) {
				logger.info("date entered: {}, error message: {}", userEnteredDOB, errorMessage);
				bindingResult.addError(
						new FieldError(findStudentFormVO.getClass().getName(), DATE_OF_BIRTH_FIELD, errorMessage));

			}
		}

	}

	public static String convertNumberToWord(long number) {
		logger.info("convertNumberToWord: {}", number);
		// 0 to 999 999 999 999
		if (number == 0) {
			return "zero";
		}

		// pad with "0"
		String mask = "000000000000";
		DecimalFormat df = new DecimalFormat(mask);
		String sNumber = df.format(number);

		// XXXnnnnnnnnn
		int billions = Integer.parseInt(sNumber.substring(0, 3));
		// nnnXXXnnnnnn
		int millions = Integer.parseInt(sNumber.substring(3, 6));
		// nnnnnnXXXnnn
		int hundredThousands = Integer.parseInt(sNumber.substring(6, 9));
		// nnnnnnnnnXXX
		int thousands = Integer.parseInt(sNumber.substring(9, 12));

		String tradBillions;
		switch (billions) {
			case 0:
				tradBillions = "";
				break;
			case 1:
				tradBillions = convertLessThanOneThousand(billions) + " billion ";
				break;
			default:
				tradBillions = convertLessThanOneThousand(billions) + " billion ";
		}
		String result = tradBillions;

		String tradMillions;
		switch (millions) {
			case 0:
				tradMillions = "";
				break;
			case 1:
				tradMillions = convertLessThanOneThousand(millions) + " million ";
				break;
			default:
				tradMillions = convertLessThanOneThousand(millions) + " million ";
		}
		result = result + tradMillions;

		String tradHundredThousands;
		switch (hundredThousands) {
			case 0:
				tradHundredThousands = "";
				break;
			case 1:
				tradHundredThousands = "one thousand ";
				break;
			default:
				tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " thousand ";
		}
		result = result + tradHundredThousands;

		String tradThousand;
		tradThousand = convertLessThanOneThousand(thousands);
		result = result + tradThousand;

		// remove extra spaces!
		return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
	}

	private static String convertLessThanOneThousand(int number) {
		String soFar;

		if (number % 100 < 20) {
			soFar = numNames[number % 100];
			number /= 100;
		} else {
			soFar = numNames[number % 10];
			number /= 10;

			soFar = tensNames[number % 10] + soFar;
			number /= 10;
		}
		if (number == 0)
			return soFar;
		return numNames[number] + " hundred" + soFar;
	}

	public static void setStudentDetails(FindStudentService findStudentService,
										 ApplicationResponse dashboardData) {

		StudentResultVO studentResult = new StudentResultVO();
		try {
			if (dashboardData.getSessionCode() != null) {
				studentResult = findStudentService.findByStudReferenceNumber(dashboardData.getStudentReferenceNumber(), dashboardData.getSessionCode());
			} else {
				studentResult = findStudentService.findByStudReferenceNumber(dashboardData.getStudentReferenceNumber());
			}
		} catch (IllegalAccessException e) {
			logger.error("Exception fetching student details for student Reference number {} "
					, dashboardData.getStudentReferenceNumber(), e);
		}

		dashboardData.setFirstName(studentResult.getFirstName());
		dashboardData.setLastName(studentResult.getLastName());
		dashboardData.setDateOfBirth(studentResult.getDob());
		dashboardData.setAcademicYear(studentResult.getStudentCourseYear().getAcademicYearFull());
		dashboardData.setSessionCode(studentResult.getStudentCourseYear().getSessionCode());
		dashboardData.setFundingEligibilityStatus(studentResult.getFundingEligibilityStatus());
		dashboardData.setInstitutionName(capitalizeFully(studentResult.getStudentCourseYear().getInstitutionName()));
	}
}
