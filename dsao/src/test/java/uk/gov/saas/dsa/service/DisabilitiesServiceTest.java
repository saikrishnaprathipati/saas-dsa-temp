package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.saas.dsa.domain.DSAApplicationStudDisabilities;
import uk.gov.saas.dsa.domain.DisabilityType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DSAApplicationStudDisabilitiesRepository;
import uk.gov.saas.dsa.persistence.DisabilityTypeRepository;
import uk.gov.saas.dsa.vo.DisabilityTypeVO;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class DisabilitiesServiceTest {
	private static final long DSA_APPLICATION_NUMBER_1 = 1L;
	private static final long ID1 = 1L;
	private static final long ID2 = 2L;
	@MockitoBean
	private DisabilityTypeRepository disabilityTypeRepository;
	@MockitoBean
	private DSAApplicationStudDisabilitiesRepository applicationStudDisabilitiesRepository;
	@MockitoBean
	private ApplicationService applicationService;

	private DisabilitiesService subject;

	@BeforeEach
	public void setUp() throws Exception {
		subject = new DisabilitiesService(disabilityTypeRepository, applicationStudDisabilitiesRepository,
				applicationService);
	}

	@Test
	void shouldReturnAllActiveDisabilityTypes() {
		DisabilityType disabilityType1 = mockDisabilityType(DisabilityTypeVO.DISABILITY_NOT_LISTED);
		DisabilityType disabilityType2 = mockDisabilityType("BLAH_CODE");
		Mockito.when(disabilityTypeRepository.findByIsActiveIgnoreCase("yes"))
				.thenReturn(Arrays.asList(disabilityType1, disabilityType2));

		List<DisabilityTypeVO> disabilityTypes = subject.getActiveDisabilityTypes();
		Assertions.assertEquals(2, disabilityTypes.size());
	}

	private DisabilityType mockDisabilityType(String disabilityTypeCode) {
		DisabilityType disabilityType = new DisabilityType();
		disabilityType.setDisabilityTypeCode(disabilityTypeCode);
		return disabilityType;
	}

	@Test
	void shouldReturnApplicationDisabilities() {

		DisabilityType disabilityType1 = mockDisabilityType(DisabilityTypeVO.DISABILITY_NOT_LISTED);
		DisabilityType disabilityType2 = mockDisabilityType("BLAH_CODE");

		Mockito.when(disabilityTypeRepository.findAllByDisabilityTypeCodeIn(Mockito.anyList()))
				.thenReturn(Arrays.asList(disabilityType1, disabilityType2));

		mockApplicationDisabilities();

		List<DisabilityTypeVO> disabilityTypes = subject.populateApplicationDisabilities(DSA_APPLICATION_NUMBER_1);
		Assertions.assertEquals(2, disabilityTypes.size());
	}

	@Test
	void shouldReturnApplicationDisabilitiesIfAccessingFirstTime() {
		Mockito.when(disabilityTypeRepository.findAllByDisabilityTypeCodeIn(Mockito.anyList()))
				.thenReturn(Arrays.asList());

		mockApplicationDisabilities();

		List<DisabilityTypeVO> disabilityTypes = subject.populateApplicationDisabilities(DSA_APPLICATION_NUMBER_1);
		Assertions.assertEquals(0, disabilityTypes.size());
	}

	private void mockApplicationDisabilities() {
		DSAApplicationStudDisabilities studDisabilities1 = mockDisability(ID1, DSA_APPLICATION_NUMBER_1,
				DisabilityTypeVO.DISABILITY_NOT_LISTED, "SOME_TEXT");
		DSAApplicationStudDisabilities studDisabilities2 = mockDisability(ID2, DSA_APPLICATION_NUMBER_1, "BLAH_CODE",
				null);

		Mockito.when(applicationStudDisabilitiesRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER_1))
				.thenReturn(Arrays.asList(studDisabilities1, studDisabilities2));
	}

	@Test
	void shouldSaveUserSelectedDisabilities() throws IllegalAccessException {
		mockApplicationDisabilities();
		subject.saveDisabilities(DSA_APPLICATION_NUMBER_1, DSA_APPLICATION_NUMBER_1,
				Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED), "NOT LISTED TEXT");

		verify(applicationStudDisabilitiesRepository, times(1)).deleteAll(Mockito.anyList());
		verify(applicationStudDisabilitiesRepository, times(1)).saveAll(Mockito.anyList());
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.DISABILITIES,
				SectionStatus.COMPLETED);
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.ALLOWANCES,
				SectionStatus.NOT_STARTED);
		 

	}

	@Test
	void shouldSaveUserSelectedDisabilitiesWhenFirstAccessed() throws IllegalAccessException {

		subject.saveDisabilities(DSA_APPLICATION_NUMBER_1, DSA_APPLICATION_NUMBER_1,
				Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED), "NOT LISTED TEXT");
		verify(applicationStudDisabilitiesRepository, times(0)).deleteAll(Mockito.anyList());
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.DISABILITIES,
				SectionStatus.COMPLETED);
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.ALLOWANCES,
				SectionStatus.NOT_STARTED);
		 

	}

	@Test
	void shouldSaveIfUserDataAndSavedDataAreSame() throws IllegalAccessException {
		DSAApplicationStudDisabilities studDisabilities1 = mockDisability(ID1, DSA_APPLICATION_NUMBER_1,
				DisabilityTypeVO.DISABILITY_NOT_LISTED, "SOME_TEXT");
		Mockito.when(applicationStudDisabilitiesRepository.findByDsaApplicationNumber(DSA_APPLICATION_NUMBER_1))
				.thenReturn(Arrays.asList(studDisabilities1));

		subject.saveDisabilities(DSA_APPLICATION_NUMBER_1, DSA_APPLICATION_NUMBER_1,
				Arrays.asList(DisabilityTypeVO.DISABILITY_NOT_LISTED), "SOME_TEXT");

		verify(applicationStudDisabilitiesRepository, times(0)).deleteAll(Mockito.anyList());
		verify(applicationStudDisabilitiesRepository, times(0)).saveAll(Mockito.anyList());
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.DISABILITIES,
				SectionStatus.COMPLETED);
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.ALLOWANCES,
				SectionStatus.NOT_STARTED);
	 

	}

	private DSAApplicationStudDisabilities mockDisability(long id, long dsaApplicationNumber, String disabilityCode,
			String notListedText) {
		DSAApplicationStudDisabilities studDisabilities = new DSAApplicationStudDisabilities();
		studDisabilities.setId(id);
		studDisabilities.setDsaApplicationNumber(dsaApplicationNumber);
		studDisabilities.setDisabilityTypeCode(disabilityCode);
		studDisabilities.setDisabilityNotlistedText(notListedText);
		return studDisabilities;
	}

}
