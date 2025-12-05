package uk.gov.saas.dsa.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.saas.dsa.domain.DeviceMetadata;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.email.EmailService;
import uk.gov.saas.dsa.model.DeviceVerificationStatusType;
import uk.gov.saas.dsa.persistence.DeviceMetadataRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceMetadataService {

	private static final String SEC_CH_UA_PLATFORM = "sec-ch-ua-platform";
	private static final String SEC_CH_UA = "sec-ch-ua";
	private static final String USER_AGENT = "user-agent";
	private static final String X_FORWARDED_FOR = "x-forwarded-for";
	private static final String DEFAULT_IP = "0.0.0.0";
	private static final String EMPTY = "";
	private static final String DOUBLE_QUOTES = "\"";
	private static final String COMMA = ",";
	private static final String REGEX_BROWSER = "[(;)]";
	private static final String REGEX_PLATFORM = "[ ]";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DeviceMetadataRepository deviceMetadataRepository;
	private final EmailService emailSender;

	@Autowired
	public DeviceMetadataService(DeviceMetadataRepository deviceMetadataRepository, EmailService emailSender) {
		this.emailSender = emailSender;
		this.deviceMetadataRepository = deviceMetadataRepository;
	}

	public DeviceMetadata isExistingDevice(HttpServletRequest request, String emailId) {
		Map<String, String> header = extractHeader(request);
		String browser = extractBrowser(header);
		String platform = header.getOrDefault(SEC_CH_UA_PLATFORM, StringUtils.EMPTY);
		String ipAddress = getIpAddress(header);

		logger.info("verifyDevice using {} and {} ", browser, platform);
		return findExistingDevice(emailId, browser, platform, ipAddress);
	}

	public void updateExistingDevice(DeviceMetadata existingDevice) {
		deviceMetadataRepository.save(existingDevice);
	}

	public DeviceMetadata findDeviceByToken(String token, String userId) {
		return deviceMetadataRepository.findByDeviceVerificationToken(token);
	}

	public void saveDevice(HttpServletRequest request, String token, DsaAdvisor dsaAdvisor) {
		Map<String, String> header = extractHeader(request);
		String deviceDetails = header.getOrDefault(USER_AGENT, StringUtils.EMPTY);
		String browser = extractBrowser(header);
		String platform = extractPlatform(header);
		String ipAddress = getIpAddress(header);

		DeviceMetadata newDevice = new DeviceMetadata();
		newDevice.setDeviceVerificationStatus(DeviceVerificationStatusType.DEVICE_TOKEN_REQUESTED.name());
		newDevice.setDeviceVerificationToken(token);
		newDevice.setDeviceVerificationDate(Timestamp.valueOf(LocalDateTime.now()));
		newDevice.setUserId(dsaAdvisor.getUserId());
		newDevice.setEmailId(dsaAdvisor.getEmail());
		newDevice.setBrowser(browser);
		newDevice.setPlatform(platform);
		newDevice.setIpAddress(StringUtils.isBlank(ipAddress) ? DEFAULT_IP : ipAddress);
		newDevice.setDeviceDetails(deviceDetails);
		newDevice.setLastLoggedIn(Timestamp.valueOf(LocalDateTime.now()));
		deviceMetadataRepository.save(newDevice);
	}

	private String getIpAddress(Map<String, String> header) {
		String ipAddress = header.getOrDefault(X_FORWARDED_FOR, StringUtils.EMPTY);
		if (StringUtils.isBlank(header.getOrDefault(X_FORWARDED_FOR, StringUtils.EMPTY))) {
			ipAddress = DEFAULT_IP;
		}
		return ipAddress;
	}

	private String extractBrowser(Map<String, String> header) {
		String browser = header.getOrDefault(SEC_CH_UA, StringUtils.EMPTY);
		logger.info("verifyDevice using browser {}  ", browser);

		if (browser.contains("Not") || browser.equalsIgnoreCase(StringUtils.EMPTY)) {
			String deviceDetails = header.getOrDefault(USER_AGENT, StringUtils.EMPTY);
			if(deviceDetails.equalsIgnoreCase(StringUtils.EMPTY)){
				return browser;
			}
			browser = deviceDetails.split(REGEX_BROWSER)[1].trim();
		} else if(browser.contains(COMMA)) {
			browser = browser.split(COMMA)[1];
		} else {
			return browser;
		}

		return browser.contains(";") ? browser.split(";")[0] : browser;
	}

	private String extractPlatform(Map<String, String> header) {
		String platform = header.getOrDefault(SEC_CH_UA_PLATFORM, StringUtils.EMPTY);
		if (!platform.equalsIgnoreCase(StringUtils.EMPTY)) {
			return platform;
		} else {
			String[] deviceDetails = header.getOrDefault(USER_AGENT, StringUtils.EMPTY).split(REGEX_PLATFORM);
			return deviceDetails[deviceDetails.length - 1].trim();
		}
	}

	private Map<String, String> extractHeader(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String value = request.getHeader(key);
			result.put(key, value.replaceAll(DOUBLE_QUOTES, EMPTY));
		}

		return result;
	}

	private DeviceMetadata findExistingDevice(String emailId, String browser, String platform, String ipAddress) {
		List<DeviceMetadata> knownDevices = deviceMetadataRepository.findByEmailIdIgnoreCase(emailId);
		logger.info("knownDevices {} for emailId {} ", knownDevices.size(), emailId);

		for (DeviceMetadata existingDevice : knownDevices) {
			if (existingDevice.getBrowser().equalsIgnoreCase(browser)
					&& existingDevice.getPlatform().equalsIgnoreCase(platform)
					&& existingDevice.getIpAddress().equalsIgnoreCase(ipAddress)) {
				logger.info("knownDevice found for ipAddress {} and browser {} ", ipAddress, browser);
				return existingDevice;
			}
		}
		logger.info("knownDevice not found for ipAddress {} and browser {} ", ipAddress, browser);

		return null;
	}
}
