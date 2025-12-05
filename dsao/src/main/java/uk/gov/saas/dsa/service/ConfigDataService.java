package uk.gov.saas.dsa.service;

import static uk.gov.saas.dsa.web.helper.DSAConstants.CURRENT_SESSION;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.ConfigData;
import uk.gov.saas.dsa.persistence.ConfigDataRepository;

/**
 * To load the configuration data
 */
@Service
public class ConfigDataService {
	private static final Logger logger = LogManager.getLogger(ConfigDataService.class);
	private static ConfigDataRepository configDataRepository;

	public ConfigDataService(ConfigDataRepository configDataRepository) {
		this.configDataRepository = configDataRepository;
	}

	/**
	 * Get the ConfigData
	 * 
	 * @param itemName The configuration name
	 * @return the ConfigData.
	 */
	public static ConfigData findByItemName(String itemName) {
        return configDataRepository.findByItemName(itemName);
	}
	
	public static int getCurrentActiveSession() {
		ConfigData configData = findByItemName(CURRENT_SESSION);
		int sessionCode = configData.getNumericalValue();
		logger.info("Current Active session code is : {}",  sessionCode);
		return sessionCode;
	}
	
}
