package uk.gov.saas.dsa.model;

public enum ActivationStatusType  {
	
    ACTIVE("Active"),
    EXPIRED("Expired"),
    ACTIVATION_REQUESTED("ActivationRequested"),
    ACTIVATION_RE_REQUESTED("ActivationReRequested"),
    ACTIVATION_LIMIT_EXCEEDED("ActivationLimitExceeded"),
    NON_ACTIVE("nonActive");

    private String message;

    ActivationStatusType( String message) {
        this.message = message;
    }

    public String getMessage( ) { return message; }
}
