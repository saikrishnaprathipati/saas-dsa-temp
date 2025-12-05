package uk.gov.saas.dsa.persistence;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.ConfigData;

@Repository("configDataRepository")
public interface ConfigDataRepository extends CrudRepository<ConfigData, String> {

	@Cacheable(value = "dsaServiceCache", key = "#itemName")
	public ConfigData findByItemName(String itemName);
	public ConfigData findByNumericalValue(Long nval);

}
