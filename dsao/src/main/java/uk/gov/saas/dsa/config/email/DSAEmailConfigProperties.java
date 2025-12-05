package uk.gov.saas.dsa.config.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import uk.gov.saas.dsa.web.helper.DSAConstants;

/**
 * DSA CMS Cache configuration properties
 * 
 * @author Siva Chimpiri
 *
 */
@Configuration
@ConfigurationProperties(prefix = "dsa.mail")
@Data
public class DSAEmailConfigProperties {
	//private String saasNoreplyEmail = "noreply@saas.gov.uk";
	private String saasNoreplyEmail = "SAASBSU2@lab.scotland.gov.uk";
	private String saasNoreplyEmailName = "Students Awards Agency Scotland";
	/*
	 * CC email to be added to the each email notification, this is a comma(,)
	 * separated filed to include multiple mails if required.
	 */
	private String ccEmails;

	@Value("${dsa.mail.host}")
	private String host ;
	private String saasLogo = DSAConstants.SAAS_LOGO_PATH;

	// email.activation.link.alive.duration.time
	int emailActivationLinkAliveDurationTime = 15;
	private Long batchSize = 10l;
	private Long batchDelay = 10l;
	private Long batchCounter = 0l;

}
