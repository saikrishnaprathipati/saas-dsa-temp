package uk.gov.saas.dsa.web.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.FindStudentFormVO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class FindStudentControllerTest {
	private static final String LAST_NAME_MAX_LENGTH = "lastName.maxLength";
	private static final String LAST_NAME_INVALID = "lastName.invalid";
	private static final String FIRST_NAME_INVALID = "firstName.invalid";
	private static final String FIRST_NAME_MAX_LENGTH = "firstName.maxLength";
	private static final String LAST_NAME_REQUIRED = "lastName.required";
	private static final String FIRST_NAME_REQUIRED = "firstName.required";
	private static final String _1 = "1";
	private static final String DATE_OF_BIRTH_YEAR_REQUIRED = "dateOfBirthYear.required";
	private static final String DATE_OF_BIRTH_DAY_REQUIRED = "dateOfBirthDay.required";
	private static final String DATE_OF_BIRTH_HUNDRED_YEARS = "dateOfBirth.hundredYears";
	private static final String DATE_OF_BIRTH_FIFTEEN_YEARS = "dateOfBirth.fifteenYears";
	private static final String LNAME = "lname";
	private static final String FNAME = "fname";
	private static final String FIND_STUDENT_FORM_VO = "findStudentFormVO";
	private static final String DATE_OF_BIRTH_PAST = "dateOfBirth.past";
	private static final String DATE_OF_BIRTH_DAY_INVALID = "dateOfBirthDay.invalid";
	private static final String DATE_OF_BIRTH_YEAR_INVALID = "dateOfBirthYear.invalid";
	private static final String DATE_OF_BIRTH_MONTH_INVALID = "dateOfBirthMonth.invalid";
	private static final String VALIDATION_BINDING = "org.springframework.validation.BindingResult.findStudentFormVO";
	private static final String ADVISOR_STUDENT_RESULTS = "advisor/studentResults";
	private static final String FIND_STUDENT = "/findStudent";
	private static final String ADVISOR_FIND_STUDENT = "advisor/findStudent";
	@MockitoBean
	private FindStudentService findStudentService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private WebApplicationContext webAppContext;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void shouldRedirectToFindStudentInitPageCorrectly() throws Exception {
		ResultMatcher redirection = MockMvcResultMatchers.status().is2xxSuccessful();
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(FIND_STUDENT);
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirection).andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(ADVISOR_FIND_STUDENT, modelAndView.getViewName());
	}

	@Test
	void shouldRedirectToResultPageIfNamesContainsSpaceApostropheAndDash() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName("firt nam-e' ");
		findStudentFormVO.setLastName("Last nam-e' ");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyRedirect(builder, ADVISOR_STUDENT_RESULTS);
	}

	@Test
	void shouldReturnToFindStudentPageInfTheFormIsEmpty() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);

		String foreNameError = messageSource.getMessage(FIRST_NAME_REQUIRED, null, LocaleContextHolder.getLocale());
		String lastNameError = messageSource.getMessage(LAST_NAME_REQUIRED, null, LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(foreNameError, lastNameError));

		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldShowErrorMessagesForFirstNameAndLastHasUnknownCharacters() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		findStudentFormVO.setFirstName(FNAME + "&");
		findStudentFormVO.setLastName(LNAME + "&");
		String foreNameError = messageSource.getMessage(FIRST_NAME_INVALID, null, LocaleContextHolder.getLocale());
		String lastNameError = messageSource.getMessage(LAST_NAME_INVALID, null, LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(foreNameError, lastNameError));

		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldShowErrorMessagesForFirstNameAndLastMoreThan25Characters() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		findStudentFormVO.setFirstName(FNAME + FNAME + FNAME + FNAME + FNAME + FNAME + FNAME + FNAME);
		findStudentFormVO.setLastName(LNAME + LNAME + LNAME + LNAME + LNAME + LNAME + LNAME + LNAME);
		String foreNameError = messageSource.getMessage(FIRST_NAME_MAX_LENGTH, null, LocaleContextHolder.getLocale());
		String lastNameError = messageSource.getMessage(LAST_NAME_MAX_LENGTH, null, LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(foreNameError, lastNameError));

		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldReturnToFindStudentResultsPageInfTheFormHasCorrectValues() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);

		verifyRedirect(builder, ADVISOR_STUDENT_RESULTS);
	}

	@Test
	void shouldReturnToFindStudentPageInfTheDobIsToday() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		mockDateOfBirth(findStudentFormVO, DateUtils.addDays(new Date(), 0));

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		String errorMessage = messageSource.getMessage(DATE_OF_BIRTH_FIFTEEN_YEARS, new Object[] { 15 },
				LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(errorMessage));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldReturnToFindStudentPageInfTheDobIsTomorrow() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);

		mockDateOfBirth(findStudentFormVO, DateUtils.addDays(new Date(), 1));

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		String errorMessage = messageSource.getMessage(DATE_OF_BIRTH_PAST, null, LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(errorMessage));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldReturnToFindStudentPageInfTheDobIsYesterday() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		mockDateOfBirth(findStudentFormVO, DateUtils.addDays(new Date(), -1));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		String errorMessage = messageSource.getMessage(DATE_OF_BIRTH_FIFTEEN_YEARS, new Object[] { 15 },
				LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(errorMessage));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldReturnToFindStudentPageInfTheDobIs101yearsBack() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		mockDateOfBirth(findStudentFormVO, DateUtils.addYears(new Date(), -101));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		String errorMessage = messageSource.getMessage(DATE_OF_BIRTH_HUNDRED_YEARS, new Object[] { 100 },
				LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(errorMessage));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfDateIsMissingFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("2023");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_DAY_REQUIRED));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfMonthIsMissingFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobYear("2023");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList("dateOfBirthMonth.required"));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearIsMissingFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_REQUIRED));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearIsEmptyStringFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("  ");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_REQUIRED));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearIsInvalidFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("100");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearValueIs5Digits() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("12345");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearValueHavingSpaceDigits() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("12 3");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearValueHavingPeriodDigits() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("12.3");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfYearValueHavingSpaceAtLastDigits() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth(_1);
		findStudentFormVO.setDobYear("123 ");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfMonthIsInvalidFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay(_1);
		findStudentFormVO.setDobMonth("14");
		findStudentFormVO.setDobYear("2013");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_MONTH_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfDayIsInvalidFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("165");
		findStudentFormVO.setDobMonth("12");
		findStudentFormVO.setDobYear("2013");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_DAY_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfDateWithInvalidFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("165");
		findStudentFormVO.setDobMonth("123");
		findStudentFormVO.setDobYear("235");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder,
				asList(DATE_OF_BIRTH_DAY_INVALID, DATE_OF_BIRTH_MONTH_INVALID, DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfDateWithNegativeValuesFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("-1");
		findStudentFormVO.setDobMonth("-2");
		findStudentFormVO.setDobYear("-2014");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder,
				asList(DATE_OF_BIRTH_DAY_INVALID, DATE_OF_BIRTH_MONTH_INVALID, DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfDateStringsFromDOB() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("a");
		findStudentFormVO.setDobMonth("a");
		findStudentFormVO.setDobYear("a");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder,
				asList(DATE_OF_BIRTH_DAY_INVALID, DATE_OF_BIRTH_MONTH_INVALID, DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldValidateIfAllDOBValuesAreZero() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("0");
		findStudentFormVO.setDobMonth("0");
		findStudentFormVO.setDobYear("0");
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder,
				asList(DATE_OF_BIRTH_DAY_INVALID, DATE_OF_BIRTH_MONTH_INVALID, DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldGotoResultPageIfDOBDayAndMonthAreStartingWithZero() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("01");
		findStudentFormVO.setDobMonth("01");
		findStudentFormVO.setDobYear("0111");

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyErrorCodes(builder, asList(DATE_OF_BIRTH_YEAR_INVALID));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldGotoResultPageIfDOBDayAndMonthAreEndingWithSpace() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		findStudentFormVO.setDobDay("1 ");
		findStudentFormVO.setDobMonth("1 ");
		findStudentFormVO.setDobYear("1000");

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		String errorMessage = messageSource.getMessage(DATE_OF_BIRTH_HUNDRED_YEARS, new Object[] { 100 },
				LocaleContextHolder.getLocale());
		verifyErrorMessages(builder, asList(errorMessage));
		verifyRedirect(builder, ADVISOR_FIND_STUDENT);
	}

	@Test
	void shouldGotoResultPageForAllValidFormInput() throws Exception {

		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		findStudentFormVO.setFirstName(FNAME);
		findStudentFormVO.setLastName(LNAME);
		mockDateOfBirth(findStudentFormVO, DateUtils.addYears(new Date(), -20));
		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(FIND_STUDENT);
		builder.flashAttr(FIND_STUDENT_FORM_VO, findStudentFormVO);
		verifyRedirect(builder, ADVISOR_STUDENT_RESULTS);
	}

	private void mockDateOfBirth(FindStudentFormVO findStudentFormVO, Date date) {

		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int dateVal = localDate.getDayOfMonth();
		int month = localDate.getMonthValue();
		int year = localDate.getYear();

		findStudentFormVO.setDobDay(Integer.toString(dateVal));
		findStudentFormVO.setDobMonth(Integer.toString(month));
		findStudentFormVO.setDobYear(Integer.toString(year));
	}

	private void verifyErrorMessages(MockHttpServletRequestBuilder builder, List<String> errorMessages)
			throws Exception {
		ResultMatcher ok = MockMvcResultMatchers.status().isOk();
		MvcResult result = this.mockMvc.perform(builder).andExpect(ok).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model.get(VALIDATION_BINDING);
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<String> messages = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());

		assertTrue(messages.containsAll(errorMessages));

		assertEquals(ADVISOR_FIND_STUDENT, modelAndView.getViewName());
	}

	private void verifyErrorCodes(MockHttpServletRequestBuilder builder, List<String> errorCodes) throws Exception {
		ResultMatcher ok = MockMvcResultMatchers.status().isOk();
		MvcResult result = this.mockMvc.perform(builder).andExpect(ok).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		Map<String, Object> model = modelAndView.getModel();
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) model.get(VALIDATION_BINDING);
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getCode).collect(Collectors.toList());
		assertTrue(messageCodes.containsAll(errorCodes));
		assertEquals(ADVISOR_FIND_STUDENT, modelAndView.getViewName());
	}

	private void verifyRedirect(MockHttpServletRequestBuilder builder, String redirection) throws Exception {
		ResultMatcher redirect = MockMvcResultMatchers.status().is2xxSuccessful();
		MvcResult result = this.mockMvc.perform(builder).andExpect(redirect).andReturn();
		ModelAndView modelAndView = result.getModelAndView();
		assertEquals(redirection, modelAndView.getViewName());
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
