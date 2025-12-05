package uk.gov.saas.dsa.persistence.refdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.refdata.Nationality;

@Repository("nationalityRepo")
public interface NationalityRepository extends JpaRepository<Nationality, Long> {

	@Query(value = "select * from nationality where is_active='Y' order by case when nationality_name = 'BRITISH (UK)' then 1 "
			+ "when nationality_name = 'SCOTTISH' then 2 when nationality_name = 'ENGLISH' then 3 when nationality_name = 'WELSH' then 4 "
			+ "when nationality_name = 'IRISH (REPUBLIC)' then 5 else 6 end, nationality_name asc", 
			nativeQuery = true)
	List<Nationality> findByIsActive();
}
