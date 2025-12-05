package uk.gov.saas.dsa.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;
import uk.gov.saas.dsa.config.email.DSAEmailConfigProperties;
import uk.gov.saas.dsa.domain.DeviceMetadata;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.domain.DsaAdvisorLoginDetails;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.DeviceVerificationStatusType;
import uk.gov.saas.dsa.model.Response;
import uk.gov.saas.dsa.model.ResponseCode;
import uk.gov.saas.dsa.persistence.*;
import uk.gov.saas.dsa.vo.CreatePasswordFormVO;
import uk.gov.saas.dsa.vo.SaveDeviceFormVO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class LoginServiceTest {

    private LoginService loginService;

    @MockitoBean
    private DsaAdvisorLoginRepository dsaAdvisorLoginRepository;
    @MockitoBean
    private DeviceMetadataRepository deviceMetadataRepository;
    @MockitoBean
    private EmailService emailSender;

    @MockitoBean
    private DsaAdvisorAuthRepository dsaAdvisorAuthRepository;

    @MockitoBean
    private DsaAdvisorRepository dsaAdvisorRepository;
    @MockitoBean
    private DsaStudentAuthDetailsRepository dsaStudentAuthDetailsRepository;
    @MockitoBean
    private DeviceMetadataService deviceMetadataService;
    
    @MockitoBean
    private DSAEmailConfigProperties emailConfigProperties;
    
    @MockitoBean
    private Model model;

    private final String emailId = "test@gmail.com";

    @BeforeEach
    public void setUp() {
        loginService = new LoginService(dsaAdvisorLoginRepository, dsaAdvisorAuthRepository, emailSender,
                dsaAdvisorRepository, deviceMetadataService, emailConfigProperties,dsaStudentAuthDetailsRepository);
        Mockito.when(emailConfigProperties.getEmailActivationLinkAliveDurationTime()).thenReturn(15);
    }

    @Test
    public void testVerifyDevice_emailInvalid() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(dsaAdvisorLoginDetails);

        Response response = loginService.verifyDevice(request, emailId);
        assertEquals(ResponseCode.EMAIL_INVALID, response.getResponseCode());
    }

    @Test
    public void testVerifyDevice_deviceNotVerified() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(dsaAdvisorLoginDetails);

        Response response = loginService.verifyDevice(request, emailId);
        assertEquals(ResponseCode.DEVICE_NOT_VERIFIED, response.getResponseCode());
    }

    @Test
    public void testVerifyDevice_existingDevice() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);

        DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(dsaAdvisorLoginDetails);

        Response response = loginService.verifyDevice(request, emailId);
        assertEquals(ResponseCode.DEVICE_NOT_VERIFIED, response.getResponseCode());
    }

    @Test
    public void testVerifyDevice_deviceVerified() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        existingDevice.setRememberDevice(Boolean.TRUE);
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);

        DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(dsaAdvisorLoginDetails);

        Response response = loginService.verifyDevice(request, emailId);
        assertEquals(ResponseCode.DEVICE_VERIFIED, response.getResponseCode());
    }

    @Test
    public void testVerifyDevice_deviceVerifiedAndExpired() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(8)));
        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        existingDevice.setRememberDevice(Boolean.TRUE);
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);

        DsaAdvisorLoginDetails dsaAdvisorLoginDetails = new DsaAdvisorLoginDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(dsaAdvisorLoginDetails);

        Response response = loginService.verifyDevice(request, emailId);
        assertEquals(ResponseCode.DEVICE_NOT_VERIFIED, response.getResponseCode());
    }

    @Test
    public void test_getExistingDevice() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        existingDevice.setRememberDevice(Boolean.TRUE);
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);

        DeviceMetadata response = loginService.getExistingDevice(request, emailId);
        assertEquals(response, existingDevice);
    }

    @Test
    public void test_ForgottenPassword_invalid() {
        when(dsaAdvisorAuthRepository.findByPasswordResetToken("1234")).thenReturn(null);

        Response response = loginService.forgottenPassword("1234", "A123");
        assertEquals(ResponseCode.RESET_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_ForgottenPassword_valid() {
        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        dsaAdvisorAuthDetails.setActivationRequestDate((Timestamp.valueOf(LocalDateTime.now())));
        DsaAdvisor dsaAdvisor = new DsaAdvisor();

        when(dsaAdvisorAuthRepository.findByPasswordResetToken("1234")).thenReturn(dsaAdvisorAuthDetails);
        when(dsaAdvisorRepository.findByEmailIgnoreCase(any())).thenReturn(dsaAdvisor);

        Response response = loginService.forgottenPassword("1234", "A123");
        assertEquals(ResponseCode.SUCCESS, response.getResponseCode());
    }

    @Test
    public void test_rememberDeviceAndContinue() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        SaveDeviceFormVO saveDeviceFormVO = new SaveDeviceFormVO();
        saveDeviceFormVO.setEmail("test@gmail.com");

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        existingDevice.setRememberDevice(Boolean.TRUE);
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);

        loginService.rememberDeviceAndContinue(request, saveDeviceFormVO);
        Mockito.verify(deviceMetadataService, times(1)).updateExistingDevice(any());
    }

    @Test
    public void test_requestEmailActivation() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAdvisor);

        DeviceMetadata existingDevice = new DeviceMetadata();
        existingDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        existingDevice.setRememberDevice(Boolean.TRUE);

        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);
        Response deviceNotVerified = loginService.requestEmailVerification(request, emailId);
        assertEquals(ResponseCode.DEVICE_NOT_VERIFIED, deviceNotVerified.getResponseCode());

        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_VERIFIED.name());
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);
        Response deviceVerified = loginService.requestEmailVerification(request, emailId);
        assertEquals(ResponseCode.DEVICE_ALREADY_VERIFIED, deviceVerified.getResponseCode());

        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_EXPIRED.name());
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);
        Response deviceTokenExpired = loginService.requestEmailVerification(request, emailId);
        assertEquals(ResponseCode.DEVICE_TOKEN_EXPIRED, deviceTokenExpired.getResponseCode());

        existingDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_RE_REQUESTED.name());
        when(deviceMetadataService.isExistingDevice(request, emailId)).thenReturn(existingDevice);
        Response deviceTokenRequested = loginService.requestEmailVerification(request, emailId);
        assertEquals(ResponseCode.DEVICE_ALREADY_VERIFIED, deviceTokenRequested.getResponseCode());
    }

    @Test
    public void test_saveAdvisorLoginDetails() {
        CreatePasswordFormVO createPasswordFormVO = new CreatePasswordFormVO();
        createPasswordFormVO.setEmail(emailId);
        createPasswordFormVO.setPassword("password");
        DsaAdvisorAuthDetails dsaAuthAdvisorDetails = new DsaAdvisorAuthDetails();
        when(dsaAdvisorLoginRepository.findByUserNameIgnoreCase(emailId)).thenReturn(null);

        Response response = loginService.saveAdvisorLoginDetails(createPasswordFormVO, dsaAuthAdvisorDetails);
        assertEquals(ResponseCode.SUCCESS, response.getResponseCode());
    }

    @Test
    public void test_completeRegistration() {
        CreatePasswordFormVO createPasswordFormVO = new CreatePasswordFormVO();
        createPasswordFormVO.setEmail(emailId);
        createPasswordFormVO.setPassword("password");
        DsaAdvisorAuthDetails dsaAuthAdvisorDetails = new DsaAdvisorAuthDetails();
        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(emailId)).thenReturn(dsaAuthAdvisorDetails);

        Response response = loginService.completeRegistration(createPasswordFormVO);
        assertEquals(ResponseCode.SUCCESS, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails_expired() {
        DeviceMetadata savedDevice = new DeviceMetadata();
        savedDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_EXPIRED.name());
        savedDevice.setRememberDevice(Boolean.TRUE);

        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(savedDevice);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails_Invalid() {
        DeviceMetadata savedDevice = new DeviceMetadata();
        savedDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        savedDevice.setRememberDevice(Boolean.TRUE);

        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(savedDevice);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails_DeviceNotExists() {
        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(null);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails_DeviceExpired() {
        DeviceMetadata savedDevice = new DeviceMetadata();
        savedDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_EXPIRED.name());
        savedDevice.setRememberDevice(Boolean.TRUE);

        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(savedDevice);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails_DeviceVerified() {
        DeviceMetadata savedDevice = new DeviceMetadata();
        savedDevice.setDeviceVerificationToken("12345");
        savedDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_VERIFIED.name());
        savedDevice.setRememberDevice(Boolean.TRUE);

        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(savedDevice);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_VERIFIED_LINK_EXPIRED, response.getResponseCode());
    }

    @Test
    public void test_saveDeviceDetails() {
        DeviceMetadata savedDevice = new DeviceMetadata();
        savedDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
        savedDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
        savedDevice.setRememberDevice(Boolean.TRUE);
        savedDevice.setEmailId(emailId);
        when(deviceMetadataService.findDeviceByToken("1234", "A123")).thenReturn(savedDevice);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        dsaAdvisor.setAdvisorId(1L);
        dsaAdvisor.setEmail("");
        dsaAdvisor.setFirstName("");
        dsaAdvisor.setLastName("");
        dsaAdvisor.setInstitution("");
        dsaAdvisor.setTeamEmail("");
        dsaAdvisor.setRoleName("");
        when(dsaAdvisorRepository.findByEmailIgnoreCase(savedDevice.getEmailId())).thenReturn(dsaAdvisor);

        Response response = loginService.saveDeviceDetails("1234", "A123");
        assertEquals(ResponseCode.DEVICE_TOKEN_INVALID, response.getResponseCode());
    }

    @Test
    public void test_lockedAccount() {
        DsaAdvisorAuthDetails dsaAdvisorAuthDetails = new DsaAdvisorAuthDetails();
        when(dsaAdvisorAuthRepository.findByEmailIgnoreCase(any())).thenReturn(dsaAdvisorAuthDetails);

        DsaAdvisor dsaAdvisor = new DsaAdvisor();
        when(dsaAdvisorRepository.findByEmailIgnoreCase("1234")).thenReturn(dsaAdvisor);

        loginService.lockedAccount("1234", "A123");
        Mockito.verify(emailSender, times(1)).sendEmail(any());
    }
}



