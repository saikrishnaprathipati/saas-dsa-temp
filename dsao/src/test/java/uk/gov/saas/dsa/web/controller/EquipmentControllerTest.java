package uk.gov.saas.dsa.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
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
import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.persistence.readonly.StudRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.allowances.EquipmentService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.equipment.AddEquipmentFormVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.equipment.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.allowances.EquipmentController;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.saas.dsa.UnitTestHelper.STUDENT_REFERENCE_NUMBER;
import static uk.gov.saas.dsa.UnitTestHelper.*;
import static uk.gov.saas.dsa.web.controller.allowances.EquipmentController.EQUIPMENT_SUMMARY_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;

@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class EquipmentControllerTest {
	private static final long _EQUIPMENT_ID = 11;
	private static final String PRODUCT_NAME = "Laptop";
	private static final String DESCRIPTION = "Work Laptop";

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private EquipmentService equipmentService;

	@MockitoBean
	private FindStudentService findStudentService;

	@Autowired
	private StudRepository studRepo;

	@MockitoBean
	private ApplicationService applicationService;
	
	@Captor
	private ArgumentCaptor<EquipmentAllowanceVO> equipmentCaptor;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);
		this.mockMvc = builder.build();
		mockSecurityContext();
		mockDSAApplicationMade();
	}

	 

	@Test
	void shouldShowInitEquipmentPageIfUserIsComingFromConsumablesPage() throws Exception {

		mockEquipment(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_EQUIPMENT_FROM_CONSUMABLES_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(EquipmentController.ADVISOR_ADD_EQUIPMENT_PAGE, modelAndView.getViewName());
	}

	@Test
	void shouldShowErrorPathForUnknownAction() throws Exception {

		mockEquipment(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());
	}

	@Test
	void shouldShowInitEquipmentPageIfUSerIsConingFormAllowancesSummaryPage() throws Exception {

		mockEquipment(Collections.emptyList());
		mockStudentFirstName(findStudentService);
		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_EQUIPMENT_FROM_CONSUMABLES_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertTrue(hasKeyValue(modelAndView, STUDENT_FIRST_NAME, FIRST_NAME));
		assertTrue(hasKeyValue(modelAndView, EquipmentController.SHOW_SKIP_EQUIPMENT, Boolean.TRUE.toString()));
		assertTrue(hasKeyValue(modelAndView, EquipmentController.SHOW_SKIP_EQUIPMENT, Boolean.TRUE.toString()));
		assertTrue(hasKeyValue(modelAndView, EQUIPMENT_CAP, "5,160.00"));
		assertEquals(EquipmentController.ADVISOR_ADD_EQUIPMENT_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowEquipmentSummaryPageIfUSerIsConingFormConsumablesPage() throws Exception {

		mockEquipment(Collections.singletonList(EquipmentAllowanceVO.builder().build()));
		mockStudentFirstName(findStudentService);
		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_EQUIPMENT_FROM_CONSUMABLES_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowDashboardPageIfUSerClickDashboardLink() throws Exception {

		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowAddEquipmentPageIfUSerHasErrorsInTheRequest() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(EquipmentController.ADVISOR_ADD_EQUIPMENT_PAGE, modelAndView.getViewName());

		assertTrue(hasValue(modelAndView, "You must add a product name."));
		assertTrue(hasValue(modelAndView, "You must add a cost."));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_zro() throws Exception {

		ModelAndView modelAndView = mockCostCall("0");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid.range"));
		assertTrue(hasValue(modelAndView, "0.01"));
		assertTrue(hasValue(modelAndView, "99,999.99"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_negative() throws Exception {

		ModelAndView modelAndView = mockCostCall("-1");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_negative_zero() throws Exception {

		ModelAndView modelAndView = mockCostCall("-0");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_hex_number() throws Exception {

		ModelAndView modelAndView = mockCostCall("2e22");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_double_number() throws Exception {

		ModelAndView modelAndView = mockCostCall("2d");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_long_number() throws Exception {

		ModelAndView modelAndView = mockCostCall("2L");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostFieldForInvalidRange_for_moreThan_5_digits() throws Exception {

		ModelAndView modelAndView = mockCostCall("999999");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid.range"));
	}

	@Test
	void shouldValidateCostField_for_moreThen_2_decimal_points() throws Exception {

		ModelAndView modelAndView = mockCostCall("9.9999");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostField_for_dot_01() throws Exception {

		ModelAndView modelAndView = mockCostCall(".001");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	@Test
	void shouldValidateCostField_for_1_000() throws Exception {

		ModelAndView modelAndView = mockCostCall("1.000");

		assertTrue(hasValue(modelAndView, "equipment.cost.invalid"));
	}

	private AddEquipmentFormVO mockFormHappyRequest() {
		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setCost("1.0");
		formVO.setProductName(PRODUCT_NAME);
		formVO.setDescription(DESCRIPTION);
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		return formVO;
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_0_01() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("0.01");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_0_1() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("0.1");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_1() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("1");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_1_00() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("1.00");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_1_1() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("1.1");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_1_99() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("1.99");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	@Test
	void shouldAddEquipmentSuccessfully_cost_99999_99() throws Exception {

		AddEquipmentFormVO formVO = addEquipmentWithCost("99999.99");
		EquipmentAllowanceVO value = equipmentCaptor.getValue();
		assertEquals(BigDecimal.valueOf(Double.parseDouble(formVO.getCost())), value.getCost());
	}

	private AddEquipmentFormVO addEquipmentWithCost(String cost) throws Exception {
		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setCost(cost);

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setProductName(PRODUCT_NAME);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());
		Mockito.verify(equipmentService, times(1)).addEquipmentAllowance(Mockito.any(EquipmentAllowanceVO.class));
		verify(equipmentService).addEquipmentAllowance(equipmentCaptor.capture());
		return formVO;
	}

	@Test
	void shouldShowEquipmentSummaryPage() throws Exception {

		mockStudentFirstName(findStudentService);

		ApplicationKeyDataFormVO formVO = new ApplicationKeyDataFormVO();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, "");

		ModelAndView modelAndView = performPostMap(this.mockMvc, EQUIPMENT_SUMMARY_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertEquals(EquipmentController.ADVISOR_EQUIPMENT_SUMMARY_PAGE, modelAndView.getViewName());

	}
 
	@Test
	void shouldShowErrorPageIfActionIsInvalidForChangeEquipment() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DSAConstants.BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowChangeEquipmentItemPageSuccessfully_for_INT_CHANGEN_MPH_ACTION() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = new AddEquipmentFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_EQUIPMENT_ID);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.INIT_CHANGE_EQUIPMENT_ACTION);

		EquipmentAllowanceVO item = EquipmentAllowanceVO.builder().id(_EQUIPMENT_ID).cost(BigDecimal.ONE).costStr("1.0")
				.productName(PRODUCT_NAME).description(DESCRIPTION).build();
		Mockito.when(equipmentService.getEquipmentItem(_EQUIPMENT_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(EquipmentController.ADVISOR_CHANGE_EQUIPMENT_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldShowErrorsSuccessfullyForChangeAction() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_EQUIPMENT_ID);
		formVO.setCost("");
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(EquipmentController.ADVISOR_CHANGE_EQUIPMENT_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldChangeEquipmentItemSuccessfully() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_EQUIPMENT_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		EquipmentAllowanceVO item = EquipmentAllowanceVO.builder().id(_EQUIPMENT_ID).cost(BigDecimal.ONE).costStr("1.0")
				.productName(PRODUCT_NAME).description(DESCRIPTION).build();
		Mockito.when(equipmentService.getEquipmentItem(_EQUIPMENT_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowEquipmentSummaryForCancelAction() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_EQUIPMENT_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CANCEL_ACTION);

		EquipmentAllowanceVO item = EquipmentAllowanceVO.builder().id(_EQUIPMENT_ID).cost(BigDecimal.ONE).costStr("1.0")
				.productName(PRODUCT_NAME).description(DESCRIPTION).build();
		Mockito.when(equipmentService.getEquipmentItem(_EQUIPMENT_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowDashboardPageCorrectly() throws Exception {

		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = mockFormHappyRequest();

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setId(_EQUIPMENT_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.CHANGE_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		EquipmentAllowanceVO item = EquipmentAllowanceVO.builder().id(_EQUIPMENT_ID).cost(BigDecimal.ONE).costStr("1.0")
				.productName(PRODUCT_NAME).description(DESCRIPTION).build();
		Mockito.when(equipmentService.getEquipmentItem(_EQUIPMENT_ID)).thenReturn(item);
		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.CHANGE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());

	}

	@Test
	void shouldShowRemovePageCorrectly() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, REMOVE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(EquipmentController.ADVISOR_REMOVE_EQUIPMENT, modelAndView.getViewName());

	}

	@Test
	void shouldShowErrorPageForUnknownAction() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	 

	@Test
	void shouldShowEquipmentSummaryPageCorrectlyForBackAction() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldShowDashboardPageCorrectlyFor_DASHBOARD_ACTION() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void shouldRemoveItemSuccessfully() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setRemoveItem(YesNoType.YES.name());
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + EQUIPMENT_SUMMARY_PATH, modelAndView.getViewName());
		Mockito.verify(equipmentService, times(1)).deleteItem(Mockito.anyLong());

	}

	@Test
	void shouldValidateRemoveItemFormSuccessfully() throws Exception {

		RemoveItemFormVO formVO = new RemoveItemFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.REMOVE_ITEM_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.REMOVE_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertEquals(EquipmentController.ADVISOR_REMOVE_EQUIPMENT, modelAndView.getViewName());
		assertTrue(hasValue(modelAndView, "allowances.remove.option.required"));
		Mockito.verify(equipmentService, times(0)).deleteItem(Mockito.anyLong());

	}

	private void mockEquipment(List<EquipmentAllowanceVO> list) {
		Mockito.when(equipmentService.getAllEquipmentAllowances(UnitTestHelper.DSA_APPLICATION_NO)).thenReturn(list);
	}

	private ModelAndView mockCostCall(String value) throws Exception {
		mockStudentFirstName(findStudentService);

		AddEquipmentFormVO formVO = mockFormHappyRequest();
		formVO.setCost(value);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(EquipmentController.ADD_EQUIPMENT_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, EquipmentController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, EquipmentController.ADD_EQUIPMENT_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(EquipmentController.ADVISOR_ADD_EQUIPMENT_PAGE, modelAndView.getViewName());
		return modelAndView;
	}
	
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private void mockDSAApplicationMade() throws IllegalAccessException {
		DSAApplicationsMade dsaApplicationsMade = new DSAApplicationsMade();
		dsaApplicationsMade.setSessionCode(0);
		ApplicationResponse applicationResponse = new ApplicationResponse();
		Mockito.when(applicationService.findByDsaApplicationNumberAndStudentReferenceNumber(DSA_APPLICATION_NO, STUDENT_REFERENCE_NUMBER))
				.thenReturn(dsaApplicationsMade);
		Mockito.when(applicationService.findApplication(DSA_APPLICATION_NO, STUDENT_REFERENCE_NUMBER))
				.thenReturn(applicationResponse);
	}
}
