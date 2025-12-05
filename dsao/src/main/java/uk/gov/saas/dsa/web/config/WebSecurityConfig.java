package uk.gov.saas.dsa.web.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import uk.gov.saas.dsa.domain.helpers.DefaultPasswordEncoderFactories;

@Configuration
@EnableWebSecurity //(debug = true)
public class WebSecurityConfig {
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	private DsaAuthenticationProvider dsaAuthenticationProvider;
	
	private static final String REPORT_TO = 
			"{\"group\":\"csp-violation-report\",\"max_age\":2592000,\"endpoints\":[{\"url\":\"https://localhost:8080/report\"}]}";
	
    @Autowired
    public void SecurityConfig(DsaAuthenticationProvider dsaAuthenticationProvider) {
        this.dsaAuthenticationProvider = dsaAuthenticationProvider;
    }
    
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(dsaAuthenticationProvider);
    }

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		logger.info("Inside filterChain");
		

		http
			.addFilterBefore(new ContentSecurityPolicyNonceFilter(), HeaderWriterFilter.class)

			//NOTE - see https://csp-evaluator.withgoogle.com
			.headers(headers -> headers
					.addHeaderWriter(new StaticHeadersWriter("Report-To", REPORT_TO))
		            .contentSecurityPolicy(csp -> csp
	                        .policyDirectives(
	                        		"script-src 'strict-dynamic' 'unsafe-inline' 'nonce-{nonce}' https:;" + 
	                        		"script-src-elem 'self';" + 
	                        		"require-trusted-types-for 'script';" +
                				  	"font-src fonts.gstatic.com; " + 
                				  	"style-src 'self'  fonts.googleapis.com; " + 
                				  	"img-src 'self' data:;" +
                				  	"object-src 'none'; " + 
                				  	"form-action 'self';" +
                				  	"base-uri 'none';" +
                				  	"frame-ancestors 'self';" +
                				  	"default-src 'self';" +
	                        		"report-uri /report; report-to csp-violation-report")
	                ).cacheControl(withDefaults())
					.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
					.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true)
							.maxAgeInSeconds(31536000)
						.requestMatcher(AnyRequestMatcher.INSTANCE)))

			.sessionManagement(session -> session
			            .maximumSessions(1)
					    .maxSessionsPreventsLogin(true));
		
			http
			.sessionManagement(session -> compositeSessionAuthenticationStrategy());

			http.logout(logout -> logout.invalidateHttpSession(true));

			http.cors(withDefaults());

		return http.build();
	}
	
	
	 // Concurrent Session Control
	 @Bean
	 public HttpSessionEventPublisher httpSessionEventPublisher() {
	     return new HttpSessionEventPublisher();
	 }


	 /**
	  * Setup {@link JdbcUserDetailsManager} service
	  * @param dataSource connection factory for the DB
	  * @return users the manager
	  */
	@Bean
	public UserDetailsManager users(DataSource dataSource) {

		logger.info("Datasource {}", dataSource);

		JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
		users.setUsersByUsernameQuery("select ua.user_id,ua.password,a.enabled from user_auth_details ua, user_personal_details upd, authorities a where ua.user_id=upd.user_id and UPPER(upd.email_addr) = UPPER(?) and ua.authority = a.id");
		users.setAuthoritiesByUsernameQuery("select ua.user_id, a.name from user_auth_details ua, authorities a where ua.user_id = ? and ua.authority = a.id");

		return users;
	}
	

	/**
	 * Use our custom {@link DefaultPasswordEncoderFactories} to authorise legacy passwords
	 * and use the recommended {@link Argon2PasswordEncoder} for any new ones
	 * @return {@link DelegatingPasswordEncoder)
	 */
	@Bean 
	public PasswordEncoder passwordEncoder() { 
		return DefaultPasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
    @Bean
    protected CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy(){
        List<SessionAuthenticationStrategy> list = new ArrayList<>();
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(1);
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        SessionFixationProtectionStrategy sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry());
        list.add(concurrentSessionControlAuthenticationStrategy);
        list.add(sessionFixationProtectionStrategy);
        list.add(registerSessionAuthenticationStrategy);
        return new CompositeSessionAuthenticationStrategy(list);
    }

    @Bean
    protected SessionRegistryImpl sessionRegistry(){
        return new SessionRegistryImpl();
    }


}
