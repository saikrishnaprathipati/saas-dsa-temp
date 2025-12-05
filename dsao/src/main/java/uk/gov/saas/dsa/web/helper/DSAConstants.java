package uk.gov.saas.dsa.web.helper;

public class DSAConstants {
	private static final String BACK_SLASH = "/";

	private DSAConstants() {

	}

	public static final String LOGGEDIN_USER_TYPE = "LOGGEDIN_USER_TYPE";
	public static final String ACTION = "action";
	public static final String FINISH_ACTION = "FINISH";
	public static final String SAVE_AND_CONTINUE_ACTION = "SAVE AND CONTINUE";
	public static final String CONFIRM_AND_CONTINUE_ACTION = "CONFIRM AND CONTINUE";
	public static final String UPLOAD_ACTION = "UPLOAD";
	public static final String UPLOAD_FROM_SHOW_QUOTE_ACTION = "UPLOAD FROM SHOW QUOTE";
	public static final String BACK_ACTION = "BACK";
	public static final String ADD_ANOTHER_QUOTE = "ADD_ANOTHER_QUOTE";
	public static final String DASHBOARD_ACTION = "DASHBOARD";
	public static final String CONSUMABLES_SUMMARY_ACTION = "SUMMARY";
	public static final String NMPH_SUMMARY_ACTION = "SUMMARY";
	public static final String CANCEL_ACTION = "CANCEL";
	public static final String CHANGE_ACTION = "CHANGE";
	public static final String CHANGE_ACTION_FROM_SUMMARY = "CHANGE FROM SUMMARY";
	public static final String REMOVE_ACTION = "REMOVE";
	public static final String REMOVE_FROM_SHOW_QUOTE_ACTION = "REMOVE FROM SHOW QUOTE";
	public static final String SKIP_ACTION = "SKIP";
	public static final String I_AGREE_ACTION = "I AGREE";
	public static final String I_DO_NOT_AGREE_ACTION = "I DO NOT AGREE";
	public static final String STUDENT_ACTION = "STUDENT";
	public static final String ADVISOR_ACTION = "ADVISOR";
	public static final String APPLICATION_DASHBOARD_ACTION = "DASHBOARD";
	public static final String ALLOWANCES_SUMMARY_ACTION = "ALLOWANCES_SUMMARY";
	public static final String BACK_BUTTON_ACTION = "BACK_ACTION";
	public static final String BACK_TO_DASHBOARD = "BACK_TO_DASHBOARD";
	public static final String CANCEL_BUTTON_ACTION = "CANCEL_ACTION";
	public static final String APP_ID_SEQ = "APP_ID_SEQ";
	public static final String BACK_TO_SELECT_TRAVEL_EXP = "BACK_TO_SELECT_TRAVEL_EXP";
	public static final String COST = "cost";
	public static final String COMMA_DELIMETER = ",";

	public static final String APPLICATION_CHECK_URL_PATH = BACK_SLASH + "applicationCheck";
	public static final String APPLICATION_DETAILS_COURSE_URL_PATH = "application/course";

	public static final String STUDENT_REFERENCE_NUMBER = "studentReferenceNumber";
	public static final String SUID = "suid";
	public static final String DSA_APPLICATION_NUMBER = "dsaApplicationNumber";

	public static final String PREVIOUS_YEAR_ELIGIBILITY = "previousYearEligibility";

	public static final String ALLOWANCES_REMOVE_OPTION_REQUIRED = "allowances.remove.option.required";
	public static final String GENERIC_MESSAGE_ERROR = "generic.message.option";

	public static final String REDIRECT = "redirect:/";

	public static final String ADD_TRAVEL_EXPENSE_PATH = "addTravelExpense";
	public static final String APPLICATION_DASHBOARD_PATH = "applicationDashboard";
	public static final String INIT_CONSUMABLES_PATH = "initConsumables";
	public static final String CONSUMABLES_SUMMARY_PATH = "consumablesSummary";
	public static final String NMPH_SUMMARY_PATH = "nmphSummary";
	public static final String ASSESSMENT_FEE_SUMMARY_PATH = "assessmentFeeSummary";
	public static final String TRAVEL_EXP_SUMMARY_PATH = "travelExpSummary";
	public static final String ALLOWANCES_SUMMARY_PATH = "allowancesSummary";
	public static final String DISABILITY_DETAILS_PATH = "disabilityDetails";

