package uk.gov.saas.dsa.service.allowances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.DSAApplicationStudConsumables;
import uk.gov.saas.dsa.model.ConsumableItem;
import uk.gov.saas.dsa.persistence.DSAApplicationStudConsumablesRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemChangeFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableItemFormVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ConsumablesServiceTest {
	private static final String COST = "12";
	private static final long CONSUMABLE_ITEM_ID = 21l;
	private static final long STUDENT_REF_NO = 12l;

	private static final long DSA_APPLICATION_NUMBER = 233l;
	@MockitoBean
	private DSAApplicationStudConsumablesRepository consumablesRepository;
	@MockitoBean
	private ApplicationService applicationService;
	private ConsumablesService subject;

	@Captor
	private ArgumentCaptor<ArrayList<DSAApplicationStudConsumables>> stdConsumablesAddCaptor;

	@BeforeEach
	void setUp() {
		subject = new ConsumablesService(applicationService, consumablesRepository);
		mockSecurityContext();
	}

	@Test
	public void shouldAddGivenConsumablesSuccesfully() throws IllegalAccessException {
		List<ConsumableItemFormVO> consumableTypes = Arrays
				.asList(addConsumableItem(ConsumableItem.INK_CARTRIDGE, COST));

		Mockito.when(consumablesRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER))
				.thenReturn(new ArrayList<>());

		subject.addConsumables(DSA_APPLICATION_NUMBER, STUDENT_REF_NO, consumableTypes);

		verify(consumablesRepository, times(1)).saveAll(Mockito.anyList());

		verify(consumablesRepository).saveAll(stdConsumablesAddCaptor.capture());

		ArrayList<DSAApplicationStudConsumables> savedConsumables = stdConsumablesAddCaptor.getValue();
		Assertions.assertEquals(1, savedConsumables.size());
		DSAApplicationStudConsumables savedItem = getSavedItem(savedConsumables, ConsumableItem.INK_CARTRIDGE);
		Assertions.assertEquals(BigDecimal.valueOf(12.0), savedItem.getCost());
		Assertions.assertEquals(LoggedinUserUtil.getUserId(), savedItem.getLastUpdatedBy());

	}

	@Test
	public void shouldUpdateConsumablesSuccesfullyForAnExitingConsumableItem() throws IllegalAccessException {
		List<ConsumableItemFormVO> consumableTypes = Arrays
				.asList(addConsumableItem(ConsumableItem.INK_CARTRIDGE, COST));

		mockExistingConsumableItems();

		subject.addConsumables(DSA_APPLICATION_NUMBER, STUDENT_REF_NO, consumableTypes);

		verify(consumablesRepository, times(1)).saveAll(Mockito.anyList());
		verify(consumablesRepository).saveAll(stdConsumablesAddCaptor.capture());
		ArrayList<DSAApplicationStudConsumables> savedConsumables = stdConsumablesAddCaptor.getValue();
		Assertions.assertEquals(1, savedConsumables.size());
		DSAApplicationStudConsumables savedItem = getSavedItem(savedConsumables, ConsumableItem.INK_CARTRIDGE);
		Assertions.assertEquals(BigDecimal.valueOf(12.0), savedItem.getCost());
		Assertions.assertEquals(LoggedinUserUtil.getUserId(), savedItem.getLastUpdatedBy());

	}

	@Test
	public void shouldGetAllConsumableItems() {
		mockExistingConsumableItems();
		List<ConsumableTypeVO> allConsumableItems = subject.getAllConsumableItems(DSA_APPLICATION_NUMBER);
		Assertions.assertEquals(1, allConsumableItems.size());

	}

	@Test
	public void shouldGetSpecificItemById() throws IllegalAccessException {
		DSAApplicationStudConsumables existingConsumableItem = mockConsumableItem();
		Mockito.when(consumablesRepository.findById(CONSUMABLE_ITEM_ID)).thenReturn(Optional.of(existingConsumableItem));
		ConsumableTypeVO item = subject.getConsumableItem(CONSUMABLE_ITEM_ID);
		Assertions.assertEquals(CONSUMABLE_ITEM_ID, item.getId());

		verify(consumablesRepository, times(1)).findById(CONSUMABLE_ITEM_ID);
	}

	@Test
	public void shouldUpdateConsumableItem() throws IllegalAccessException {
		DSAApplicationStudConsumables mockConsumableItem = mockConsumableItem();

		Mockito.when(consumablesRepository.save(mockConsumableItem)).thenReturn(mockConsumableItem);

		ConsumableItemChangeFormVO itemFormVO = new ConsumableItemChangeFormVO();
		itemFormVO.setId(CONSUMABLE_ITEM_ID);
		itemFormVO.setCost(COST);

		ConsumableTypeVO updatedItem = subject.updateConsumableItem(itemFormVO);
		Assertions.assertEquals(CONSUMABLE_ITEM_ID, updatedItem.getId());
		verify(consumablesRepository, times(1)).findById(CONSUMABLE_ITEM_ID);
		verify(consumablesRepository, times(1)).save(Mockito.any(DSAApplicationStudConsumables.class));
	}

	@Test
	public void shouldDeleteGivenConsumableItem() {

		subject.deleteItem(CONSUMABLE_ITEM_ID);
		verify(consumablesRepository, times(1)).deleteById(CONSUMABLE_ITEM_ID);

	}

	@Test
	void shouldThrowExceptionIfItemAlreadyDeleted() {

		Mockito.doThrow(EmptyResultDataAccessException.class).when(consumablesRepository).deleteById(CONSUMABLE_ITEM_ID);
		subject.deleteItem(CONSUMABLE_ITEM_ID);
		verify(consumablesRepository, times(1)).deleteById(CONSUMABLE_ITEM_ID);

	}

	@Test
	void shouldThrowExceptionIfNoItemFound() {
		IllegalAccessException thrown = Assertions.assertThrows(IllegalAccessException.class, () -> {
			subject.getConsumableItem(CONSUMABLE_ITEM_ID);
		});
		Assertions.assertEquals("No Consumable item found for id:21", thrown.getMessage());

	}

	private void mockExistingConsumableItems() {
		DSAApplicationStudConsumables existingConsumableItem = mockConsumableItem();

		Mockito.when(consumablesRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER))
				.thenReturn(Arrays.asList(existingConsumableItem));
	}

	private DSAApplicationStudConsumables mockConsumableItem() {
		DSAApplicationStudConsumables existingConsumableItem = new DSAApplicationStudConsumables();
		existingConsumableItem.setId(CONSUMABLE_ITEM_ID);
		existingConsumableItem.setConsumabelItem(ConsumableItem.INK_CARTRIDGE);
		existingConsumableItem.setCost(BigDecimal.ONE);
		Mockito.when(consumablesRepository.findById(CONSUMABLE_ITEM_ID)).thenReturn(Optional.of(existingConsumableItem));
		return existingConsumableItem;
	}

	private DSAApplicationStudConsumables getSavedItem(ArrayList<DSAApplicationStudConsumables> savedConsumables,
			ConsumableItem item) {
		Optional<DSAApplicationStudConsumables> findFirst = savedConsumables.stream()
				.filter(t -> t.getConsumabelItem().equals(item)).findFirst();
		return findFirst.orElse(null);
	}

	private ConsumableItemFormVO addConsumableItem(ConsumableItem item, String cost) {
		ConsumableItemFormVO form = new ConsumableItemFormVO();
		form.setCost(cost);
		form.setConsumableItem(item);
		form.setDescription(item.equals(ConsumableItem.OTHER) ? "Some consumable item desc" : null);
		return form;
	}
	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
