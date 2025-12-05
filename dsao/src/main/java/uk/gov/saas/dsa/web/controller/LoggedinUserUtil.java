package uk.gov.saas.dsa.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.SecurityContextHelper;

import java.util.Arrays;
import java.util.Objects;

import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

public class LoggedinUserUtil {

	private static final Logger logger = LogManager.getLogger(LoggedinUserUtil.class);

	public static LoggedinUserType loggedinUserType() {
		LoggedinUserType userType = LoggedinUserType.ADVISOR;

		if (SecurityContextHelper.getLoggedInUserRole().equalsIgnoreCase("STUDENT")) {
			logger.info("Logged in user is : {} and his role is {}", SecurityContextHelper.getLoggedInUser(),
					SecurityContextHelper.getLoggedInUserRole());
			return LoggedinUserType.STUDENT;
		} else if (SecurityContextHelper.getLoggedInUserRole().equalsIgnoreCase("ADVISOR")) {
			logger.info("Logged in user is : {} and his role is {}", SecurityContextHelper.getLoggedInUser(),
					SecurityContextHelper.getLoggedInUserRole());
			return LoggedinUserType.ADVISOR;
		}

		userType = mockStudProfiles(userType);

		return userType;
	}

	// This Method can be deleted
	private static LoggedinUserType mockStudProfiles(LoggedinUserType userType) {
		String studentAccounts = System.getProperty("student.user.accounts");
		if (studentAccounts != null) {
			if (Arrays.asList(studentAccounts.split(",")).contains(getLoggedUserEmail())) {
				userType = LoggedinUserType.STUDENT;
			}
		}
		return userType;
	}

	public static void setLoggedinUserInToModel(Model model) {
		model.addAttribute(DSAConstants.LOGGEDIN_USER_TYPE, loggedinUserType().name());
	}

	public static boolean isAdvisor() {
		LoggedinUserType loggedinUserType = loggedinUserType();
		boolean isAdvisor = loggedinUserType.equals(LoggedinUserType.ADVISOR);
		logger.info("isAdvisor {}", isAdvisor);
		return isAdvisor;
	}

	private static String getLoggedUserEmail() {
		return (String) Objects.requireNonNull(securityContext()).getAuthentication().getPrincipal();
	}

	public static String getUserId() {
		String loggedInUserEmail = getLoggedUserEmail();
		logger.info("Looged in user email is {}", loggedInUserEmail);
		return loggedInUserEmail;
	}

}
