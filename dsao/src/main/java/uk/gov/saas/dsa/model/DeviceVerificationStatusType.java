package uk.gov.saas.dsa.model;

public enum DeviceVerificationStatusType  {
	
	DEVICE_VERIFIED("DeviceVerified"),
    DEVICE_TOKEN_EXPIRED("DeviceTokenExpired"),
    DEVICE_TOKEN_REQUESTED("DeviceTokenRequested"),
    DEVICE_TOKEN_RE_REQUESTED("DeviceTokenReRequested"),
    DEVICE_TOKEN_LIMIT_EXCEEDED("DeviceTokenLimitExceeded"),
    DEVICE_NOT_VERIFIED("DeviceNotVerified");


    private String message;

    DeviceVerificationStatusType( String message) {
        this.message = message;
    }

    public String getMessage( ) { return message; }
}
