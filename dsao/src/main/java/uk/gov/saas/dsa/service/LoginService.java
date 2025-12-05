package uk.gov.saas.dsa.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.*;
import uk.gov.saas.dsa.domain.helpers.PasswordEncryptionHelper;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.ActivationStatusType;
import uk.gov.saas.dsa.model.DeviceVerificationStatusType;
import uk.gov.saas.dsa.model.EmailContent;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorLoginRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;
import uk.gov.saas.dsa.persistence.DsaStudentAuthDetailsRepository;
import uk.gov.saas.dsa.vo.AdvisorResultVO;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;
import uk.gov.saas.dsa.vo.ForgotPasswordFormVO;
import uk.gov.saas.dsa.vo.SaveDeviceFormVO;
import uk.gov.saas.dsa.web.helper.DateHelper;
import uk.gov.saas.dsa.web.helper.EmailTokenGenerator;

@Service
public class LoginService {
	private static final String SUBJECT_RESET_YOUR_PASSWORD = "Reset your password";

	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final String NOT_AVAILABLE = "NOT AVAILABLE";
	private static final String LOGIN_NOTIFICATION_EMAIL_FTLH = "mail-templates/loginNotificationEmail.html";
	private static final String FROM_NAME_STUDENTS_AWARDS_AGENCY_SCOTLAND = "Students Awards Agency Scotland";
	//private static final String NOREPLY_SAAS_GOV_UK = "noreply@saas.gov.uk";
	private String NOREPLY_SAAS_GOV_UK = "SAASBSU2@lab.scotland.gov.uk";
	private static final String ACCOUNT_LOCKED_EMAIL_FTLH = "mail-templates/lockedAccountNotificationEmail.html";
	private static final String PASSWORD_RESET_EMAIL_FTLH = "mail-templates/passwordResetNotificationEmail.html";
	private static final String FORGOT_PASSWORD_EMAIL_FTLH = "mail-templates/forgottenPasswordNotificationEmail.html";

	private static final String DISABLED_STUDENT_PASSWORD_RESET = "Disabled Student's Password Reset";
	private static final String RESET_PASSWORD = "/dsa/resetPassword?token=";
	private static final String LOAD_SAVE_DEVICE_PAGE = "/dsa/loadSaveDevice?token=";
	private static final String EMAIL_ACTIVATION_PATH = "/dsa/saveDevice?token=";
	private static final String START_PAGE = "/dsa/start";
	private static final String FORGOTTEN_PASSWORD = "/dsa/resetPassword?token=";
	private DsaAdvisorAuthRepository dsaAdvisorAuthRepository;
	private DsaAdvisorLoginRepository dsaAdvisorLoginRepository;
	private DsaStudentAuthDetailsRepository dsaStudentAuthDetailsRepository;
	private EmailService emailSender;
	private DsaAdvisorRepository dsaAdvisorRepository;
	private DeviceMetadataService deviceMetadataService;
	private DSAEmailConfigProperties emailConfigProperties;

	@Autowired
	public LoginService(DsaAdvisorLoginRepository dsaAdvisorLoginRepository,
						DsaAdvisorAuthRepository dsaAdvisorAuthRepository, EmailService emailSender,
						DsaAdvisorRepository dsaAdvisorRepository, DeviceMetadataService deviceMetadataService,
						DSAEmailConfigProperties emailConfigProperties,
						DsaStudentAuthDetailsRepository dsaStudentAuthDetailsRepository) {
		this.dsaAdvisorLoginRepository = dsaAdvisorLoginRepository;
		this.dsaAdvisorAuthRepository = dsaAdvisorAuthRepository;
		this.emailSender = emailSender;
		this.dsaAdvisorRepository = dsaAdvisorRepository;
		this.deviceMetadataService = deviceMetadataService;
		this.emailConfigProperties = emailConfigProperties;
		this.dsaStudentAuthDetailsRepository = dsaStudentAuthDetailsRepository;
	}

	public Response verifyDevice(HttpServletRequest request, String emailId) {
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId);
		dsaAdvisorLoginDetails.setFailedPasswordCount(0);
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorLoginRepository.save(dsaAdvisorLoginDetails);

