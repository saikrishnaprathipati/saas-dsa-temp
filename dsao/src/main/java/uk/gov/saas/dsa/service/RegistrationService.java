package uk.gov.saas.dsa.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.ActivationStatusType;
import uk.gov.saas.dsa.model.EmailContent;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;
import uk.gov.saas.dsa.web.helper.DateHelper;
import uk.gov.saas.dsa.web.helper.EmailTokenGenerator;

/**
 * Registration Service
 */

@Service
public class RegistrationService {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final String ACTIVATION_LINK_LIFE_TIME = "15";
	private static final String NOT_AVAILABLE = "NOT AVAILABLE";

	private static final String ACTIVATION_EMAIL_FTLH = "mail-templates/activationEmail.html";

	private static final String STUDENTS_AWARDS_AGENCY_SCOTLAND = "Students Awards Agency Scotland";
	//private static final String NOREPLY_SAAS_GOV_UK = "noreply@saas.gov.uk";
	private String NOREPLY_SAAS_GOV_UK = "SAASBSU2@lab.scotland.gov.uk";

	private DsaAdvisorAuthRepository dsaAdvisorAuthRepository;
	private EmailService emailSender;
	private DsaAdvisorRepository dsaAdvisorRepository;
	private LoginService loginService;

	private DSAEmailConfigProperties dsaEmailConfigProperties;

	private static final String ACCOUNT_ACTIVATION_PATH = "/dsa/activateAccount?token=";
	private static final String START_PAGE = "/dsa/start";

	@Autowired
	public RegistrationService(DsaAdvisorAuthRepository dsaAdvisorAuthRepository, EmailService emailSender,
							   DsaAdvisorRepository dsaAdvisorRepository, LoginService loginService, DSAEmailConfigProperties dsaEmailConfigProperties) {
		this.dsaAdvisorAuthRepository = dsaAdvisorAuthRepository;
		this.emailSender = emailSender;
		this.dsaAdvisorRepository = dsaAdvisorRepository;
		this.loginService = loginService;
		this.dsaEmailConfigProperties = dsaEmailConfigProperties;
	}

	public Response requestActivation(String emailId) {
		logger.info("Finding user by email address: " + emailId);
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);

		if (null == dsaAdvisor) {
			return new Response(ResponseCode.EMAIL_INVALID, null);
		}

		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository.findByEmailIgnoreCase(dsaAdvisor.getEmail());
		logger.info("Finding dsaAdvisorAuthDetails by email address: " + dsaAdvisorAuthDetails);
		String token = EmailTokenGenerator.generateRegistrationCode();

		// save the activation token details to DB
		if (null == dsaAdvisorAuthDetails) {
			dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
			dsaAdvisorAuthDetails.setEmail(dsaAdvisor.getEmail());
			dsaAdvisorAuthDetails.setRoleName(dsaAdvisor.getRoleName());
			dsaAdvisorAuthDetails.setUserId(dsaAdvisor.getUserId());
			dsaAdvisorAuthDetails.setActivationStatus(ActivationStatusType.ACTIVATION_REQUESTED.toString());
			dsaAdvisorAuthDetails.setActivationToken(token);
			dsaAdvisorAuthDetails.setActivationRequestDate(Timestamp.valueOf(LocalDateTime.now()));
			sendActivationEmail(emailId, dsaAdvisor, token);
			dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
			return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
		}

		Date lastAllowableActivationDate = getAllowableActivationDate(dsaAdvisorAuthDetails);

