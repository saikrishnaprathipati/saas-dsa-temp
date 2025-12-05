package uk.gov.saas.dsa.email;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.model.EmailContent;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.service.notification.NotificationUtil;
import uk.gov.saas.dsa.web.helper.DSAConstants;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

	private final Logger logger = LogManager.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender sender;

	@Autowired
	private Configuration configuration;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private DSAEmailConfigProperties dsaEmailConfigProperties;

	private final Environment environment;

	public EmailService(JavaMailSender sender, Configuration configuration, ResourceLoader resourceLoader,
						Environment environment, DSAEmailConfigProperties dsaEmailConfigProperties) {
		this.sender = sender;
		this.configuration = configuration;
		this.resourceLoader = resourceLoader;
		this.environment = environment;
		this.dsaEmailConfigProperties = dsaEmailConfigProperties;
	}

	public Response sendEmail(EmailContent emailContent) {
		logger.info("Sending email to {} ", emailContent);

		Map<String, Object> model = new HashMap<>();
		model.put("emailContent", emailContent);
		NotificationUtil.buildCommonUrls(model, dsaEmailConfigProperties);
		emailContent.setModel(model);
 
		doEmail(emailContent);

 
 
		return new Response(ResponseCode.SUCCESS, null);
	}

	private void doEmail(EmailContent emailContent) {

		StringWriter stringWriter = new StringWriter();
		MimeMessage message = sender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(emailContent.getFromAddress(), emailContent.getFromName());
			helper.setTo(emailContent.getToAddress());
			helper.setSubject(emailContent.getSubject());
			Map<String, Object> model = emailContent.getModel();
			if (null != configuration.getTemplate(emailContent.getEmailTemplate())) {
				configuration.getTemplate(emailContent.getEmailTemplate()).process(model, stringWriter);
				String emailText = stringWriter.getBuffer().toString();
				helper.setText(emailText, emailContent.isHTML());
				Resource resource = resourceLoader.getResource(DSAConstants.SAAS_LOGO_PATH);
				helper.addInline(DSAConstants.SAAS_LOGO_PNG, resource);
			}

			boolean isDeveloperProfile = NotificationUtil.isDevProfile(environment);

			if (isDeveloperProfile) {
				sender.send(message);
				logger.error("Sorry we dont have the infrastructure to send the email in Dev environment");
			} else {
				sender.send(message);
				logger.info("Sent email {} ", emailContent.getToAddress());
			}
		} catch (MessagingException | IOException | TemplateException e) {

			logger.error("Exception occurred while sending email {}", e.getMessage());
 
		}
	}
}
