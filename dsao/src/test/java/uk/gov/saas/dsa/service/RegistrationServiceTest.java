package uk.gov.saas.dsa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static uk.gov.saas.dsa.model.ActivationStatusType.ACTIVATION_REQUESTED;
import static uk.gov.saas.dsa.model.ActivationStatusType.ACTIVATION_RE_REQUESTED;
import static uk.gov.saas.dsa.model.ActivationStatusType.ACTIVE;
import static uk.gov.saas.dsa.model.ActivationStatusType.EXPIRED;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;

import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorRepository;

@ExtendWith(SpringExtension.class)
class RegistrationServiceTest {

    private RegistrationService registrationService;

    @MockitoBean
    private DsaAdvisorAuthRepository dsaAdvisorAuthRepository;

    @MockitoBean
    private EmailService emailSender;

    @MockitoBean
    private DsaAdvisorRepository dsaAdvisorRepository;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
	private DSAEmailConfigProperties dsaEmailConfigProperties;

    @MockitoBean
    private Model model;

    private final String emailId = "test@gmail.com";
    @BeforeEach
    public void setUp() {
        registrationService = new RegistrationService(dsaAdvisorAuthRepository, emailSender, dsaAdvisorRepository, loginService, dsaEmailConfigProperties);
        Mockito.when(dsaEmailConfigProperties.getEmailActivationLinkAliveDurationTime()).thenReturn(15);
    }

    @Test
    public void testRequestActivation_ActivationRequested() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setEmail(emailId);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(ACTIVATION_REQUESTED.name());
        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));
        dsaAdvisorAuthDetails.setEmail(emailId);

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACCOUNT_NOT_ACTIVATED, response.getResponseCode());
        assertEquals(ACTIVATION_RE_REQUESTED.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void testRequestActivation_AccountNotActivated() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(null);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACCOUNT_NOT_ACTIVATED, response.getResponseCode());
    }

    @Test
    public void testRequestActivation_EmailInvalid() {
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(null);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.EMAIL_INVALID, response.getResponseCode());
    }

    @Test
    public void testCompleteRegistration_TokenInvalid() {

        Response response = registrationService.checkAdvisorDetails("token", "userId");
        assertEquals(ResponseCode.ACTIVATION_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void testCompleteRegistration_Success() {
        DsaAdvisorAuthDetails dsaAuthAdvisorDetails = new DsaAdvisorAuthDetails();
        dsaAuthAdvisorDetails.setActivationToken("123");
        dsaAuthAdvisorDetails.setActivationStatus(ACTIVATION_REQUESTED.getMessage());
        dsaAuthAdvisorDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));

        when(dsaAdvisorAuthRepository.findByActivationToken("123")).thenReturn(dsaAuthAdvisorDetails);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(any())).thenReturn(dsaAdvisor);

        Response response = registrationService.checkAdvisorDetails("123", "A123");
        assertEquals(ResponseCode.SUCCESS, response.getResponseCode());
    }

    @Test
    public void testRequestActivation_ActivationRequestedAndLinkExpired() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(ACTIVATION_REQUESTED.name());

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(yesterday.toEpochMilli()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACCOUNT_NOT_ACTIVATED, response.getResponseCode());
        assertEquals(ACTIVATION_REQUESTED.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void testRequestActivation_ActivationReRequestedAndLinkExpired() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setEmail(emailId);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(ACTIVATION_RE_REQUESTED.name());
        dsaAdvisorAuthDetails.setEmail(emailId);

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(yesterday.toEpochMilli()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACCOUNT_NOT_ACTIVATED, response.getResponseCode());
        assertEquals(ACTIVATION_REQUESTED.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void testRequestActivation_ActivationReRequestedAndLinkDidNotExpire() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setEmail(emailId);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(ACTIVATION_RE_REQUESTED.name());
        dsaAdvisorAuthDetails.setEmail(emailId);

        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACTIVATION_LIMIT_EXCEEDED, response.getResponseCode());
        assertEquals(ACTIVATION_RE_REQUESTED.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void testRequestActivation_ActiveStatus() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setEmail(emailId);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(ACTIVE.name());
        dsaAdvisorAuthDetails.setEmail(emailId);

        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACCOUNT_ALREADY_ACTIVATED, response.getResponseCode());
        assertEquals(ACTIVE.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void testRequestActivation_ExpiredStatus() {
        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setEmail(emailId);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(EXPIRED.name());
        dsaAdvisorAuthDetails.setEmail(emailId);

        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        Response response = registrationService.requestActivation(emailId);
        assertEquals(ResponseCode.ACTIVATION_LINK_EXPIRED, response.getResponseCode());
        assertEquals(EXPIRED.name(),
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }

    @Test
    public void test_checkActivationStatus() {
        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationToken("123");
        dsaAdvisorAuthDetails.setActivationStatus(EXPIRED.name());
        dsaAdvisorAuthDetails.setActivationRequestDate(new Timestamp(System.currentTimeMillis()));

        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisorAuthDetails);

        String response = registrationService.checkActivationStatus(emailId);
        assertEquals(response,
                dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId).getActivationStatus());
    }
}