		DeviceMetadata existingDevice = getExistingDevice(request, emailId);
		logger.info("verify Device for emailid {} and device is {}", emailId, existingDevice);

		if (Objects.nonNull(existingDevice) && existingDevice.isRememberDevice()) {
			boolean hasDeviceVerificationExpired = DateHelper
					.addDaysToDate(existingDevice.getDeviceVerificationDate(), 7).before(DateHelper.getCurrentDate());
			if (hasDeviceVerificationExpired) {
				logger.info("Device is verified but expired, requesting email verification {}", existingDevice);
				return requestEmailVerification(request, emailId);
			} else {
				logger.info("Device is verified {}", existingDevice);
				return new Response(ResponseCode.DEVICE_VERIFIED,
						populateWithErrorDetails(ResponseCode.DEVICE_VERIFIED.getMessage(), dsaAdvisor.getEmail()));
			}
		} else {
			logger.info("Device is not verified, requesting email verification {}", existingDevice);
			return requestEmailVerification(request, emailId);
		}
	}

	public void rememberDeviceAndContinue(HttpServletRequest request, SaveDeviceFormVO saveDeviceFormVO) {
		DeviceMetadata existingDevice = getExistingDevice(request, saveDeviceFormVO.getEmail());
		logger.info("remember Device And Continue {}", existingDevice);

		if (Objects.nonNull(existingDevice)) {
			existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_VERIFIED.name());
			existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
			existingDevice.setRememberDevice(saveDeviceFormVO.isRememberDevice() ? true : false);
			deviceMetadataService.updateExistingDevice(existingDevice);
		}
	}

	public void forgotPassword(HttpServletRequest request, ForgotPasswordFormVO forgotPasswordFormVO) {
		String token = EmailTokenGenerator.generateRegistrationCode();
		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository
				.findByEmailIgnoreCase(forgotPasswordFormVO.getEmailAddress());
		sendForgotPasswordEmail(forgotPasswordFormVO.getEmailAddress(), dsaAdvisorAuthDetails.getUserId(), token);
		dsaAdvisorAuthDetails.setPasswordResetReqDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorAuthDetails.setPasswordResetToken(token);

		dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
	}

	private void sendForgotPasswordEmail(String emailId, String userId, String token) {
		logger.info("Sending forgot password email to {} ", emailId);

		EmailContent emailContent = new EmailContent();

		emailContent.setBody(
				StringUtils.join(emailConfigProperties.getHost(), FORGOTTEN_PASSWORD, token, "&userId=", userId));
		emailContent.setToAddress(emailId);
		emailContent.setFromAddress(NOREPLY_SAAS_GOV_UK);
		emailContent.setFromName(FROM_NAME_STUDENTS_AWARDS_AGENCY_SCOTLAND);
		emailContent.setSubject("Reset your password");
		emailContent.setEmailTemplate(FORGOT_PASSWORD_EMAIL_FTLH);
		emailContent.setHomePage(StringUtils.join(emailConfigProperties.getHost(), START_PAGE));
 
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);
		String fullName = dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName();
		emailContent.setFullName(fullName);

		emailSender.sendEmail(emailContent);
 
	}

	public DeviceMetadata getExistingDevice(HttpServletRequest request, String emailId) {
		return deviceMetadataService.isExistingDevice(request, emailId);
	}

	public Response requestEmailVerification(HttpServletRequest request, String emailId) {
		logger.info("Finding user by email address: " + emailId);
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);

		DeviceMetadata existingDevice = getExistingDevice(request, emailId);

		if (null == dsaAdvisor) {
			return new Response(ResponseCode.EMAIL_INVALID, null);
		}
		String token = EmailTokenGenerator.generateRegistrationCode();

		// save the verification token details to DB
		if (null == existingDevice) {
			logger.info("No existing device found {}", existingDevice);
			deviceMetadataService.saveDevice(request, token, dsaAdvisor);
			sendVerificationEmail(emailId, dsaAdvisor, token);
			return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
		}

		Date lastAllowableActivationDate = getAllowableActivationDate(existingDevice);
		boolean hasDeviceVerificationExpired = DateHelper.addDaysToDate(existingDevice.getDeviceVerificationDate(), 7)
				.before(DateHelper.getCurrentDate());

		switch (DeviceVerificationStatusType.valueOf(existingDevice.getDeviceVerificationStatus())) {
			case DEVICE_TOKEN_REQUESTED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					logger.info("Verification link expired {}", lastAllowableActivationDate);
					saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
				} else {
					logger.info("Verification link re requested for {}", emailId);
					saveStatusDeviceVerificationTokenAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_RE_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
				}
			case DEVICE_TOKEN_RE_REQUESTED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					logger.info("Verification link expired {}", lastAllowableActivationDate);
					saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
				}
			case DEVICE_VERIFIED:
				if (!existingDevice.isRememberDevice()) {
					logger.info("Device verified but not saved so sending email for verification {}", emailId);
					saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_TOKEN_REQUESTED, null);
				} else if (existingDevice.isRememberDevice() && hasDeviceVerificationExpired) {
					logger.info("Verification verified but expired so sending email for verification {}", emailId);
					saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_TOKEN_REQUESTED, null);
				} else {
					logger.info("Device verified {}", emailId);
					return new Response(ResponseCode.DEVICE_ALREADY_VERIFIED, null);
				}
			case DEVICE_TOKEN_EXPIRED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					logger.info("Verification token requested {}", emailId);
					saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
							DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
					return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
				} else {
					logger.info("Device token expired {}", emailId);
					return new Response(ResponseCode.DEVICE_TOKEN_EXPIRED, null);
				}
			default:
				saveStatusDeviceVerificationTokenDateAndSendEmail(existingDevice,
						DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.toString(), token, emailId, dsaAdvisor);
				logger.info("Verification token requested {}", emailId);
				return new Response(ResponseCode.DEVICE_NOT_VERIFIED, null);
		}
	}

	public Response saveAdvisorLoginDetails(CreatePasswordFormVO createPasswordFormVO,
											DsaAdvisorAuthDetails dsaAuthAdvisorDetails) {
		logger.info("Finding user by email {} ", createPasswordFormVO.getEmail());

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = dsaAdvisorLoginRepository
				.findByUserNameIgnoreCase(createPasswordFormVO.getEmail());

		if (null == dsaAdvisorLoginDetails) {
			extractAndSaveLoginDetails(createPasswordFormVO, dsaAuthAdvisorDetails);
		}

		return new Response(ResponseCode.SUCCESS, null);
	}

	public Response completeRegistration(CreatePasswordFormVO createPasswordFormVO) {
		logger.info("Finding user by email {} ", createPasswordFormVO.getEmail());
		DsaAdvisorAuthDetails dsaAuthAdvisorDetails = dsaAdvisorAuthRepository
				.findByEmailIgnoreCase(createPasswordFormVO.getEmail());

		dsaAuthAdvisorDetails.setActivationRequestDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAuthAdvisorDetails.setActivationStatus(ActivationStatusType.ACTIVE.toString());

		dsaAdvisorAuthRepository.save(dsaAuthAdvisorDetails);

		saveAdvisorLoginDetails(createPasswordFormVO, dsaAuthAdvisorDetails);

		return new Response(ResponseCode.SUCCESS, null);
	}

	public Response saveDeviceDetails(String token, String userId) {
		logger.info("Finding user by token {} ", token);

		DeviceMetadata savedDevice = deviceMetadataService.findDeviceByToken(token, userId);

		if (null == savedDevice || null == savedDevice.getDeviceVerificationToken()) {
			logger.info("Device details not found, activation token {} invalid ", token);
			return new Response(ResponseCode.DEVICE_TOKEN_INVALID,
					populateWithErrorDetails(ResponseCode.DEVICE_TOKEN_INVALID.name(), "NOT AVAILABLE"));
		}

		Date lastAllowableActivationDate = getAllowableActivationDate(savedDevice);
		switch (DeviceVerificationStatusType.valueOf(savedDevice.getDeviceVerificationStatus())) {
			case DEVICE_TOKEN_EXPIRED:
				logger.info("Device details not found,  activation token {} expired", token);
				return new Response(ResponseCode.DEVICE_TOKEN_EXPIRED,
						populateWithErrorDetails(ResponseCode.DEVICE_TOKEN_EXPIRED.name(), savedDevice.getEmailId()));

			case DEVICE_VERIFIED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					logger.info("Activation token: " + token + " expired and activation requested with allowable date: "
							+ lastAllowableActivationDate);
					savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_EXPIRED.name());
					deviceMetadataService.updateExistingDevice(savedDevice);

					return new Response(ResponseCode.DEVICE_VERIFIED_LINK_EXPIRED, populateWithErrorDetails(
							ResponseCode.DEVICE_VERIFIED_LINK_EXPIRED.name(), savedDevice.getEmailId()));
				} else {
					logger.info("Account already activated for activation token {} ", token);
					return new Response(ResponseCode.DEVICE_ALREADY_VERIFIED, populateWithErrorDetails(
							ResponseCode.DEVICE_ALREADY_VERIFIED.name(), savedDevice.getEmailId()));
				}
			case DEVICE_TOKEN_REQUESTED:
			case DEVICE_TOKEN_RE_REQUESTED:
				if (isLinkExpired(lastAllowableActivationDate)) {
					logger.info("Activation token: " + token + " expired and activation requested with allowable date: "
							+ lastAllowableActivationDate);
					savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_EXPIRED.name());
					deviceMetadataService.updateExistingDevice(savedDevice);

					return new Response(ResponseCode.DEVICE_TOKEN_EXPIRED,
							populateWithErrorDetails(ResponseCode.DEVICE_TOKEN_EXPIRED.name(), savedDevice.getEmailId()));
				}
			default:
				logger.info("Advisor details found");
				DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(savedDevice.getEmailId());
				return new Response(ResponseCode.SUCCESS, populateAdvisorResultVO(dsaAdvisor));
		}
	}

	public Response forgottenPassword(String token, String userId) {
		logger.info("Finding user by token {} ", token);

		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository.findByPasswordResetToken(token);
		logger.info("Advisor details found {}", dsaAdvisorAuthDetails);

		if (null == dsaAdvisorAuthDetails) {
			logger.info("Device details not found, activation token {} invalid ", token);
			return new Response(ResponseCode.RESET_TOKEN_INVALID,
					populateWithErrorDetails(ResponseCode.RESET_TOKEN_INVALID.name(), "NOT AVAILABLE"));
		}

		Date lastAllowableActivationDate = getAllowableActivationDate(dsaAdvisorAuthDetails);
		if (isLinkExpired(lastAllowableActivationDate)) {
			return new Response(ResponseCode.RESET_TOKEN_EXPIRED, populateWithErrorDetails(
					ResponseCode.RESET_TOKEN_EXPIRED.name(), dsaAdvisorAuthDetails.getEmail()));
		}

		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(dsaAdvisorAuthDetails.getEmail());
		return new Response(ResponseCode.SUCCESS, populateAdvisorResultVO(dsaAdvisor));
	}

	public Response saveNewPassword(CreatePasswordFormVO createPasswordFormVO) {
		String token = EmailTokenGenerator.generateRegistrationCode();

		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository
				.findByEmailIgnoreCase(createPasswordFormVO.getEmail());
		dsaAdvisorAuthDetails.setPasswordResetReqDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorAuthDetails.setPasswordResetToken(token);
		dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = dsaAdvisorLoginRepository
				.findByUserNameIgnoreCase(createPasswordFormVO.getEmail());

		saveSecretKeyAndPassword(createPasswordFormVO, dsaAdvisorLoginDetails);

		dsaAdvisorLoginDetails.setIsActive(Boolean.TRUE);
		dsaAdvisorLoginDetails.setFailedPasswordCount(0);
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorLoginRepository.save(dsaAdvisorLoginDetails);

		sendPasswordResetEmail(createPasswordFormVO.getEmail(), createPasswordFormVO.getUserId(), token);
		return new Response(ResponseCode.SUCCESS, null);
	}

	public void lockedAccount(String emailAddress, String userId) {
		String token = EmailTokenGenerator.generateRegistrationCode();
		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailAddress);
		dsaAdvisorAuthDetails.setPasswordResetReqDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorAuthDetails.setPasswordResetToken(token);

		dsaAdvisorAuthRepository.save(dsaAdvisorAuthDetails);
		sendAccountLockedEmail(emailAddress, userId, token);
	}

	public void resetStudentAuthDetails(String user) {
		DsaStudentAuthDetails dsaStudentAuthDetails = dsaStudentAuthDetailsRepository.findBySuid(user);
		if (null != dsaStudentAuthDetails) {
			dsaStudentAuthDetails.setIsLoggedIn(Boolean.FALSE);
			dsaStudentAuthDetails.setLastLoggedInDate(Timestamp.valueOf(LocalDateTime.now()));
			dsaStudentAuthDetailsRepository.save(dsaStudentAuthDetails);
		}
	}

	private void sendPasswordResetEmail(String emailId, String userId, String token) {
		// send account locked email
		logger.info("Sending password reset email to {} ", emailId);
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);
		String fullName = dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName();

		EmailContent emailContent = new EmailContent();
		emailContent.setBody(
				StringUtils.join(emailConfigProperties.getHost(), LOAD_SAVE_DEVICE_PAGE, token, "&userId=", userId));
		emailContent.setToAddress(emailId);
		emailContent.setFromAddress(NOREPLY_SAAS_GOV_UK);
		emailContent.setFromName(FROM_NAME_STUDENTS_AWARDS_AGENCY_SCOTLAND);
		emailContent.setSubject(DISABLED_STUDENT_PASSWORD_RESET);
		emailContent.setEmailTemplate(PASSWORD_RESET_EMAIL_FTLH);
		emailContent.setHomePage(StringUtils.join(emailConfigProperties.getHost(), START_PAGE));
		emailContent.setFullName(fullName); 
		emailSender.sendEmail(emailContent);
 
 
	}

	private void sendAccountLockedEmail(String emailId, String userId, String token) {
		// send account locked email
		logger.info("Sending account locked email to {} ", emailId);
		DsaAdvisor dsaAdvisor = dsaAdvisorRepository.findByEmailIgnoreCase(emailId);
		String fullName = dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName();

		EmailContent emailContent = new EmailContent();
		emailContent
				.setBody(StringUtils.join(emailConfigProperties.getHost(), RESET_PASSWORD, token, "&userId=", userId));
		emailContent.setToAddress(emailId);
		emailContent.setFromAddress(NOREPLY_SAAS_GOV_UK);
		emailContent.setFromName(FROM_NAME_STUDENTS_AWARDS_AGENCY_SCOTLAND);
		emailContent.setSubject(SUBJECT_RESET_YOUR_PASSWORD);
		emailContent.setEmailTemplate(ACCOUNT_LOCKED_EMAIL_FTLH);
		emailContent.setHomePage(StringUtils.join(emailConfigProperties.getHost(), START_PAGE));
		emailContent.setFullName(fullName);
		emailSender.sendEmail(emailContent);
	}

	private Date getAllowableActivationDate(DeviceMetadata existingDevice) {

		Date lastAllowableActivationDate = DateHelper.addMinutesToDate(existingDevice.getDeviceVerificationDate(),
				emailConfigProperties.getEmailActivationLinkAliveDurationTime());
		return lastAllowableActivationDate;
	}

	private Date getAllowableActivationDate(DsaAdvisorAuthDetails dsaAuthAdvisorDetails) {

		Date lastAllowableActivationDate = DateHelper.addMinutesToDate(dsaAuthAdvisorDetails.getActivationRequestDate(),
				emailConfigProperties.getEmailActivationLinkAliveDurationTime());
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

	private Date addDaysToDate(Timestamp ts, int days) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ts.getTime());
		calendar.add(Calendar.DATE, days);

		return new Date(calendar.getTimeInMillis());
	}

	private void extractAndSaveLoginDetails(CreatePasswordFormVO createPasswordFormVO,
											DsaAdvisorAuthDetails dsaAuthAdvisorDetails) {
		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
		dsaAdvisorLoginDetails.setUserId(dsaAuthAdvisorDetails.getUserId());
		dsaAdvisorLoginDetails.setUserName(dsaAuthAdvisorDetails.getEmail());
		dsaAdvisorLoginDetails.setRole(dsaAuthAdvisorDetails.getRoleName());

		saveSecretKeyAndPassword(createPasswordFormVO, dsaAdvisorLoginDetails);

		dsaAdvisorLoginDetails.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorLoginDetails.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaAdvisorLoginDetails.setIsActive(Boolean.TRUE);
		dsaAdvisorLoginRepository.save(dsaAdvisorLoginDetails);
	}

	private void saveSecretKeyAndPassword(CreatePasswordFormVO createPasswordFormVO,
										  DsaAdvisorLoginDetails dsaAdvisorLoginDetails) {
		byte[] salt = PasswordEncryptionHelper.getSalt();
		dsaAdvisorLoginDetails.setSalt(salt.toString());

		SecretKey secretKey = getSecretKey(createPasswordFormVO, salt);
		String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		dsaAdvisorLoginDetails.setSecretKey(encodedKey);
		dsaAdvisorLoginDetails.setPassword(encryptedPassword(secretKey, createPasswordFormVO));
		dsaAdvisorLoginDetails.setPreviousPassword1(dsaAdvisorLoginDetails.getPassword());
	}

	private SecretKey getSecretKey(CreatePasswordFormVO createPasswordFormVO, byte[] salt) {
		return PasswordEncryptionHelper.getSecretKey(createPasswordFormVO.getPassword(), salt);
	}

	private String encryptedPassword(SecretKey secretKey, CreatePasswordFormVO createPasswordFormVO) {
		return PasswordEncryptionHelper.encrypt(secretKey, createPasswordFormVO.getPassword());
	}

	private void saveStatusDeviceVerificationTokenDateAndSendEmail(DeviceMetadata existingDevice, String status,
																   String token, String emailId, DsaAdvisor dsaAdvisor) {
		existingDevice.setDeviceVerificationStatus(status);
		existingDevice.setDeviceVerificationToken(token);
		existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
		sendVerificationEmail(emailId, dsaAdvisor, token);
		deviceMetadataService.updateExistingDevice(existingDevice);
	}

	private void saveStatusDeviceVerificationTokenAndSendEmail(DeviceMetadata existingDevice, String status,
															   String token, String emailId, DsaAdvisor dsaAdvisor) {
		existingDevice.setDeviceVerificationStatus(status);
		existingDevice.setDeviceVerificationToken(token);
		sendVerificationEmail(emailId, dsaAdvisor, token);
		deviceMetadataService.updateExistingDevice(existingDevice);
	}

	private void sendVerificationEmail(String emailId, DsaAdvisor dsaAdvisor, String token) {
		// send verification email
		logger.info("Sending email to {} ", emailId);

		EmailContent emailContent = new EmailContent();
		emailContent.setBody(StringUtils.join(emailConfigProperties.getHost(), EMAIL_ACTIVATION_PATH, token, "&userId=",
				dsaAdvisor.getUserId()));
		emailContent.setToAddress(emailId);
		emailContent.setFromAddress(NOREPLY_SAAS_GOV_UK);
		emailContent.setFromName(FROM_NAME_STUDENTS_AWARDS_AGENCY_SCOTLAND);
		emailContent.setSubject("Sign in to your DSA Account");
		emailContent.setEmailTemplate(LOGIN_NOTIFICATION_EMAIL_FTLH);
		emailContent.setHomePage(StringUtils.join(emailConfigProperties.getHost(), START_PAGE));
		emailContent.setFullName(dsaAdvisor.getFirstName() + " " + dsaAdvisor.getLastName());
		emailSender.sendEmail(emailContent);
	}
}
