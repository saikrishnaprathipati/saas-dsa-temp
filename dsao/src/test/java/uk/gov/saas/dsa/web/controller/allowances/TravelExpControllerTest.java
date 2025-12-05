package uk.gov.saas.dsa.web.controller.allowances;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.allowances.TravelExpAllowancesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.travelExp.RemoveTaxiProviderFormVO;
import uk.gov.saas.dsa.vo.travelExp.RemoveTravelExpFormVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpSelectionFormVO;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.saas.dsa.UnitTestHelper.FIRST_NAME;
import static uk.gov.saas.dsa.UnitTestHelper.performPostMap;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

@ActiveProfiles({"localdev"})
@SpringBootTest
@AutoConfigureMockMvc
class TravelExpControllerTest {
	private static final long DSA_APPLICATION_NUMBER = 1L;
	private static final long STUDENT_REFERENCE_NUMBER = 12222L;
	private static final ResultMatcher IS2XX_SUCCESSFUL = MockMvcResultMatchers.status().is2xxSuccessful();
	private static final ResultMatcher IS3XX_REDIRECT = MockMvcResultMatchers.status().is3xxRedirection();
	public static final String BACK_TO_SELECT_TRAVEL_EXP = "BACK_TO_SELECT_TRAVEL_EXP";
	private static final String INIT_TRAVEL_EXP_FROM_TE_SELECTION_ACTION = "INIT_TRAVEL_EXP_FROM_TE_SELECTION";
	private static final String TRAVEL_EXP_SELECTION_FORM_VO = "travelExpSelectionFormVO";
	private static final String SELECT_TRAVEL_EXP_FROM_NMPH_ACTION = "SELECT_TRAVEL_EXP_FROM_NMPH_SUMMARY";
	private static final String REMOVE_TAXI_PROVIDER_FORM_VO = "removeTaxiProviderFormVO";
	private static final String REMOVE_PROVIDER_BACK = "REMOVE_PROVIDER_BACK";
	private static final String REMOVE_TAXI_PROVIDER_PATH = "removeTaxiProvider";
	private static final String REMOVE_TRAVEL_EXP_FORM_VO = "removeTravelExpFormVO";
	private static final String REMOVE_TRAVEL_EXP_PATH = "removeTravelExp";
	private static final String CONFIRM_REMOVE_TRAVEL_EXP_ACTION = "CONFIRM_REMOVE_TRAVEL_EXP";

	@MockitoBean
	TravelExpAllowancesService travelExpService;
	@MockitoBean
	FindStudentService findStudentService;
	@MockitoBean
	ApplicationService applicationService;
	@Autowired
	private WebApplicationContext webAppContext;

	private MockMvc mockMvc;
	TravelExpController subject;

