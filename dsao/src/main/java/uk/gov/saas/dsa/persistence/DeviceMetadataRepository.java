package uk.gov.saas.dsa.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.DeviceMetadata;

@Repository("DeviceMetaDataRepo")
public interface DeviceMetadataRepository extends CrudRepository<DeviceMetadata, String> {

	List<DeviceMetadata> findByUserId(String userId);
	
	List<DeviceMetadata> findByEmailIdIgnoreCase(String emailId);
	
	DeviceMetadata findByDeviceVerificationToken(String emailId);
}
