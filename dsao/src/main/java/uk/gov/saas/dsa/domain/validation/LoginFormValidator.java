package uk.gov.saas.dsa.domain.validation;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.domain.helpers.PasswordEncryptionHelper;
import uk.gov.saas.dsa.service.AdvisorLoginService;
import uk.gov.saas.dsa.service.LoginService;
import uk.gov.saas.dsa.vo.LoginFormVO;
import uk.gov.saas.dsa.web.helper.DateHelper;

@Component
public class LoginFormValidator {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final int PASSWORD_MIN_LENGTH = 14;
	 
	private static final String DSA_PASSWORD_LENGTH = "dsa.login.password.length";
	private static final String DSA_PASSWORD_REQUIRED = "dsa.password.required";
	private static final String DSA_PASSWORD_INCORRECT = "dsa.login.password.incorrect";
	private static final String DSA_ACCOUNT_PERMANENT_LOCKED = "dsa.login.account.permanent.locked";

	private static final String DSA_ACCOUNT_TEMPORARY_LOCKED = "dsa.login.account.temporary.locked";
	private static final String DSA_VALID_EMAIL_REQUIRED = "dsa.validEmailAddress.required";
	private static final String PASSWORD = "password";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final int THREE_TEMPORARY_LIMIT = 3;
	private static final int SIX_PERMANENT_LIMIT = 6;

	private DSAEmailConfigProperties emailConfigProperties;

 

	private AdvisorLoginService advisorLoginService;
	private LoginService loginService;

	@Autowired
	public LoginFormValidator(AdvisorLoginService advisorLoginService, LoginService loginService, DSAEmailConfigProperties emailConfigProperties) {
		this.advisorLoginService = advisorLoginService;
		this.loginService = loginService;
		this.emailConfigProperties = emailConfigProperties;
	}

	public void validate(Object target, Errors errors) {
		LoginFormVO loginFormVO = (LoginFormVO) target;
		validateLoginForm(errors, loginFormVO);
	}

	private void validateLoginForm(Errors errors, LoginFormVO loginFormVO) {
		logger.info("LoginFormValidator validateLoginForm {} ", loginFormVO);

		String password = loginFormVO.getPassword().toLowerCase();
		String emailAddress = loginFormVO.getEmailAddress().toLowerCase();

		if (StringUtils.isBlank(password)) {
			errors.rejectValue(PASSWORD, DSA_PASSWORD_REQUIRED);
		}
		if (password.length() < PASSWORD_MIN_LENGTH) {
			errors.rejectValue(PASSWORD, DSA_PASSWORD_LENGTH);
		}

		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = advisorLoginService.findByEmail(emailAddress);
		logger.info("dsaAdvisorLoginDetails {}", dsaAdvisorLoginDetails);

		if (null == dsaAdvisorLoginDetails) {
			errors.rejectValue(EMAIL_ADDRESS, DSA_VALID_EMAIL_REQUIRED);
		} else {
			int failedPasswordCount = dsaAdvisorLoginDetails.getFailedPasswordCount();
			if (isPasswordIncorrect(password, dsaAdvisorLoginDetails)) {
				failedPasswordCount++;
				advisorLoginService.saveFailedLoginDetails(failedPasswordCount, dsaAdvisorLoginDetails);
				if (failedPasswordCount <= THREE_TEMPORARY_LIMIT) {
					errors.rejectValue(PASSWORD, DSA_PASSWORD_INCORRECT);
				} else if (failedPasswordCount < SIX_PERMANENT_LIMIT) {
					errors.rejectValue(PASSWORD, DSA_ACCOUNT_TEMPORARY_LOCKED);
				} else {
					loginService.lockedAccount(emailAddress, dsaAdvisorLoginDetails.getUserId());
					errors.rejectValue(PASSWORD, DSA_ACCOUNT_PERMANENT_LOCKED);
				}
			} else {
				Date lastAllowableActivationDate = getAllowableActivationDate(dsaAdvisorLoginDetails);
				if ((failedPasswordCount >= THREE_TEMPORARY_LIMIT
						&& failedPasswordCount < SIX_PERMANENT_LIMIT)
						&& lastAllowableActivationDate.after(Timestamp.valueOf(LocalDateTime.now()))) {
					errors.rejectValue(PASSWORD, DSA_ACCOUNT_TEMPORARY_LOCKED);
				} else if(failedPasswordCount >= SIX_PERMANENT_LIMIT) {
					loginService.lockedAccount(emailAddress, dsaAdvisorLoginDetails.getUserId());
					errors.rejectValue(PASSWORD, DSA_ACCOUNT_PERMANENT_LOCKED);
				}
			}
		}
	}

	private Date getAllowableActivationDate(DsaAdvisorLoginDetails dsaAdvisorLoginDetails) {
		Date lastAllowableActivationDate = DateHelper.addMinutesToDate(dsaAdvisorLoginDetails.getLastUpdatedDate(),
				emailConfigProperties.getEmailActivationLinkAliveDurationTime());
		return lastAllowableActivationDate;
	}

	private boolean isPasswordIncorrect(String password, DsaAdvisorLoginDetails dsaAdvisorLoginDetails) {
		return password.length() >= PASSWORD_MIN_LENGTH && !PasswordEncryptionHelper
				.checkPassword(dsaAdvisorLoginDetails.getSecretKey(), password, dsaAdvisorLoginDetails.getPassword());
	}

}