	public static final String DISABILITY_DETAILS_SUMMARY_PATH = "disabilitiesSummary";

	public static final String SELECT_TRAVEL_EXPENSE_PATH = "selectTravelExpense";
	public static final String ERROR_PAGE = "error";
	public static final String STUDENT_FIRST_NAME = "studentFirstName";
	public static final String SESSION_CODE = "sessionCode";
	public static final String STUDENT_COURSE_YEAR = "studentCourseYear";
	public static final String APPLICATION_KEY_DATA_FORM_VO = "applicationKeyDataFormVO";
	public static final String RANGE = ".range";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String REMOVE_ITEM = "removeItem";
	public static final String REMOVE_QUOTE_FIELD = "removeQuote";
	public static final String UTF_8 = "UTF-8";
	public static final String COST_FORMAT = "%.2f";

	public static final String CONSUMABLES_CAP = "consumablesCap";

	public static final String NMPH_CAP = "nmphCap";
	public static final String EQUIPMENT_CAP = "equipmentCap";
	public static final String OTHER_TEXT_PATTERN = "[\\x00-\\x7F]+";
	public static final String COST_REGEX = "(?:\\d+)((\\d{1,3})*([\\,\\ ]\\d{3})*)(\\.\\d+)?";

	public static final String TWO_DIG_NO_REGEX = "^\\d{1,2}?$";
	public static final String THREE_DIG_NO_REGEX = "^\\d{1,3}?$";
	public static final String FIVE_DIG_NO_REGEX = "^\\d{1,5}?$";	
	public static final String JOURNEY_REGEX = "^\\d{1,3}?$";
	public static final String INVALID = ".invalid";
	public static final Integer PAGINATION_SIZE = 5;
	public static final Integer EQUIPMENT_ITEMS_LIMIT = 50;
	public static final Integer QUOTE_ITEMS_LIMIT = 5;

	public static final String LOGIN = "redirect:/login";
	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String GREY = "grey";
	public static final String YELLOW = "yellow";
	public static final String AMBER = "orange";
	public static final String CREATE_NEW_NOMINEE = "Create new nominee";
	public static final String MAIN_FUNDING = "Main funding";
	public static final String MAIN_FUNDING_AND_DISABLED_STUDENT_ALLOWANCES = MAIN_FUNDING + ", "
			+ "Disabled Students’ Allowance";
	public static final String DISABLED_STUDENT_ALLOWANCES = "Disabled Students’ Allowance";

	public static final String ADVISOR = "Advisor";

	public static final String HEI_TEAM = "HeiTeam";

	public static final String STUDENT = "Student";
	public static final String HEI_TEAM_EMAIL = "HEI_TEAM_EMAIL";
	public static final String HEI_NAME = "HEI_NAME";
	public static final String SAAS_REFERNECE_NUMBER = "SAAS_REFERNECE_NUMBER";
	public static final String DSA_TEAM_CONTACT_US_URL = "DSA_TEAM_CONTACT_US_URL";
	public static final String CONTACT_US_URL = "CONTACT_US_URL";
	public static final String STUDENT_CONTACT_US_URL = "STUDENT_CONTACT_US_URL";
	public static final String STUDENT_FULL_NAME = "STUDENT_FULL_NAME";
	public static final String STUD_FIRST_NAME = "STUDENT_FIRST_NAME";
	public static final String SAAS_LOGO_PATH = "classpath:/templates/mail-templates/SAAS_Logo.png";
	public static final String SAAS_LOGO_PNG = "SAAS_Logo.png";
	public static final String STUDENT_NOTIFICATION_TEMPLATE_HTML = "mail-templates/studentNotification.html";
	public static final String STUDENT_SUBMIT_NOTIFICATION_TEMPLATE_HTML = "mail-templates/studentSubmitConfiramtion.html";
	public static final String TO_EMAIL_ADDRESSES = "TO_EMAIL_ADDRESSES";
	public static final String LOCALDEV_PROFILES = "localdev,dev,dev1";
	public static final String CURRENT_SESSION = "CURRENT_SESSION";
}
