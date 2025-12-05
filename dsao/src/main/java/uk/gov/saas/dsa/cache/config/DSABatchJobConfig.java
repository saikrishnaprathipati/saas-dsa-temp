package uk.gov.saas.dsa.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "dsa.batchjob")
@Data
public class DSABatchJobConfig {

	boolean enabled = false;

}
