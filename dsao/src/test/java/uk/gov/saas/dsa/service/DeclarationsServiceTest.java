package uk.gov.saas.dsa.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.saas.dsa.domain.DeclarationType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DeclarationTypeRepository;
import uk.gov.saas.dsa.vo.DeclarationTypeVO;

@ExtendWith(SpringExtension.class)
class DeclarationsServiceTest {
	private static final long DSA_APPLICATION_NUMBER_1 = 1l;
	@MockitoBean
	private DeclarationTypeRepository declarationTypeRepository;
	@MockitoBean
	private ApplicationService applicationService;

	private DeclarationsService subject;

	@BeforeEach
	public void setUp() throws Exception {
		subject = new DeclarationsService(declarationTypeRepository, applicationService);
	}

	@Test
	void shouldLoadAllDeclarations() {

		DeclarationType type = new DeclarationType();
		type.setDeclarationFor("advisor");

		Mockito.when(declarationTypeRepository
				.findByDeclarationIgnoreCaseForAndIsActiveIgnoreCase("advisor", "yes"))
				.thenReturn(Arrays.asList(type));
		List<DeclarationTypeVO> allActiveDeclarations = subject.findAllActiveDeclarations("advisor");
		Assertions.assertEquals(1, allActiveDeclarations.size());

	}

	@Test
	void shouldSaveAdvisorDeclarations() throws IllegalAccessException {
		subject.saveAdvisorDeclarations(DSA_APPLICATION_NUMBER_1);
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.ADVISOR_DECLARATION,
				SectionStatus.COMPLETED);
		verify(applicationService, times(1)).updateSectionStatus(DSA_APPLICATION_NUMBER_1, Section.STUDENT_DECLARATION,
				SectionStatus.NOT_STARTED);
	}

}
