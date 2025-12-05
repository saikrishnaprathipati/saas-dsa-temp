package uk.gov.saas.dsa.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * DSA CMS Cache configuration properties
 * 
 * @author Siva Chimpiri
 *
 */
@Configuration
@ConfigurationProperties(prefix = "dsa.cache.config")
@Data
public class DSACacheConfigProperties {

	/**
	 * Minimum cache size, the default value is 10
	 */
	private int sizeMB = 10;
	/**
	 * Cache timeToIdleExpiration time in seconds, the default value is 300
	 */
	private int timeToIdleExpiration = 300;
	/**
	 * Cache CMS timeToLiveExpiration time in seconds
	 * <p>
	 * NOTE: 604800 sec = 1 week
	 */
	private int timeToLiveExpirationCMS = 604800;
	/**
	 * Cache Service timeToLiveExpiration time in seconds
	 * <p>
	 * NOTE: 2630000 sec = 1 month
	 */
	private int timeToLiveExpirationService = 2630000;
	/**
	 * To add all DSA CMS content to be added to cache on initialisation, the
	 * default value is true
	 */
	boolean initAllCMSTextInCache = true;
	/**
	 * To show the identifiers in the content, the default value is true
	 */
	boolean showCMSIdentifiers = true;
}
