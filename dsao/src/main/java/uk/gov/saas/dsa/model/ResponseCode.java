package uk.gov.saas.dsa.model;

/**
 * The Enum ResponseCode.
 */
public enum ResponseCode {

	SUCCESS(0,"success"),
	
	FAILURE(1,"Unknown failure"),
	
	ACCOUNT_ALREADY_ACTIVATED(2, "Account already activated"),
	
	EMAIL_INVALID(3, "Email not in system"),
	
	TEMPORARY_LOCK(4, "Account is temporarily locked"),
	
	PERMANENT_LOCK(5, "Account is permanently locked"),
	
	PASSWORD_INVALID(6, "Password not valid for account"),
	
	ALREADY_LOGGED_IN(7, "Already logged in"),
	
	PASS(8, "Pass"),
	
	PASSWORD_USED_PREVIOUSLY(9, "Password used previously"),
	
	USER_HAS_ACCEPTED_PRIVACY_STATEMENT(10, "The user has not accepted the privacy statement"),
	
	USER_HAS_NOT_ACCEPTED_PRIVACY_STATEMENT(11, "The user has accepted the privacy statement"),
	
	EMAIL_UPDATED(12, "Email address has been updated"),

	/** The account not activated. */
	ACCOUNT_NOT_ACTIVATED(13, "Account not activated"),    
    
    ALREADY_TEMP_LOCKED(14, "User Already temporary locked"),
    
	ACTIVATION_LINK_EXPIRED(15, "Activation link Expired"),
	
	ACTIVATION_LIMIT_EXCEEDED(16, "Activation limit Exceeded"),

	ACTIVATION_REQUESTED(17, "Activation Requested"),
	
	PASSWORD_IN_USE(18, "New password is the same as the users current password"),
	
	ACTIVATION_TOKEN_INVALID(19, "Activation token is Invalid"),
	
	DEVICE_VERIFIED(20, "Device verified"),
	
	DEVICE_TOKEN_INVALID(21, "Device token is Invalid"),
	
	DEVICE_TOKEN_EXPIRED(22, "Device token is expired"),
	
	DEVICE_TOKEN_LIMIT_EXCEEDED(23, "Device token limit Exceeded"),

	DEVICE_TOKEN_REQUESTED(24, "Device token Requested"),
	
	DEVICE_NOT_VERIFIED(25, "Device not verified"),
	
	DEVICE_ALREADY_VERIFIED(26, "Device already verified"),
	
	ACCOUNT_INACTIVE(27, "Device token is Invalid"),
	
	LOCATION_INVALID(28, "Location Invalid"),
	
	RESET_TOKEN_INVALID(29, "Password Reset token is Invalid"),

	RESET_TOKEN_EXPIRED(30, "Password Reset token is Expired"),

	DEVICE_VERIFIED_LINK_EXPIRED(31, "Device link is expired");

	private final int responseCode;
	
	private final String message;
	
	ResponseCode(int code,String message) {
		this.responseCode = code;
		this.message = message;
	}
	
	public int getResponseCode() {
		return this.responseCode;
	}
	
	public String getMessage() {
		return this.message;
	}

}

