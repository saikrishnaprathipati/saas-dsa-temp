package uk.gov.saas.dsa.persistence.refdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.refdata.Country;

@Repository("countryRepo")
public interface CountryRepository extends JpaRepository<Country, Long> {

	@Query(value = "select * from country where is_active='Y' order by case when long_name = 'UNITED KINGDOM' then 1 "
			+ "when long_name = 'SCOTLAND' then 2 when long_name = 'ENGLAND' then 3 when long_name = 'NORTHERN IRELAND' then 4 "
			+ "when long_name = 'WALES' then 5 else 6 end, long_name asc", 
			nativeQuery = true)
	List<Country> findByIsActive();
}
