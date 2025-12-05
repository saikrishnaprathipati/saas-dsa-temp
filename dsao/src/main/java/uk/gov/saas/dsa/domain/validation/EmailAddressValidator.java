package uk.gov.saas.dsa.domain.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;

import uk.gov.saas.dsa.model.ActivationStatusType;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;

public class EmailAddressValidator {

	private static final String DSA_EMAIL_ADDRESS_ALREADY_REGISTERED = "dsa.emailAddress.already.registered";
	private static final String DSA_EMAIL_ADDRESS_NOTFOUND = "dsa.emailAddress.notfound";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final String DSA_VALID_EMAIL_ADDRESS_REQUIRED = "dsa.validEmailAddress.required";
	private static final String DSA_EMAIL_ADDRESS_REQUIRED = "dsa.emailAddress.required";
	private static final String EMAIL_REGEX = "^\\s*(?=.{1,80}$)([a-zA-Z0-9!#$%&'*+-/=?^_`{|}~]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\s*$";

	private final Logger logger = LogManager.getLogger(this.getClass());

	private AdvisorLookupService advisorLookupService;

	public EmailAddressValidator(AdvisorLookupService advisorLookupService) {
		this.advisorLookupService = advisorLookupService;
	}

	public void validate(Object target, Errors errors) {
		logger.info("EmailAddressValidator {}", target);

		CreateAccountFormVO createAccountForm = (CreateAccountFormVO) target;
		validateEmailAddress(errors, createAccountForm);
	}

	private void validateEmailAddress(Errors errors, CreateAccountFormVO createAccountForm) {
		String email = createAccountForm.getEmailAddress().toLowerCase();
		if (StringUtils.isBlank(email)) {
			errors.rejectValue(EMAIL_ADDRESS, DSA_EMAIL_ADDRESS_REQUIRED);
		} else if (!matches(EMAIL_REGEX, createAccountForm.getEmailAddress())) {
			errors.rejectValue(EMAIL_ADDRESS, DSA_VALID_EMAIL_ADDRESS_REQUIRED);
		} else if (null == advisorLookupService.findByEmail(email)) {
			errors.rejectValue(EMAIL_ADDRESS, DSA_EMAIL_ADDRESS_NOTFOUND);
		} else {
			String activationStatus = advisorLookupService.findAdvisorActivationStatusByEmail(email);
			if (null != activationStatus
					&& activationStatus.equalsIgnoreCase(ActivationStatusType.ACTIVE.getMessage())) {
				errors.rejectValue(EMAIL_ADDRESS, DSA_EMAIL_ADDRESS_ALREADY_REGISTERED);
				createAccountForm.setIsEmailAlreadyActive(true);
			}
		}
	}

	public static boolean matches(String regex, String value) {
		return (value.matches(regex));
	}
}
