package uk.gov.saas.dsa.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;
import uk.gov.saas.dsa.domain.DeviceMetadata;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.persistence.DeviceMetadataRepository;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class DeviceMetadataServiceTest {

    private DeviceMetadataService deviceMetadataService;
    @MockitoBean
    private DeviceMetadataRepository deviceMetadataRepository;

    @MockitoBean
    private EmailService emailSender;

    @MockitoBean
    private Model model;

    private String emailId = "test@gmail.com";

    @BeforeEach
    public void setUp() {
        deviceMetadataService = new DeviceMetadataService(deviceMetadataRepository, emailSender);
    }

    @Test
    public void testVerifyDevice_existingDevice() {
        HttpServletRequest request = new MockHttpServletRequest();
        DeviceMetadata device = deviceMetadataService.isExistingDevice(request, emailId);
        assertNull(device);
    }
}
