/**
 * 
 */
package uk.gov.saas.dsa.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.allowances.ConsumablesService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.consumables.*;
import uk.gov.saas.dsa.web.controller.allowances.ConsumablesController;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static uk.gov.saas.dsa.UnitTestHelper.hasKeyValue;
import static uk.gov.saas.dsa.UnitTestHelper.hasValue;
import static uk.gov.saas.dsa.UnitTestHelper.performPostMap;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_DASHBOARD_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.BACK_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CANCEL_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CHANGE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CONSUMABLES_CAP;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CONSUMABLES_SUMMARY_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.CONSUMABLES_SUMMARY_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.DASHBOARD_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.DISABILITY_DETAILS_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.INIT_CONSUMABLES_PATH;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REDIRECT;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REMOVE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SAVE_AND_CONTINUE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_FIRST_NAME;


@ActiveProfiles({ "localdev" })
@SpringBootTest
@AutoConfigureMockMvc
class ConsumablesControllerTest {

	private static final String CONSUMABLE_COST_INVALID_RANGE = "consumable.cost.invalid.range";
	private static final String CONSUMABLE_COST_INVALID = "consumable.cost.invalid";
	private static final long CONSUMABLE_ITEM_ID = 1111L;
	private static final String BLAH_ACTION = "blahAction";
	private static final String ADD_CONSUMABLE_FORM_VO = "addConsumableFormVO";
	private static final ResultMatcher IS3XX_REDIRECTION = MockMvcResultMatchers.status().is3xxRedirection();
	private static final ResultMatcher IS2XX_SUCCESSFUL = MockMvcResultMatchers.status().is2xxSuccessful();
	private static final String FIRST_NAME = "FIRST_NAME";
	private static final long STUDENT_REFERENCE_NUMBER = 12222L;
	private static final long DSA_APPLICATION_NO = 1L;
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@MockitoBean
	private ConsumablesService consumablesService;

	@MockitoBean
	private FindStudentService findStudentService;

	@MockitoBean
	private ApplicationService applicationService;

	@BeforeEach
	void setUp() throws Exception {
		DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.webAppContext);

