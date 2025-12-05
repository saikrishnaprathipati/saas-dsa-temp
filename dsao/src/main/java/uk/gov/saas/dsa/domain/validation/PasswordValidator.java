package uk.gov.saas.dsa.domain.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.domain.helpers.PasswordEncryptionHelper;
import uk.gov.saas.dsa.service.AdvisorLoginService;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;
import uk.gov.saas.dsa.web.helper.BadPassPhraseChecker;

public class PasswordValidator {

	private static final int PASSWORD_MIN_LENGTH = 14;
	private static final String DSA_PASSWORD_LENGTH = "dsa.password.length";
	private static final String DSA_PASSWORD_REQUIRED = "dsa.password.required";
	private static final String DSA_PASSWORD_DOESNOTMATCH = "dsa.password.doesnotmatch";
	private static final String DSA_PASSWORD_PWNED = "dsa.password.pwned";
	private static final String DSA_PASSWORD_CANNOT_BE_SAME_AS_OLD_PASSWORD = "dsa.password.oldpassword";
	private static final String CONFIRM_PASSWORD = "confirmPassword";
	private static final String PASSWORD = "password";
	
	private final AdvisorLoginService advisorLoginService;
	
	@Autowired
	public PasswordValidator(AdvisorLoginService advisorLoginService) {
		this.advisorLoginService = advisorLoginService;
	}

	public void validate(Object target, Errors errors) {
		CreatePasswordFormVO createPasswordFormVO = (CreatePasswordFormVO) target;
		validatePassword(errors, createPasswordFormVO);
	}

	private void validatePassword(Errors errors, CreatePasswordFormVO createPasswordFormVO) {
		String password = createPasswordFormVO.getPassword().toLowerCase();
		String confirmPassword = createPasswordFormVO.getConfirmPassword().toLowerCase();
		if (StringUtils.isBlank(password)) {
			errors.rejectValue(PASSWORD, DSA_PASSWORD_REQUIRED);
		}
		if (password.length() < PASSWORD_MIN_LENGTH) {
			errors.rejectValue(PASSWORD, DSA_PASSWORD_LENGTH);
		}
		if (!password.equalsIgnoreCase(confirmPassword)) {
			errors.rejectValue(CONFIRM_PASSWORD, DSA_PASSWORD_DOESNOTMATCH);
		}
		if (BadPassPhraseChecker.isPasswordPwned(password)) {
			errors.rejectValue(PASSWORD, DSA_PASSWORD_PWNED);
		}
		
		DsaAdvisorLoginDetails dsaAdvisorLoginDetails = advisorLoginService.findByEmail(createPasswordFormVO.getEmail());

		if(null != dsaAdvisorLoginDetails) {
			if (PasswordEncryptionHelper.checkPassword(dsaAdvisorLoginDetails.getSecretKey(), password, dsaAdvisorLoginDetails.getPassword())) {
				errors.rejectValue(PASSWORD, DSA_PASSWORD_CANNOT_BE_SAME_AS_OLD_PASSWORD);
			}
		}
	}

	public static boolean matches(String regex, String value) {
		return (value.matches(regex));
	}
}