		switch (ActivationStatusType.valueOf(dsaAdvisorAuthDetails.getActivationStatus())) {
			case ACTIVATION_REQUESTED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					saveStatusActivationTokenAndDate(dsaAdvisorAuthDetails,
							ActivationStatusType.ACTIVATION_REQUESTED.toString(), token);
					sendActivationEmail(emailId, dsaAdvisor, token);
					dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
					return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
				} else {
					saveStatusAndActivationToken(dsaAdvisorAuthDetails,
							ActivationStatusType.ACTIVATION_RE_REQUESTED.toString(), token);
					sendActivationEmail(emailId, dsaAdvisor, token);
					dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
					return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
				}
			case ACTIVATION_RE_REQUESTED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					saveStatusActivationTokenAndDate(dsaAdvisorAuthDetails,
							ActivationStatusType.ACTIVATION_REQUESTED.toString(), token);
					sendActivationEmail(emailId, dsaAdvisor, token);
					dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
					return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
				} else {
					return new Response(ResponseCode.ACTIVATION_LIMIT_EXCEEDED, null);
				}
			case ACTIVE:
				return new Response(ResponseCode.ACCOUNT_ALREADY_ACTIVATED, null);
			case EXPIRED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					saveStatusActivationTokenAndDate(dsaAdvisorAuthDetails,
							ActivationStatusType.ACTIVATION_REQUESTED.toString(), token);
					sendActivationEmail(emailId, dsaAdvisor, token);
					dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
					return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
				} else {
					return new Response(ResponseCode.ACTIVATION_LINK_EXPIRED, null);
				}
			default:
				saveStatusActivationTokenAndDate(dsaAdvisorAuthDetails,
						ActivationStatusType.ACTIVATION_REQUESTED.toString(), token);
				return new Response(ResponseCode.ACCOUNT_NOT_ACTIVATED, null);
		}
	}

	private void saveStatusActivationTokenAndDate(DsaAdvisorAuthDetails dsaAdvisorAuthDetails, String status,
												  String token) {
		dsaAdvisorAuthDetails.setActivationStatus(status);
		dsaAdvisorAuthDetails.setActivationToken(token);
		dsaAdvisorAuthDetails.setActivationRequestDate(Timestamp.valueOf(LocalDateTime.now()));
	}

	private void saveStatusAndActivationToken(DsaAdvisorAuthDetails dsaAdvisorAuthDetails, String status,
											  String token) {
		dsaAdvisorAuthDetails.setActivationStatus(status);
		dsaAdvisorAuthDetails.setActivationToken(token);
	}

	private void sendActivationEmail(String emailId, DsaAdvisor dsaAdvisor, String token) {
		// send an activation email
		logger.info("Sending email to {} ", emailId);

		EmailContent emailContent = new EmailContent();
		emailContent.setBody(StringUtils.join(dsaEmailConfigProperties.getHost(),
				ACCOUNT_ACTIVATION_PATH, token, "&userId=", dsaAdvisor.getUserId()));
		emailContent.setToAddress(emailId);
		emailContent.setFromAddress(NOREPLY_SAAS_GOV_UK);
		emailContent.setFromName(STUDENTS_AWARDS_AGENCY_SCOTLAND);
		emailContent.setSubject("Complete your DSA Account registration");
		emailContent.setEmailTemplate(ACTIVATION_EMAIL_FTLH);
		emailContent.setHomePage(StringUtils.join(dsaEmailConfigProperties.getHost(), START_PAGE));
		emailContent.setFullName(dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName());
 
		emailSender.sendEmail(emailContent);
 
	}

	public String checkActivationStatus(String emailId) {
		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId);
		return dsaAdvisorAuthDetails.getActivationStatus();
	}

	public Response completeRegistration(CreatePasswordFormVO createPasswordFormVO) {
		logger.info("Finding user by email {} ", createPasswordFormVO.getEmail());
		DsaAdvisorAuthDetails dsaAuthAdvisorDetails = dsaAdvisorAuthRepository
				.findByEmailIgnoreCase(createPasswordFormVO.getEmail());

		dsaAuthAdvisorDetails.setActivationRequestDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAuthAdvisorDetails.setActivationStatus(ActivationStatusType.ACTIVE.toString());
		dsaAdvisorAuthRepository.save(dsaAuthAdvisorDetails);

		loginService.saveAdvisorLoginDetails(createPasswordFormVO, dsaAuthAdvisorDetails);

		return new Response(ResponseCode.SUCCESS, null);
	}

	public Response checkAdvisorDetails(String token, String userId) {
		logger.info("Finding user by token {} ", token);
		DsaAdvisorAuthDetails dsaAuthAdvisorDetails = dsaAdvisorAuthRepository.findByActivationToken(token);

		if (null == dsaAuthAdvisorDetails) {
			logger.info("Advisor details not found, activation token {} invalid ", token);
			return new Response(ResponseCode.ACTIVATION_TOKEN_INVALID,
					populateWithErrorDetails(ResponseCode.ACTIVATION_TOKEN_INVALID.getMessage(), "NOT AVAILABLE"));
		}

		if (null != dsaAuthAdvisorDetails.getActivationStatus() && dsaAuthAdvisorDetails.getActivationStatus()
				.equalsIgnoreCase(ActivationStatusType.EXPIRED.getMessage())) {
			logger.info("Advisor details not found,  activation token {} expired", token);
			return new Response(ResponseCode.ACTIVATION_LINK_EXPIRED, populateWithErrorDetails(
					ResponseCode.ACTIVATION_LINK_EXPIRED.getMessage(), dsaAuthAdvisorDetails.getEmail()));
		}

		if (null != dsaAuthAdvisorDetails.getActivationToken() && dsaAuthAdvisorDetails.getActivationStatus()
				.equalsIgnoreCase(ActivationStatusType.ACTIVE.getMessage())) {
			logger.info("Account already activated for activation token {} ", token);
			return new Response(ResponseCode.ACCOUNT_ALREADY_ACTIVATED,
					populateWithErrorDetails(ResponseCode.ACCOUNT_ALREADY_ACTIVATED.getMessage(), "NOT AVAILABLE"));
		}

		Date lastAllowableActivationDate = getAllowableActivationDate(dsaAuthAdvisorDetails);

		if (isLinkExpired(lastAllowableActivationDate)) {
			logger.info("Activation token: " + token + " expired and activation requested with allowable date: "
					+ lastAllowableActivationDate);
			dsaAuthAdvisorDetails.setActivationStatus(ActivationStatusType.EXPIRED.toString());
			dsaAdvisorAuthRepository.save(dsaAuthAdvisorDetails);
			return new Response(ResponseCode.ACTIVATION_LINK_EXPIRED, populateWithErrorDetails(
					ResponseCode.ACTIVATION_LINK_EXPIRED.getMessage(), dsaAuthAdvisorDetails.getEmail()));
		}

		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(dsaAuthAdvisorDetails.getEmail());
		return new Response(ResponseCode.SUCCESS, populateAdvisorResultVO(dsaAdvisor));
	}

	public Response getAdvisorDetails(String emailAddress) {
		logger.info("Finding user by emailAddress {} ", emailAddress);
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailAddress);

		if (null == dsaAdvisor) {
			logger.info("Advisor details not found, userId {} invalid ", emailAddress);
			return new Response(ResponseCode.ACTIVATION_TOKEN_INVALID,
					populateWithErrorDetails(ResponseCode.ACTIVATION_TOKEN_INVALID.getMessage(), "NOT AVAILABLE"));
		}

		return new Response(ResponseCode.SUCCESS, populateAdvisorResultVO(dsaAdvisor));
	}

	private Date getAllowableActivationDate(DsaAdvisorAuthDetails dsaAuthAdvisorDetails) {

		Date lastAllowableActivationDate = DateHelper.addMinutesToDate(dsaAuthAdvisorDetails.getActivationRequestDate(),
				dsaEmailConfigProperties.getEmailActivationLinkAliveDurationTime());
		return lastAllowableActivationDate;
	}

	private AdvisorResultVO populateAdvisorResultVO(DsaAdvisor dsaAdvisor) {

		AdvisorResultVO resultVO = new AdvisorResultVO();
		resultVO.setAdvisorId(dsaAdvisor.getAdvisorId());
		resultVO.setEmail(dsaAdvisor.getEmail());
		resultVO.setFirstName(dsaAdvisor.getFirstName());
		resultVO.setLastName(dsaAdvisor.getLastName());
		resultVO.setInstitution(dsaAdvisor.getInstitution());
		resultVO.setTeamEmail(dsaAdvisor.getTeamEmail());
		resultVO.setRoleName(dsaAdvisor.getRoleName());
		resultVO.setUserId(dsaAdvisor.getUserId());
		return resultVO;
	}

	private AdvisorResultVO populateWithErrorDetails(String response, String email) {

		AdvisorResultVO resultVO = new AdvisorResultVO();
		resultVO.setAdvisorId(0L);
		resultVO.setEmail(email);
		resultVO.setFirstName(NOT_AVAILABLE);
		resultVO.setLastName(NOT_AVAILABLE);
		resultVO.setInstitution(NOT_AVAILABLE);
		resultVO.setTeamEmail(NOT_AVAILABLE);
		resultVO.setRoleName(NOT_AVAILABLE);
		resultVO.setResponse(response);
		return resultVO;
	}

	private boolean isLinkExpired(Date lastAllowableActivationDate) {
		return DateHelper.getCurrentDate().after(lastAllowableActivationDate);
	}
}
