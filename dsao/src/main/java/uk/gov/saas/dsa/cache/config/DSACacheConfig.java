package uk.gov.saas.dsa.cache.config;

import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.saas.dsa.domain.ConfigData;

/**
 * To create the cache configuration
 *
 * @author Siva Chimpiri
 *
 */
@Configuration
@EnableCaching
public class DSACacheConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String DSA_CMS_CACHE = "dsaCMSCache";
    public static final String DSA_SERVICE_CACHE = "dsaServiceCache";

    private final DSACacheConfigProperties cacheConfigProperties;
    private final CacheManager cacheManager;

    public DSACacheConfig(DSACacheConfigProperties cacheConfigProperties) {
        this.cacheConfigProperties = cacheConfigProperties;
        this.cacheManager = Caching.getCachingProvider().getCacheManager();
    }

    /**
     * Cache manager configuration
     *
     * @return the cache manager
     */
    @Bean
    public CacheManager dsaCacheManager() {
        logger.info("cacheConfigProperties {}", cacheConfigProperties);

        ExpiryPolicy<Object, Object> timeToIdleExpiration = ExpiryPolicyBuilder
                .timeToIdleExpiration(Duration.ofSeconds(cacheConfigProperties.getTimeToIdleExpiration()));

        ResourcePools cacheMemorySize = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .offheap(cacheConfigProperties.getSizeMB(), MemoryUnit.MB).build();

        // CMS Configuration
        dsaCMSCacheConfig(timeToIdleExpiration, cacheMemorySize);

        // Service Configuration
        dsaServiceCacheConfig(timeToIdleExpiration, cacheMemorySize);

        logger.info("Cache manager created successfully for {} and {}", DSA_CMS_CACHE, DSA_SERVICE_CACHE);
        return cacheManager;
    }

    /**
     * CMS Cache configuration
     */
    private void dsaCMSCacheConfig(ExpiryPolicy<Object, Object> timeToIdleExpiration, ResourcePools cacheMemorySize){
        ExpiryPolicy<Object, Object> timeToLiveExpiration = ExpiryPolicyBuilder
                .timeToLiveExpiration(Duration.ofSeconds(cacheConfigProperties.getTimeToLiveExpirationCMS()));

        CacheConfiguration<String, String> cacheConfig = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, String.class, cacheMemorySize)
                .withExpiry(timeToIdleExpiration).withExpiry(timeToLiveExpiration).build();

        javax.cache.configuration.Configuration<String, String> configuration = Eh107Configuration
                .fromEhcacheCacheConfiguration(cacheConfig);

        if(cacheManager.getCache(DSA_CMS_CACHE) == null) {
            logger.info("No Cache for {}", DSA_CMS_CACHE);
            cacheManager.createCache(DSA_CMS_CACHE, configuration);
        }
    }

    /**
     * Service Cache configuration
     */
    private void dsaServiceCacheConfig(ExpiryPolicy<Object, Object> timeToIdleExpiration, ResourcePools cacheMemorySize){
        ExpiryPolicy<Object, Object> timeToLiveExpiration = ExpiryPolicyBuilder
                .timeToLiveExpiration(Duration.ofSeconds(cacheConfigProperties.getTimeToLiveExpirationService()));

        CacheConfiguration<String, ConfigData> cacheConfig = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, ConfigData.class, cacheMemorySize)
                .withExpiry(timeToIdleExpiration).withExpiry(timeToLiveExpiration).build();

        javax.cache.configuration.Configuration<String, ConfigData> configuration = Eh107Configuration
                .fromEhcacheCacheConfiguration(cacheConfig);

        if(cacheManager.getCache(DSA_SERVICE_CACHE) == null) {
            logger.info("No Cache for {}", DSA_SERVICE_CACHE);
            cacheManager.createCache(DSA_SERVICE_CACHE, configuration);
        }
    }
}

