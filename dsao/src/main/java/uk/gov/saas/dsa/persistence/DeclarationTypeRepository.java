package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.DeclarationType;

import java.util.List;

/**
 * DeclarationTypeRepository
 */
@Repository("declarationTypeRepository")
public interface DeclarationTypeRepository extends CrudRepository<DeclarationType, Long> {
 	List<DeclarationType> findByDeclarationIgnoreCaseForAndIsActiveIgnoreCase(String userType, String isActive);

}