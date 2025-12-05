package uk.gov.saas.dsa.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.EmailContent;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "localdev" })
class EmailServiceTest {
	private EmailService emailService;
	@MockitoBean
	private JavaMailSender sender;
	@MockitoBean
	MimeMessageHelper mimeMessageHelper;
	@MockitoBean
	private SpringTemplateEngine thymeleafTemplateEngine;
	@MockitoBean
	private ResourceBundleMessageSource emailMessageSource;
	@MockitoBean
	private Configuration configuration;
	@MockitoBean
	private ResourceLoader resourceLoader;
	
	@MockitoBean
	private DSAEmailConfigProperties dsaEmailConfigProperties;

	@MockitoBean
	private Resource resource;
	@MockitoBean
	private Environment environment;
	@MockitoBean
	private MimeMessage message;
	private EmailContent emailContent;
	@BeforeEach
	public void setUp() throws Exception {
		emailService = new EmailService(sender, configuration, resourceLoader, environment, dsaEmailConfigProperties);
	}

//	@Test
	void test_sendActivationEmail() throws IOException {
		emailContent = new EmailContent();
		emailContent.setEmailTemplate("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setBody("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setToAddress("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setFromAddress("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setFromName("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setSubject("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setEmailTemplate("email/activationEmail.html");
		emailContent.setHomePage("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setModel(Maps.of("STUDENTS_AWARDS_AGENCY_SCOTLAND", "SAAS"));
		emailContent.setContent("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		Template template = configuration.getTemplate("email/activationEmail.html");

		when(sender.createMimeMessage()).thenReturn(message);
		when(configuration.getTemplate("email/activationEmail.html")).thenReturn(template);
		when(resourceLoader.getResource(any())).thenReturn(resource);
		when(environment.getActiveProfiles()).thenReturn(new String[]{"dev1, dev2"});
		emailService.sendEmail(emailContent);
	}

	@Test
	void test_sendEmail() throws IOException {
		emailContent = new EmailContent();
		emailContent.setEmailTemplate("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setBody("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setToAddress("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setFromAddress("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setFromName("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setSubject("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setEmailTemplate("email/activationEmail.html");
		emailContent.setHomePage("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		emailContent.setModel(Maps.of("STUDENTS_AWARDS_AGENCY_SCOTLAND", "SAAS"));
		emailContent.setContent("STUDENTS_AWARDS_AGENCY_SCOTLAND");
		Template template = configuration.getTemplate("email/activationEmail.html");

		when(sender.createMimeMessage()).thenReturn(message);
		when(configuration.getTemplate("email/activationEmail.html")).thenReturn(template);
		when(resourceLoader.getResource(any())).thenReturn(resource);
		when(environment.getActiveProfiles()).thenReturn(new String[]{"dev1, dev2"});
		emailService.sendEmail(emailContent);
	}
}