		this.mockMvc = builder.build();
		mockSecurityContext();
	}



	@Test
	void shouldHaveSkipConsumableLinkEnabled() throws Exception {

		mockConsumables(Collections.emptyList());
		mockStudentFirstName();

		ApplicationKeyDataFormVO formVO = new ApplicationKeyDataFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, "ADD_FROM_ALL_SUMMARY");
		ModelAndView modelAndView = performPostMap(this.mockMvc, DSAConstants.INIT_CONSUMABLES_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertTrue(hasKeyValue(modelAndView, STUDENT_FIRST_NAME, FIRST_NAME));

		assertTrue(hasKeyValue(modelAndView, ConsumablesController.SHOW_SKIP_CONSUMABLES, Boolean.TRUE.toString()));

		assertEquals(ConsumablesController.CONSUMABLES_SELCECTION_PAGE, modelAndView.getViewName());

	}

	@Test
	void shouldNotHaveSkipConsumableLinkIfThereAreConsumablesForTheStudent() throws Exception {

		mockConsumables(Collections.singletonList(new ConsumableTypeVO()));

		mockStudentFirstName();

		ApplicationKeyDataFormVO formVO = new ApplicationKeyDataFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, "ADD_FROM_ALL_SUMMARY");
		ModelAndView modelAndView = performPostMap(this.mockMvc, DSAConstants.INIT_CONSUMABLES_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);

		assertTrue(hasKeyValue(modelAndView, STUDENT_FIRST_NAME, FIRST_NAME));

		assertTrue(hasKeyValue(modelAndView, ConsumablesController.SHOW_SKIP_CONSUMABLES, Boolean.FALSE.toString()));

		assertEquals(ConsumablesController.CONSUMABLES_SELCECTION_PAGE, modelAndView.getViewName());
	}

	@Test
	void selectConsumables_shouldRedirectToErrorScreenIfActionIsInvalid() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		String errorMessage = String.format("Calling the {%s} service with Unknown action: {%s}", "", BLAH_ACTION);
		assertTrue(hasValue(modelAndView, errorMessage));
		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void selectConsumables_shouldRedirectToDisabilityPageOnBackAction() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BACK_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);

		Mockito.verify(consumablesService, times(1)).getAllConsumableItems(formVO.getDsaApplicationNumber());

		assertEquals(REDIRECT + DISABILITY_DETAILS_PATH, modelAndView.getViewName());

	}

	@Test
	void selectConsumables_shouldRedirectToSummaryPageOnBackAction() throws Exception {
		Mockito.when(consumablesService.getAllConsumableItems(DSA_APPLICATION_NO))
				.thenReturn(Collections.singletonList(new ConsumableTypeVO()));

		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BACK_ACTION);

		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);

		Mockito.verify(consumablesService, times(1)).getAllConsumableItems(formVO.getDsaApplicationNumber());

		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void selectConsumables_shouldRedirectToSelectionScreenIfFormHasErrors() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, "You must select a consumable item."));

		assertTrue(hasKeyValue(modelAndView, ConsumablesController.SHOW_SKIP_CONSUMABLES, Boolean.TRUE.toString()));

		assertTrue(hasKeyValue(modelAndView, CONSUMABLES_CAP, "1,725.00"));
		assertEquals(ConsumablesController.CONSUMABLES_SELCECTION_PAGE, modelAndView.getViewName());
	}

	@Test
	void selectConsumables_shouldValidateDescriptionMandatoryError() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();
		formVO.setConsumableItemCodes(Collections.singletonList(ConsumableItem.OTHER.name()));
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, "consumable.otherDescription.required"));
	}

	@Test
	void selectConsumables_shouldValidateDescriptionInvalidError() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();
		formVO.setConsumableItemCodes(Collections.singletonList(ConsumableItem.OTHER.name()));
		formVO.setOtherDescription("££££");

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, "consumable.otherDescription.invalid"));
	}

	@Test
	void selectConsumables_shouldRedirectToSummaryPageOnSkipAction() throws Exception {
		ConsumableTypeFormVO formVO = new ConsumableTypeFormVO();
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLES_TYPE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.SELECT_CONSUMABLES_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());

	}

	@Test
	void addConsumables_shouldRedirectToErrorPageForInvalidAction() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	 
	@Test
	void addConsumables_shouldRedirectToErrorPageIfNoKeyIdsAndInvalidAction() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void addConsumables_shouldRedirectToSummaryPageOnDashboardAction() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());

	}

	@Test
	void addConsumables_shouldRedirectToInitConsumablesPageOnCANCEL_ACTION() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CANCEL_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		assertEquals(REDIRECT + INIT_CONSUMABLES_PATH, modelAndView.getViewName());

	}

	@Test
	void addConsumables_shouldValidateCostForEachItemOn_ADD_ACTION() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItems(Arrays.asList(setConsumableItem(ConsumableItem.PAPER, "1"),
				setConsumableItem(ConsumableItem.OTHER, "0")));
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, ConsumablesController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertTrue(hasValue(modelAndView, CONSUMABLE_COST_INVALID_RANGE));
		assertEquals(ConsumablesController.ADVISOR_ADD_CONSUMABLES_PAGE, modelAndView.getViewName());
	}

	@Test
	void addConsumables_shouldShowCostRequired() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItems(Arrays.asList(setConsumableItem(ConsumableItem.PAPER, "1"),
				setConsumableItem(ConsumableItem.OTHER, "")));
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, ConsumablesController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertTrue(hasValue(modelAndView, "consumable.cost.required"));
		assertEquals(ConsumablesController.ADVISOR_ADD_CONSUMABLES_PAGE, modelAndView.getViewName());
	}

	@Test
	void addConsumables_shouldAddConsumablesSuccessfully() throws Exception {
		AddConsumableFormVO formVO = new AddConsumableFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItems(Arrays.asList(setConsumableItem(ConsumableItem.PAPER, "1.00"),
				setConsumableItem(ConsumableItem.OTHER, "1.00")));

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ADD_CONSUMABLE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, ConsumablesController.ADD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.ADD_CONSUMABLES_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		Mockito.verify(consumablesService, times(1)).addConsumables(DSA_APPLICATION_NO, STUDENT_REFERENCE_NUMBER,
				formVO.getConsumableItems());

		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void changeItem_shouldShoeChangePageCorrectly() throws Exception {
		mockConsumableItemInDB();

		Map<String, Object> flashMap = new HashMap<>();
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("consumableItemId", "12");
		paramMap.put(ACTION, "change");
		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ConsumablesController.ADVISOR_CHANGE_CONSUMABLE_PAGE, modelAndView.getViewName());

	}

	@Test
	void changeConsumable_shouldShowErrorPageForInvalidAction() throws Exception {
		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");
		formVO.setCost("1");
		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());
	}

	@Test
	void changeConsumable_shouldShowDashboardPage() throws Exception {
		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");
		formVO.setCost("1");
		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());
	}

	@Test
	void changeConsumable_shouldShowSummaryPageOnCancelAction() throws Exception {
		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");
		formVO.setCost("1");
		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CANCEL_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void changeConsumable_shouldShowChangePageIfAnyErrors() throws Exception {

		verifyCostRangeError("0", CONSUMABLE_COST_INVALID_RANGE);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostIsMoreThanExpectedValue() throws Exception {

		verifyCostRangeError("999999.99", CONSUMABLE_COST_INVALID_RANGE);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostIsLessThanExpectedValueWith_d() throws Exception {

		verifyCostRangeError("-12d", CONSUMABLE_COST_INVALID);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostIsLessThanExpectedValueWith_D() throws Exception {

		verifyCostRangeError("-12D", CONSUMABLE_COST_INVALID);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostIsLessThanExpectedValue() throws Exception {

		verifyCostRangeError("-12", CONSUMABLE_COST_INVALID);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostInvalidFormat() throws Exception {

		verifyCostRangeError(".001", CONSUMABLE_COST_INVALID);
	}

	@Test
	void changeConsumable_shouldShowChangePageIfCostInvalidValue() throws Exception {

		verifyCostRangeError("A", CONSUMABLE_COST_INVALID);
	}

	private void verifyCostRangeError(String cost, String message) throws Exception {
		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		mockConsumableItemInDB();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");
		formVO.setCost(cost);
		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, message));
		assertEquals(ConsumablesController.ADVISOR_CHANGE_CONSUMABLE_PAGE, modelAndView.getViewName());
	}

	@Test
	void changeConsumable_shouldShowChangePageWithCostRequired() throws Exception {

		mockConsumableItemInDB();

		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");

		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, "consumable.cost.required"));
		assertEquals(ConsumablesController.ADVISOR_CHANGE_CONSUMABLE_PAGE, modelAndView.getViewName());
	}

	@Test
	void changeConsumable_shouldShowChangePageWithCostInvalid() throws Exception {

		mockConsumableItemInDB();

		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setCost("a");
		formVO.setDescription("desc");

		formVO.setId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, CONSUMABLE_COST_INVALID));
		assertEquals(ConsumablesController.ADVISOR_CHANGE_CONSUMABLE_PAGE, modelAndView.getViewName());
	}

	private void mockConsumableItemInDB() throws IllegalAccessException {
		ConsumableTypeVO consumableTypeVO = new ConsumableTypeVO();
		consumableTypeVO.setConsumableItem(ConsumableItem.OTHER);
		consumableTypeVO.setCost(BigDecimal.ONE);

		Mockito.when(consumablesService.getConsumableItem(Mockito.anyLong())).thenReturn(consumableTypeVO);
	}

	@Test
	void changeConsumable_shouldChangeItemCorrectly() throws Exception {

		ConsumableItemChangeFormVO formVO = new ConsumableItemChangeFormVO();
		formVO.setConsumableItem(ConsumableItem.OTHER);
		formVO.setDescription("desc");
		formVO.setCost("1");
		formVO.setId(CONSUMABLE_ITEM_ID);

		ConsumableTypeVO result = new ConsumableTypeVO();
		result.setConsumableItem(ConsumableItem.OTHER);
		result.setCost(BigDecimal.ONE);
		result.setCostString("1");
		result.setDsaApplicationNumber(DSA_APPLICATION_NO);
		result.setId(CONSUMABLE_ITEM_ID);
		result.setOtehrItemText("text");
		result.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Mockito.when(consumablesService.updateConsumableItem(formVO)).thenReturn(result);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put("consumableItemChangeFormVO", formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CHANGE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.CHANGE_CONSUMABLE_ITEM_PATH,
				flashMap, paramMap, IS3XX_REDIRECTION);
		Mockito.verify(consumablesService, times(1)).updateConsumableItem(formVO);

		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());
	}

	@Test
	void removeItem_shouldShowErrorPageForInvalidAction() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, BLAH_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ERROR_PAGE, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldShowCorrectPageForREMOVE_ACTIONAction() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, REMOVE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);

		assertEquals(ConsumablesController.REMOVE_CONSUMABLE_PAGE, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldShowDashboardAction() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, DASHBOARD_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + APPLICATION_DASHBOARD_PATH, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldShowSummaryPageAction() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CONSUMABLES_SUMMARY_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);

		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldShowSelectAnOptionErrorMessage() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);
		formVO.setConsumableItemId(CONSUMABLE_ITEM_ID);

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS2XX_SUCCESSFUL);
		assertTrue(hasValue(modelAndView, "consumable.remove.option.required"));
		assertEquals(ConsumablesController.REMOVE_CONSUMABLE_PAGE, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldRemoveItemSuccessfully() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);
		formVO.setConsumableItemId(CONSUMABLE_ITEM_ID);
		formVO.setRemoveItem(YesNoType.YES.name());
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		Mockito.verify(consumablesService, times(1)).deleteItem(Mockito.anyLong());
		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void removeItem_shouldNotRemove_itemIfUserSelectsNo() throws Exception {
		ConsumableItemRemoveFormVO formVO = new ConsumableItemRemoveFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		formVO.setConsumableItem(ConsumableItem.PAPER);
		formVO.setConsumableItemId(CONSUMABLE_ITEM_ID);
		formVO.setRemoveItem(YesNoType.NO.name());
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(ConsumablesController.CONSUMABLE_ITEM_REMOVE_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, SAVE_AND_CONTINUE_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, ConsumablesController.REMOVE_CONSUMABLE_PATH, flashMap,
				paramMap, IS3XX_REDIRECTION);
		Mockito.verify(consumablesService, times(0)).deleteItem(Mockito.anyLong());
		assertEquals(REDIRECT + CONSUMABLES_SUMMARY_PATH, modelAndView.getViewName());

	}

	@Test
	void consumableSummary_shouldShowSummaryPage() throws Exception {
		mockStudentFirstName();
		ApplicationKeyDataFormVO formVO = mockKeyData();

		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CONSUMABLES_SUMMARY_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, CONSUMABLES_SUMMARY_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(ConsumablesController.ADVISOR_CONSUMABLES_SUMMARY_PAGE, modelAndView.getViewName());

	}

	@Test
	void consumableSummary_shouldHaveExceedsMessage() throws Exception {
		mockStudentFirstName();

		List<ConsumableTypeVO> items = new ArrayList<>();
		for (ConsumableItem item : ConsumableItem.values()) {
			ConsumableTypeVO consumableTypeVO = new ConsumableTypeVO();
			consumableTypeVO.setConsumableItem(item);
			consumableTypeVO.setCost(BigDecimal.valueOf(500));
			items.add(consumableTypeVO);
		}

		mockConsumables(items);

		ApplicationKeyDataFormVO formVO = mockKeyData();
		Map<String, Object> flashMap = new HashMap<>();
		flashMap.put(APPLICATION_KEY_DATA_FORM_VO, formVO);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(ACTION, CONSUMABLES_SUMMARY_ACTION);

		ModelAndView modelAndView = performPostMap(this.mockMvc, CONSUMABLES_SUMMARY_PATH, flashMap, paramMap,
				IS2XX_SUCCESSFUL);
		assertEquals(ConsumablesController.ADVISOR_CONSUMABLES_SUMMARY_PAGE, modelAndView.getViewName());
		assertTrue(hasKeyValue(modelAndView, AllowancesHelper.CONSUMABLES_TOTAL, "2,500.00"));
		assertTrue(hasKeyValue(modelAndView, AllowancesHelper.CONSUMABLES_COUNT, "" + ConsumableItem.values().length));
		assertTrue(hasKeyValue(modelAndView, AllowancesHelper.CONSUMABLE_ITEMS, "" + items));
		assertTrue(hasKeyValue(modelAndView, AllowancesHelper.HIDE_ADD_ANOTHER, "true"));
		assertTrue(hasKeyValue(modelAndView, CONSUMABLES_CAP, "1,725.00"));
		assertTrue(hasKeyValue(modelAndView, AllowancesHelper.CONSUMABLES_TOTAL_EXCEEDED, "true"));

	}

 
	private ApplicationKeyDataFormVO mockKeyData() {
		ApplicationKeyDataFormVO formVO = new ApplicationKeyDataFormVO();
		formVO.setDsaApplicationNumber(DSA_APPLICATION_NO);
		formVO.setStudentReferenceNumber(STUDENT_REFERENCE_NUMBER);
		return formVO;
	}

	private ConsumableItemFormVO setConsumableItem(ConsumableItem item, String cost) {

		ConsumableItemFormVO vo = new ConsumableItemFormVO();
		vo.setConsumableItem(item);
		vo.setCost(cost);
		return vo;
	}

	private void mockStudentFirstName() throws Exception {
		StudentResultVO studResult = new StudentResultVO();
		studResult.setFirstName(FIRST_NAME);

		Mockito.when(findStudentService.findByStudReferenceNumber(STUDENT_REFERENCE_NUMBER)).thenReturn(studResult);

	}

	private void mockConsumables(List<ConsumableTypeVO> list) {
		Mockito.when(consumablesService.getAllConsumableItems(DSA_APPLICATION_NO)).thenReturn(list);
	}
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);
 
		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

}
