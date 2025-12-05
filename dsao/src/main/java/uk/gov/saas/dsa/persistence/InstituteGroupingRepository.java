package uk.gov.saas.dsa.persistence;

import org.springframework.data.repository.CrudRepository;

import uk.gov.saas.dsa.domain.InstituteGrouping;

public interface InstituteGroupingRepository extends CrudRepository<InstituteGrouping, String> {

	InstituteGrouping findByInstCode(String instCode);

}
