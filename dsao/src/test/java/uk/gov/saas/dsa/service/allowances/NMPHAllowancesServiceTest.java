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
import uk.gov.saas.dsa.domain.DSAApplicationStudNMPH;
import uk.gov.saas.dsa.persistence.DSAApplicationStudNMPHRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class NMPHAllowancesServiceTest {

	private static final long NMPH_ID = 21334;

	private static final long DSA_APPLICATION_NUMBER = 1212;

	private NMPHAllowancesService subject;
	@Captor
	private ArgumentCaptor<DSAApplicationStudNMPH> nmphCaptor;
	@MockitoBean
	private ApplicationService applicationService;
	@MockitoBean
	private DSAApplicationStudNMPHRepository nmphRepository;

	@BeforeEach
	void setUp() {
		subject = new NMPHAllowancesService(nmphRepository, applicationService);
		mockSecurityContext();
	}

	@Test
	public void shouldGetAllNMPHAllowancesForAGivenDSAApplicationNumber() {

		DSAApplicationStudNMPH nmph = mockNMPHEntity();

		Mockito.when(nmphRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER)).thenReturn(Arrays.asList(nmph));
		List<NMPHAllowanceVO> allowances = subject.getAllNMPHAllowances(DSA_APPLICATION_NUMBER);
		assertEquals(1, allowances.size());
	}

	@Test
	public void shouldAddNMPHSuccessfully() throws IllegalAccessException {

		NMPHAllowanceVO item = mockNMPHVO();
		subject.addNMPHAllowance(item);
		verify(nmphRepository, times(1)).save(any(DSAApplicationStudNMPH.class));
	}

	@Test
	void shouldThrowExceptionIfNoItemFound() {
		IllegalAccessException thrown = assertThrows(IllegalAccessException.class, () -> {
			subject.getNMPHItem(NMPH_ID);
		});
		verify(nmphRepository, times(1)).findById(NMPH_ID);
		assertEquals("No NMPH item found for id:21334", thrown.getMessage());
	}

	@Test
	void shouldGetNMPHItemById() throws IllegalAccessException {
		Optional<DSAApplicationStudNMPH> entityItem = Optional.of(mockNMPHEntity());
		Mockito.when(nmphRepository.findById(NMPH_ID)).thenReturn(entityItem);
		NMPHAllowanceVO nmphItem = subject.getNMPHItem(NMPH_ID);
		Assertions.assertNotNull(nmphItem);
		verify(nmphRepository, times(1)).findById(NMPH_ID);
	}

	@Test
	void shouldChangeItemSuccessfully() throws IllegalAccessException {
		Optional<DSAApplicationStudNMPH> entityItem = Optional.of(mockNMPHEntity());
		Mockito.when(nmphRepository.findById(NMPH_ID)).thenReturn(entityItem);
		subject.changeNMPHItem(mockNMPHVO());
		verify(nmphRepository, times(1)).save(any(DSAApplicationStudNMPH.class));
		verify(nmphRepository).save(nmphCaptor.capture());
		DSAApplicationStudNMPH updatedEntity = nmphCaptor.getValue();
		assertEquals(LoggedinUserUtil.getUserId(), updatedEntity.getLastUpdatedBy());
	}

	@Test
	void shouldNotChangeItemIfNothingWasChanged() throws IllegalAccessException {
		DSAApplicationStudNMPH dsaApplicationStudNMPH = new DSAApplicationStudNMPH();
		dsaApplicationStudNMPH.setId(NMPH_ID);
		dsaApplicationStudNMPH.setHourlyRate(valueOf(BigDecimal.ONE.doubleValue()));
		dsaApplicationStudNMPH.setCost(valueOf(BigDecimal.ONE.doubleValue()));

		Optional<DSAApplicationStudNMPH> entityItem = Optional.of(dsaApplicationStudNMPH);

		Mockito.when(nmphRepository.findById(NMPH_ID)).thenReturn(entityItem);

		NMPHAllowanceVO mockNMPHVO = NMPHAllowanceVO.builder().id(NMPH_ID)
				.hourlyRate(valueOf(BigDecimal.ONE.doubleValue())).cost(valueOf(BigDecimal.ONE.doubleValue())).hours(0)
				.weeks(0).build();

		subject.changeNMPHItem(mockNMPHVO);
		verify(nmphRepository, times(0)).save(any(DSAApplicationStudNMPH.class));

	}

	@Test
	void shouldDeleteItemSuccessfully() {

		subject.deleteItem(NMPH_ID);
		verify(nmphRepository, times(1)).deleteById(NMPH_ID);

	}

	@Test
	void shouldThrowExceptionIfItemAlreadyDeleted() {

		Mockito.doThrow(EmptyResultDataAccessException.class).when(nmphRepository).deleteById(NMPH_ID);
		subject.deleteItem(NMPH_ID);
		verify(nmphRepository, times(1)).deleteById(NMPH_ID);

	}

	private NMPHAllowanceVO mockNMPHVO() {

		return NMPHAllowanceVO.builder().id(NMPH_ID).cost(BigDecimal.ONE).hours(1).weeks(1).hourlyRate(BigDecimal.ONE)
				.build();
	}

	private DSAApplicationStudNMPH mockNMPHEntity() {
		DSAApplicationStudNMPH dsaApplicationStudNMPH = new DSAApplicationStudNMPH();
		dsaApplicationStudNMPH.setId(NMPH_ID);
		dsaApplicationStudNMPH.setCost(BigDecimal.ONE);
		dsaApplicationStudNMPH.setHoursPerWeek(1);
		dsaApplicationStudNMPH.setWeeks(1);
		dsaApplicationStudNMPH.setHourlyRate(BigDecimal.ONE);
		dsaApplicationStudNMPH.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaApplicationStudNMPH.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		return dsaApplicationStudNMPH;
	}

	private void mockSecurityContext() {
		Authentication authentication = Mockito.mock(Authentication.class);

		Mockito.when(authentication.getPrincipal()).thenReturn("somePrincipal");

		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
