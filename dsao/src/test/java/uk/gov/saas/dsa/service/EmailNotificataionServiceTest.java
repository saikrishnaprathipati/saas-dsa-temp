package uk.gov.saas.dsa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.service.notification.EmailSenderService;

@ExtendWith(SpringExtension.class)
class EmailNotificataionServiceTest {
	private EmailSenderService subject;
	@MockitoBean
	private JavaMailSender javaMailSender;
	@MockitoBean
	private SpringTemplateEngine thymeleafTemplateEngine;
	@MockitoBean
	private ResourceBundleMessageSource emailMessageSource;
	@MockitoBean
	private ResourceLoader resourceLoader;
	@MockitoBean
	private DSAEmailConfigProperties dsaEmailConfigProperties;
	@MockitoBean
	private Environment environment;

	@BeforeEach
	public void setUp() throws Exception {
		subject = new EmailSenderService(javaMailSender, thymeleafTemplateEngine, emailMessageSource,
				resourceLoader, dsaEmailConfigProperties, environment);
	}

}