	@BeforeEach
	void setUp() {
		subject = new TravelExpController(travelExpService, findStudentService, applicationService);
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);
		this.mockMvc = builder.build();
		mockSecurityContext();
	}

	@Test
	void testSelectTravelExpenseSkipAction() throws Exception {
		String action = "SKIP";

		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/selectAccommodation", modelAndView.getViewName());
	}

	@Test
	void testSelectTravelExpenseDashboardAction() throws Exception {
		String action = "DASHBOARD";

		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/applicationDashboard", modelAndView.getViewName());
	}

	@Test
	void testSelectTravelExpenseBackAction() throws Exception {
		String action = "BACK";

		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/nmphSummary", modelAndView.getViewName());
	}

	@Test
	void testSelectTravelExpenseBackToTravelAction() throws Exception {
		String action = BACK_TO_SELECT_TRAVEL_EXP;
		mockStudentFirstName();
		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertEquals("advisor/travelExp/travelExpensesSelection", modelAndView.getViewName());
	}

	@Test
	void testSelectTravelExpenseInitialiseAction() throws Exception {
		String action = INIT_TRAVEL_EXP_FROM_TE_SELECTION_ACTION;
		mockStudentFirstName();
		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		List<String> types = Arrays.asList("OWN_VEHICLE", "TAXI", "LIFT");
		travelExpSelectionFormVO.setTravelExpTypes(types);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/addTravelExpense", modelAndView.getViewName());
	}

	@Test
	void testSelectTravelExpenseFromNMPHAction() throws Exception {
		String action = SELECT_TRAVEL_EXP_FROM_NMPH_ACTION;
		mockStudentFirstName();
		TravelExpSelectionFormVO travelExpSelectionFormVO = new TravelExpSelectionFormVO();
		travelExpSelectionFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		travelExpSelectionFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		List<String> types = Arrays.asList("OWN_VEHICLE", "TAXI", "LIFT");
		travelExpSelectionFormVO.setTravelExpTypes(types);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(TRAVEL_EXP_SELECTION_FORM_VO, travelExpSelectionFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, SELECT_TRAVEL_EXPENSE_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertEquals("advisor/travelExp/travelExpensesSelection", modelAndView.getViewName());
	}

	@Test
	void testTravelExpSummary() throws Exception {
		mockStudentFirstName();
		ApplicationKeyDataFormVO keyDataFormVO = new ApplicationKeyDataFormVO();
		keyDataFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		keyDataFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, keyDataFormVO);
		Mockito.when(travelExpService.getTravelExpAllowances(DSA_APPLICATION_NUMBER)).thenReturn(Collections.emptyList());
		ModelAndView modelAndView = performPostMap(this.mockMvc, TRAVEL_EXP_SUMMARY_PATH, flashMap, new HashMap<>(),
				IS2XX_SUCCESSFUL);

		assertEquals("advisor/travelExp/travelExpSummary", modelAndView.getViewName());
	}

	@Test
	void testRemoveTravelProviderBack() throws Exception {
		String action = REMOVE_PROVIDER_BACK;
		mockStudentFirstName();
		RemoveTaxiProviderFormVO removeTaxiProviderFormVO = new RemoveTaxiProviderFormVO();
		removeTaxiProviderFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		removeTaxiProviderFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(REMOVE_TAXI_PROVIDER_FORM_VO, removeTaxiProviderFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, REMOVE_TAXI_PROVIDER_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/addTravelExpense", modelAndView.getViewName());
	}

	@Test
	void testRemoveTravelProviderDashboard() throws Exception {
		String action = DASHBOARD_ACTION;
		mockStudentFirstName();
		RemoveTaxiProviderFormVO removeTaxiProviderFormVO = new RemoveTaxiProviderFormVO();
		removeTaxiProviderFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		removeTaxiProviderFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(REMOVE_TAXI_PROVIDER_FORM_VO, removeTaxiProviderFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, REMOVE_TAXI_PROVIDER_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/applicationDashboard", modelAndView.getViewName());
	}

	@Test
	void testRemoveTravelExpRemoveFromSummary() throws Exception {
		String action = "REMOVE_FROM_SUMMARY";
		mockStudentFirstName();
		RemoveTravelExpFormVO removeTaxiProviderFormVO = new RemoveTravelExpFormVO();
		removeTaxiProviderFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		removeTaxiProviderFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		removeTaxiProviderFormVO.setTravelExpType(TravelExpType.TAXI);
		removeTaxiProviderFormVO.setRemoveItem("example");
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(REMOVE_TRAVEL_EXP_FORM_VO, removeTaxiProviderFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, REMOVE_TRAVEL_EXP_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertEquals("advisor/travelExp/removeTravelExp", modelAndView.getViewName());
	}

	@Test
	void testRemoveTravelExpConfirmRemove() throws Exception {
		String action = CONFIRM_REMOVE_TRAVEL_EXP_ACTION;
		mockStudentFirstName();
		RemoveTravelExpFormVO removeTaxiProviderFormVO = new RemoveTravelExpFormVO();
		removeTaxiProviderFormVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		removeTaxiProviderFormVO.setDsaApplicationNumber(DSA_APPLICATION_NUMBER);
		removeTaxiProviderFormVO.setTravelExpType(TravelExpType.TAXI);
		removeTaxiProviderFormVO.setRemoveItem("YES");
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(REMOVE_TRAVEL_EXP_FORM_VO, removeTaxiProviderFormVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, action);
		ModelAndView modelAndView = performPostMap(this.mockMvc, REMOVE_TRAVEL_EXP_PATH, flashMap, paramMap,
				IS3XX_REDIRECT);

		assertEquals("redirect:/travelExpSummary", modelAndView.getViewName());
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private void mockStudentFirstName() throws Exception {
		StudentResultVO studResult = new StudentResultVO();
		studResult.setFirstName(FIRST_NAME);
		Mockito.when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studResult);
	}
}
