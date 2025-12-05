package uk.gov.saas.dsa;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import uk.gov.saas.dsa.web.helper.DSAConstants;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
public class DSAApplication {
	private final static Logger logger = LogManager.getLogger(DSAApplication.class);

	public static void main(String[] args) {
		logger.info("****************** DSA application starting ******************");
		ConfigurableApplicationContext context = SpringApplication.run(DSAApplication.class, args);
		Environment environemnt = context.getBean(Environment.class);
		logger.info("****************** DSA application started succesfully with  profiles {} ******************",
				Arrays.asList(environemnt.getActiveProfiles()));
	}

	@Bean
	public LocalValidatorFactoryBean getValidator(@Autowired MessageSource messageSource) {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource);
		return bean;
	}

	/**
	 * Default messages resource
	 * 
	 * @return MessageSource
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding(DSAConstants.UTF_8);
		return messageSource;
	}

	/**
	 * Email messages resource
	 * 
	 * @return ResourceBundleMessageSource
	 */
	@Bean(name = "emailMessageSource")
	public ResourceBundleMessageSource emailMessageSource() {
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("mailMessages");
		messageSource.setDefaultEncoding(DSAConstants.UTF_8);
		return messageSource;
	}
}
