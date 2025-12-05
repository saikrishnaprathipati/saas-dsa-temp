package uk.gov.saas.dsa.service.notification;

import static java.util.Arrays.asList;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.buildCommonUrls;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.populateAdvisorSignInURL;
import static uk.gov.saas.dsa.service.notification.NotificationUtil.populateStudentSignInURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.helper.DSAConstants;

/**
 * Email notification service
 */
@Service
public class EmailSenderService {

	private final Logger logger = LogManager.getLogger(this.getClass());

	private SpringTemplateEngine thymeleafTemplateEngine;
	private ResourceLoader resourceLoader;
	private JavaMailSender javaMailSender;
	private ResourceBundleMessageSource emailMessageSource;
	private DSAEmailConfigProperties dsaEmailConfigProperties;
	private Environment environment;

	/**
	 * Email notification
	 *
	 * @param javaMailSender
	 * @param thymeleafTemplateEngine
	 * @param emailMessageSource
	 * @param resourceLoader
	 * @param dsaEmailConfigProperties
	 * @param environment              Spring environment
	 */
	public EmailSenderService(JavaMailSender javaMailSender, SpringTemplateEngine thymeleafTemplateEngine,
							  ResourceBundleMessageSource emailMessageSource, ResourceLoader resourceLoader,
							  DSAEmailConfigProperties dsaEmailConfigProperties, Environment environment) {
		this.thymeleafTemplateEngine = thymeleafTemplateEngine;
		this.javaMailSender = javaMailSender;
		this.emailMessageSource = emailMessageSource;
		this.resourceLoader = resourceLoader;
		this.dsaEmailConfigProperties = dsaEmailConfigProperties;
		this.environment = environment;
	}

	public void sendEmailNotification(StudentResultVO studentResultVO, String emailSubject, String htmlTemplatePath,
									  Map<String, Object> modelMap) throws IllegalAccessException {
		populateDynamicModelVariables(studentResultVO, modelMap);
		String htmlBody = populateEmailBody(htmlTemplatePath, modelMap);
//		String[] ccEmails = getCCEmails((String) modelMap.get(DSAConstants.HEI_TEAM_EMAIL));
		sendEmail(new String[]{studentResultVO.getEmailAddress()}, new String[]{}, emailSubject, htmlBody);
	}

	public void sendEmailNotification(String[] toEmails, String[] ccEmails, String emailSubject,
									  String htmlTemplatePath, Map<String, Object> modelMap) throws IllegalAccessException {
		populateAdvisorSignInURL(dsaEmailConfigProperties, modelMap);
		String htmlBody = populateEmailBody(htmlTemplatePath, modelMap);

		sendEmail(toEmails, ccEmails, emailSubject, htmlBody);
	}

	private String populateEmailBody(String htmlTemplatePath, Map<String, Object> model) {
		Context thymeleafContext = new Context();
		buildCommonUrls(model, dsaEmailConfigProperties);

		thymeleafContext.setVariables(model);
		thymeleafTemplateEngine.setTemplateEngineMessageSource(emailMessageSource);
		String emailContent = thymeleafTemplateEngine.process(htmlTemplatePath, thymeleafContext);
		logger.info("Email content: {}", emailContent);
		return emailContent;

	}

	private void populateDynamicModelVariables(StudentResultVO studentResultVO, Map<String, Object> templateModel) {
		populateAdvisorSignInURL(dsaEmailConfigProperties, templateModel);
		populateStudentSignInURL(dsaEmailConfigProperties, studentResultVO.getSuid(),
				studentResultVO.getStudentReferenceNumber(), templateModel);
	}

	private void sendEmail(String[] toEmails, String[] ccEmails, String subject, String emailContent)
			throws IllegalAccessException {
		boolean isDeveloperProfile = NotificationUtil.isDevProfile(environment);

		if (isDeveloperProfile) {
			logger.info("******* Can't send email in dev profile To: {}, CC: {}", asList(toEmails), asList(ccEmails));

		} else {
			try {
				MimeMessage message = javaMailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, DSAConstants.UTF_8);
				helper.setFrom(dsaEmailConfigProperties.getSaasNoreplyEmail(),
						dsaEmailConfigProperties.getSaasNoreplyEmailName());
				helper.setTo(toEmails);
				if (!asList(ccEmails).isEmpty()) {
					helper.setCc(ccEmails);
				}
				helper.setSubject(subject);
				helper.setText(emailContent, true);

				Resource resource = resourceLoader.getResource(dsaEmailConfigProperties.getSaasLogo());
				helper.addInline(DSAConstants.SAAS_LOGO_PNG, resource);
				javaMailSender.send(message);
			} catch (MessagingException | IOException | MailException e) {

				logger.error("Exception occured while sending email", e);
				String cause = String.format("Unable to send emails To: {%s}, CC: {%s} reason: {%s}", asList(toEmails),
						asList(ccEmails), e.getMessage());
				String message = ServiceUtil.getCharactersFromString(cause, 500);

				throw new IllegalAccessException(message);

			}
			logger.info("******* email sent succesfully To: {}, CC: {}", asList(toEmails), asList(ccEmails));

		}

	}

}
