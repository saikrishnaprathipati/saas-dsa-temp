package uk.gov.saas.dsa.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.UnitTestHelper;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.persistence.StudentPersonalDetailsRepository;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.allowances.NMPHAllowancesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentCourseYearVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.nmph.AddNMPHFormVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.allowances.EquipmentController;
import uk.gov.saas.dsa.web.controller.allowances.NMPHController;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.saas.dsa.UnitTestHelper.STUDENT_REFERENCE_NUMBER;
import static uk.gov.saas.dsa.UnitTestHelper.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class NMPHControllerTest {

	private static final long _NMPH_ID = 11;

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private NMPHAllowancesService nmphService;

	@MockitoBean
	private FindStudentService findStudentService;

	@MockitoBean
	private StudRepository studRepo;
	
	@MockitoBean
	private ApplicationService applicationService;
	
	@Captor
	private ArgumentCaptor<NMPHAllowanceVO> nmphCaptor;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}

 

	@Test
	void shouldShowInitNYMPHPageIfUSSerIsConingFormConsumablesPage() throws Exception {

		mockNMPH(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, NMPHController.ADD_NMPH_FROM_EQUIPMENT_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());
	}

	@Test
	void shouldShowErrorPathForUnknownAction() throws Exception {

		mockNMPH(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());
	}

	@Test
	void shouldShowInitNMPHPageIfUSerIsConingFormAllowancesSummaryPage() throws Exception {

		mockNMPH(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, NMPHController.ADD_NMPH_FROM_SUMMARY_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertTrue(hasKeyValue(modelAndView, STUDENT_FIRST_NAME, FIRST_NAME));
		assertTrue(hasKeyValue(modelAndView, NMPHController.SHOW_SKIP_NMPH, Boolean.TRUE.toString()));
		assertTrue(hasKeyValue(modelAndView, NMPHController.SHOW_SKIP_NMPH, Boolean.TRUE.toString()));
		assertTrue(hasKeyValue(modelAndView, NMPH_CAP, "20,520.00"));
		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowNMPHSummaryPageIfUSerIsConingFormConsumablesPage() throws Exception {

		mockNMPH(Collections.singletonList(NMPHAllowanceVO.builder().build()));
		mockStudentFirstName(findStudentService);
		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, NMPHController.ADD_NMPH_FROM_EQUIPMENT_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowDashboardPageIfUSerClickDashboardLink() throws Exception {

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowConsumablesSummaryPageIfUSerClickBackAction() throws Exception {

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + EquipmentController.EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowAddNMPHPageIfUSerHasErrorsInTheRequest() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());

		assertTrue(hasValue(modelAndView, "You must add a type of support."));
		assertTrue(hasValue(modelAndView, "You must add a recommended provider."));
		assertTrue(hasValue(modelAndView, "You must add hours per week."));
		assertTrue(hasValue(modelAndView, "You must add an hourly rate."));
	}

	@Test
	void shouldValidateHoursNotMoreThan_50() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = mockFormHappyRequest();
		formVO.setHours("99");
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());

		assertTrue(hasValue(modelAndView, "nmph.hours.invalid.range"));
	}

	private AddNMPHFormVO mockFormHappyRequest() {
		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setCost("1.0");
		formVO.setRecommendedProvider("ASDAs");
		formVO.setTypeOfSupport("rrewre");
		formVO.setHourlyRate("2");

		formVO.setHours("20");
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		return formVO;
	}

	@Test
	void shouldValidateTypeOfSupportAndProviderInvalidValues() throws Exception {
		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setCost("1.0");
		formVO.setRecommendedProvider("£&£))*$");
		formVO.setTypeOfSupport("£&£))*$");
		formVO.setHourlyRate("2");
		formVO.setHours("5");

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());

		assertTrue(hasValue(modelAndView, "nmph.recommendedProvider.invalid"));
		assertTrue(hasValue(modelAndView, "nmph.typeOfSupport.invalid"));

	}

	@Test
	void shouldAddNMPHSuccessfully_cost_0_01() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("0.01");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_0_1() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("0.1");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_1() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("1");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_1_00() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("1.00");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_1_1() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("1.1");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_1_99() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("1.99");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddNMPHSuccessfully_cost_99999_99() throws Exception {

		AddNMPHFormVO formVO = addNMPHWithCost("99999.99");
		NMPHAllowanceVO value = nmphCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	private AddNMPHFormVO addNMPHWithCost(String cost) throws Exception {
		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setCost(cost);
		formVO.setRecommendedProvider("ASDAs");
		formVO.setTypeOfSupport("rrewre");
		formVO.setHourlyRate("2");
		formVO.setHours("3");

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());
		Mockito.verify(nmphService, times(1)).addNMPHAllowance(Mockito.any(NMPHAllowanceVO.class));
		verify(nmphService).addNMPHAllowance(nmphCaptor.capture());
		return formVO;
	}

	@Test
	void shouldShowNMPHSummaryPage() throws Exception {

		mockStudentFirstName(findStudentService);

		ApplicationKeyDataFormVO formVO = new ApplicationKeyDataFormVO();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPH_SUMMARY_PATH, flashMap, null, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_SUMMARY_PAGE, modelAndView.getViewName());

	}

 
	@Test
	void shouldShowErrorPageIfActionIsInvalidForChangeNMPH() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowChangeNMPHItemPageSuccessfully_for_INT_CHANGEN_MPH_ACTION() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_NMPH_ID);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, NMPHController.INT_CHANGEN_MPH_ACTION);

		NMPHAllowanceVO item = NMPHAllowanceVO.builder().id(_NMPH_ID).cost(BigDecimal.ONE).costStr("1.0")
				.hourlyRate(BigDecimal.ONE).hourlyRateStr("1.0").hours(1).recommendedProvider("ewwe").weeks(1)
				.typeOfSupport("qwe").build();
		Mockito.when(nmphService.getNMPHItem(_NMPH_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(NMPHController.ADVISOR_CHANGE_NMPH_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowErrorsSuccessfullyForChangeAction() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = new AddNMPHFormVO();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_NMPH_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(NMPHController.ADVISOR_CHANGE_NMPH_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldChangeNMPHItemSuccessfully() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_NMPH_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		NMPHAllowanceVO item = NMPHAllowanceVO.builder().id(_NMPH_ID).cost(BigDecimal.ONE).costStr("1.0")
				.hourlyRate(BigDecimal.ONE).hourlyRateStr("1.0").hours(1).recommendedProvider("ewwe").weeks(1)
				.typeOfSupport("qwe").build();
		Mockito.when(nmphService.getNMPHItem(_NMPH_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowNMPHSummaryForCancelAction() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_NMPH_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CANCEL_ACTION);

		NMPHAllowanceVO item = NMPHAllowanceVO.builder().id(_NMPH_ID).cost(BigDecimal.ONE).costStr("1.0")
				.hourlyRate(BigDecimal.ONE).hourlyRateStr("1.0").hours(1).recommendedProvider("ewwe").weeks(1)
				.typeOfSupport("qwe").build();
		Mockito.when(nmphService.getNMPHItem(_NMPH_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowDashboardPageCorrectly() throws Exception {

		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_NMPH_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.CHANGE_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		NMPHAllowanceVO item = NMPHAllowanceVO.builder().id(_NMPH_ID).cost(BigDecimal.ONE).costStr("1.0")
				.hourlyRate(BigDecimal.ONE).hourlyRateStr("1.0").hours(1).recommendedProvider("ewwe").weeks(1)
				.typeOfSupport("qwe").build();
		Mockito.when(nmphService.getNMPHItem(_NMPH_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.CHANGE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowRemovePageCorrectly() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, REMOVE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(NMPHController.ADVISOR_REMOVE_NMPH_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowErrorPageForUnknownAction() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

 
	@Test
	void shouldShowNMPHSummaryPageCorrectlyForBackAction() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowDashboardPageCorrectlyFor_DASHBOARD_ACTION() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldRemoveItemSuccessfully() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setRemoveItem(YesNoType.YES.name());
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS3XX_REDIRECTION);
		assertEquals(REDIRECT + NMPH_SUMMARY_PATH, modelAndView.getViewName());
		Mockito.verify(nmphService, times(1)).deleteItem(Mockito.anyLong());

	}

	@Test
	void shouldValidateRemoveItemFormSuccessfully() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.REMOVE_NMPH_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(NMPHController.ADVISOR_REMOVE_NMPH_PAGE, modelAndView.getViewName());
		assertTrue(hasValue(modelAndView, "allowances.remove.option.required"));
		Mockito.verify(nmphService, times(0)).deleteItem(Mockito.anyLong());

	}

	private void mockNMPH(List<NMPHAllowanceVO> list) {
		Mockito.when(nmphService.getAllNMPHAllowances(UnitTestHelper.DSA_APPLICATION_NO)).thenReturn(list);
	}

	private ModelAndView mockCostCall(String value) throws Exception {
		mockStudentFirstName(findStudentService);

		AddNMPHFormVO formVO = mockFormHappyRequest();
		formVO.setCost(value);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(NMPHController.ADD_NMPH_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, NMPHController.ADD_NMPH_ALLOWANCE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(NMPHController.ADVISOR_NMPH_ALLOWANCES_PAGE, modelAndView.getViewName());
		return modelAndView;
	}
	
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
