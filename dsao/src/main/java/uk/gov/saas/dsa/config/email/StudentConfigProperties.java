package uk.gov.saas.dsa.config.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ols")
@Data
public class StudentConfigProperties {

    private String dashboard;
    private String profile;
    private String privacy;
    private String logoutConfirm;
}
