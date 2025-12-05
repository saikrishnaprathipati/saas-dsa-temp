package uk.gov.saas.dsa.web.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

@Component
public class BadPassPhraseChecker {

	private final static Logger logger = LogManager.getLogger(BadPassPhraseChecker.class);
	private static final String API_PWNEDPASSWORDS = "https://api.pwnedpasswords.com/range/";
	private static final String HOSTNAME = "webproxy-devusers.scotland.gov.uk";
	private static final String SYSTEM_PROXIES = "java.net.useSystemProxies";

	public static boolean isPasswordPwned(String password) {

		try {
			String url = API_PWNEDPASSWORDS + sha1Hex(password).substring(0, 5);
			System.setProperty(SYSTEM_PROXIES, "true");
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(HOSTNAME, 80));
			requestFactory.setProxy(proxy);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return map(response.getStatusCodeValue(), response.getBody(), password);
		} catch (ResourceAccessException exception) {
			logger.error("Exception while accessing the API, {}", exception.getMessage());
		}
		return false;
	}

	public static String sha1Hex(String password) {
		return DigestUtils.sha1Hex(password).toUpperCase();
	}

	public static boolean map(int responseStatus, String responseBody, String password) {
		String hashSuffix = sha1Hex(password);
		logger.info("Response for hashSuffix {}", hashSuffix);
		String[] lines = responseBody.split("\\r?\\n");
		for (String line : lines) {
			// the response strings consist of HASH_SUFFIX:COUNT
			// https://haveibeenpwned.com/API/v3#PwnedPasswordsPadding)
			if (line.split(":")[0].equals(hashSuffix) && !line.split(":")[1].equals("0")) {
				logger.info("returning true ");
				return true;
			}
		}
		logger.info("returning false ");
		return false;
	}
}
