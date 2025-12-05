package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.DeclarationType;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.persistence.DeclarationTypeRepository;
import uk.gov.saas.dsa.vo.DeclarationTypeVO;

import java.util.ArrayList;
import java.util.List;

/**
 * DSA Declarations Service
 */
@Service
public class DeclarationsService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DeclarationTypeRepository declarationTypeRepository;
	private ApplicationService applicationService;

	/**
	 * DeclarationsService constructor
	 * 
	 * @param declarationTypeRepository
	 * @param applicationService
	 */
	public DeclarationsService(DeclarationTypeRepository declarationTypeRepository,
			ApplicationService applicationService) {
		this.declarationTypeRepository = declarationTypeRepository;
		this.applicationService = applicationService;
	}

	/**
	 * Find all declaration types
	 * 
	 * @param userType
	 * @return list of Declarations types
	 */
	public List<DeclarationTypeVO> findAllActiveDeclarations(String userType) {
		logger.info("getting the declaration for :{}", userType);
		List<DeclarationTypeVO> declarations = new ArrayList<>();
		List<DeclarationType> declarationTypeList = declarationTypeRepository
				.findByDeclarationIgnoreCaseForAndIsActiveIgnoreCase(userType, DisabilitiesService.YES);
		declarationTypeList.forEach(declarationType -> {
			DeclarationTypeVO declarationTypeVO = new DeclarationTypeVO();
			declarationTypeVO.setDeclarationTypeId(declarationType.getDeclarationTypeId());
			declarationTypeVO.setDeclarationCode(declarationType.getDeclarationTypeCode());
			declarationTypeVO.setDeclarationTypeDesc(declarationType.getDeclarationTypeDesc());
			declarations.add(declarationTypeVO);
		});
		logger.info("Declarations for {} are {}", userType, declarations);
		return declarations;
	}

	/**
	 * Save advisor declarations
	 * 
	 * @param dsaApplicationNumber
	 * @throws IllegalAccessException
	 */
	public void saveAdvisorDeclarations(long dsaApplicationNumber) throws IllegalAccessException {
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.ADVISOR_DECLARATION,
				SectionStatus.COMPLETED);
		
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.NEEDS_ASSESSMENT_FEE,
				SectionStatus.COMPLETED);
		
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.STUDENT_DECLARATION,
				SectionStatus.NOT_STARTED);
		 
	}

	public void saveStudentDeclarations(long dsaApplicationNumber) throws IllegalAccessException {
		ServiceUtil.updateSectionStatus(applicationService, dsaApplicationNumber, Section.STUDENT_DECLARATION,
				SectionStatus.STARTED);
		 
	}
}
