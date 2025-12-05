package uk.gov.saas.dsa.persistence.refdata;
 

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.saas.dsa.domain.refdata.Title; 
/**
 * Title repository
 * @author Z620537
 * to load the title info
 */
public interface TitleRespository extends JpaRepository<Title, Long> {
	
	Title findByLegacyCode(String legacyCode);
	
	Title findByTitleId(Long titleId);
}
