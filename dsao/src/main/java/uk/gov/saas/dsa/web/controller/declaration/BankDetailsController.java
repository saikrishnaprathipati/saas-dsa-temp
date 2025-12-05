package uk.gov.saas.dsa.web.controller.declaration;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.addErrorMessage;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatAccountNumber;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.formatSortcode;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ALLOWANCES_REMOVE_OPTION_REQUIRED;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.I_AGREE_ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.DSAConstants.REMOVE_ITEM;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SAVE_AND_CONTINUE_ACTION;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;
import static uk.gov.saas.dsa.web.helper.ValidationHelper.validateNumber;

import java.util.Arrays;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.model.PaymentFor;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatus;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.BankAccountService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.ServiceUtil;
import uk.gov.saas.dsa.vo.AddBankAccountFormVO;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.vo.ChooseBankAccountFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.vo.nmph.RemoveItemFormVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.controller.allowances.NMPHController;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

@Controller
public class BankDetailsController {
	private static final String BANK_ACCOUNT_SORT_CODE_INVALID = "bankAccount.sortCode.invalid";
	private static final String INIT_REMOVE_BANK_ACCOUNT = "INIT_REMOVE_BANK_ACCOUNT";
	private static final String REMOVE_BANK_ACCOUNT = "REMOVE_BANK_ACCOUNT";
	private static final String REMOVE_ITEM_FORM_VO = "removeItemFormVO";
	private static final String SORT_CODE = "sortCode";
	private static final String DUPLICATE_BANK_DETAILS = "duplicateBankDetails";
	private static final String BANK_ACCOUNT_ACCOUNT_NUMBER_INVALID = "bankAccount.accountNumber.invalid";
	private static final String ACCOUNT_NUMBER = "accountNumber";
	private static final String BANK_ACCOUNT_SUMMARY = "BANK_ACCOUNT_SUMMARY";
	private static final String CHANGE_BANK_ACCOUNT = "CHANGE_BANK_ACCOUNT";
	private static final String SAVE_BANK_ACCOUNT_ACTION = "SAVE_BANK_ACCOUNT_ACTION";
	private static final String CANCEL_BANK_CHANGE = "CANCEL_BANK_CHANGE";
	private static final String CHANGE_BANK_ACCOUNT_URI = "changeBankAccount";
	public static final String ADD_BANK_ACCOUNT_URI = "addBankAccount";
	public static final String BANK_ACCOUNT_SUMMARY_URI = "bankAccountSummary";
	private static final String SORTCODE_REGEX = "^\\d{2}(-?)\\d{2}\\1\\d{2}$";

	public static final String CHOOSE_BANK_ACCOUNT = "CHOOSE_BANK_ACCOUNT";
	private static final String STUDENT_DECLARATION = "STUDENT_DECLARATION";
	private static final String ADD_BANK_ACCOUNT_FORM_VO = "addBankAccountFormVO";
	private static final String CHOOSE_BANK_ACCOUNT_FORM_VO = "chooseBankAccountFormVO";
	private static final String CHANGE_BANK_ACCOUNT_FORM_VO = "changeBankAccountFormVO";
	public static final String CHOOSE_BANK_ACCOUNT_URI = "chooseBankAccount";

	private final Logger logger = LogManager.getLogger(this.getClass());

	public static final String EMAIL_SUBJECT = "Your Disabled Student’s Allowance has been submitted";

	public static final String STUDENT_DECLARATIONS_URI = "studentDeclaration";

	public static final String STUDENT_DECLARATIN_DETAILS_URI = "studentDeclarationDetails";
	public static final String CONTINUE_NEXT_URI = "continueNext";
	public static final String STUDENT_DECLARATION_VIEW = "student/studentDeclaration";
	public static final String CHOOSE_BANK_ACCOUNT_VIEW = "student/chooseBankAccount";
	public static final String ADD_BANK_ACCOUNT_VIEW = "student/addBankAccount";
	public static final String CHANGE_BANK_ACCOUNT_VIEW = "student/changeBankAccount";
	public static final String REMOVE_BANK_ACCOUNT_VIEW = "student/removeBankAccount";
	public static final String BANK_ACCOUNT_SUMMARY_VIEW = "student/bankAccountSummary";

	public static final String DECLARATION_FORM_VO = "declarationFormVO";

	private FindStudentService findStudentService;
	private BankAccountService bankAccountService;
	private ApplicationService applicationService;

	public BankDetailsController(FindStudentService findStudentService, BankAccountService bankAccountService,
			ApplicationService applicationService) {
		this.findStudentService = findStudentService;
		this.bankAccountService = bankAccountService;
		this.applicationService = applicationService;
	}

	@PostMapping(ADD_BANK_ACCOUNT_URI)
	public String addBankAccount(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = ADD_BANK_ACCOUNT_FORM_VO) AddBankAccountFormVO addBankAccountFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("in add bank account");

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, addBankAccountFormVO.getDsaApplicationNumber(),
				addBankAccountFormVO.getStudentReferenceNumber());
		String view = ERROR_PAGE;
		if (hasMandatoryValues) {
			model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, addBankAccountFormVO.getBackAction());
			switch (action.toUpperCase()) {
			case CHOOSE_BANK_ACCOUNT:
				view = AllowancesHelper.showChooseBankAccountPage(request);
				break;
			case SAVE_AND_CONTINUE_ACTION:
				model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, CHOOSE_BANK_ACCOUNT);
				view = initialiseAddBankAccountPage(model);
				break;
			case SAVE_BANK_ACCOUNT_ACTION:
				validateSortCode(bindingResult, addBankAccountFormVO.getSortCode());
				validateNumber(bindingResult, addBankAccountFormVO.getAccountNumber(), ACCOUNT_NUMBER,
						BANK_ACCOUNT_ACCOUNT_NUMBER_INVALID);

				if (bindingResult.hasErrors()) {
					model.addAttribute(ADD_BANK_ACCOUNT_FORM_VO, addBankAccountFormVO);
					view = ADD_BANK_ACCOUNT_VIEW;
					break;
				}
				boolean sameBankDetilsEntered = hasSameDetailsInStEPS(addBankAccountFormVO);
				if (sameBankDetilsEntered) {
					bindingResult.rejectValue(DUPLICATE_BANK_DETAILS, "bankAccount.duplicate.details");
					model.addAttribute(ADD_BANK_ACCOUNT_FORM_VO, addBankAccountFormVO);
					view = ADD_BANK_ACCOUNT_VIEW;
					break;
				}
				if (!bindingResult.hasErrors()) {
					saveBankDetails(addBankAccountFormVO);
					view = AllowancesHelper.showBankAccountSummaryPage(request);
				}
				break;
			case STUDENT_DECLARATION:
				view = AllowancesHelper.showStudentDeclarationPage(request);
				break;
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}
		return view;

	}

	@PostMapping(CHOOSE_BANK_ACCOUNT_URI)
	public String chooseBankAccount(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = CHOOSE_BANK_ACCOUNT_FORM_VO) ChooseBankAccountFormVO changeBankAccountVO,
			BindingResult bindingResult) throws Exception {
		logger.info("in choose bank account");

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, changeBankAccountVO.getDsaApplicationNumber(),
				changeBankAccountVO.getStudentReferenceNumber());
		String view = ERROR_PAGE;
		if (hasMandatoryValues) {
			StudentResultVO studentResultVO = findStudentByReferenceNumber(
					changeBankAccountVO.getStudentReferenceNumber());
			changeBankAccountVO.setNameOnAccount(studentResultVO.getFirstName() + " " + studentResultVO.getLastName());
			changeBankAccountVO.setPaymentFor(PaymentFor.MAIN_FUNDING_AND_DSA_PAYMNET.getDescriptionForPDF());
			changeBankAccountVO.setSortCode(studentResultVO.getSortCode());
			changeBankAccountVO.setAccountNumber(studentResultVO.getAccountNumber());
			changeBankAccountVO.setSortCodeForUI(formatSortcode(studentResultVO.getSortCode()));
			changeBankAccountVO.setAccountNumberForUI(formatAccountNumber(studentResultVO.getAccountNumber()));
			model.addAttribute(CHOOSE_BANK_ACCOUNT_FORM_VO, changeBankAccountVO);
			switch (action.toUpperCase()) {
			case "ADVISOR_AWARD_ACCESS":
				view = AllowancesHelper.showAwardAccessPage(request);
				break;
			case STUDENT_DECLARATION:
			case "BACK_TO_STUDENT_DECLARATION":
				view = AllowancesHelper.showStudentDeclarationPage(request);
				break;
			case REMOVE_BANK_ACCOUNT:
			case CHOOSE_BANK_ACCOUNT:
			case "BACK_TO_CHOOSE_BANK_ACCOUNT":
			case I_AGREE_ACTION:
				if (!StringUtils.hasText(studentResultVO.getAccountNumber())
						|| !StringUtils.hasText(studentResultVO.getSortCode())) {
					AddBankAccountFormVO addBankAccountVO = new AddBankAccountFormVO();
					addBankAccountVO.setStudentReferenceNumber(changeBankAccountVO.getStudentReferenceNumber());
					addBankAccountVO.setDsaApplicationNumber(changeBankAccountVO.getDsaApplicationNumber());
					model.addAttribute(ADD_BANK_ACCOUNT_FORM_VO, addBankAccountVO);
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, STUDENT_DECLARATION);
					view = ADD_BANK_ACCOUNT_VIEW;
				} else {
					model.addAttribute(DSAConstants.BACK_BUTTON_ACTION, STUDENT_DECLARATION);
					view = CHOOSE_BANK_ACCOUNT_VIEW;
				}
				break;
			case SAVE_AND_CONTINUE_ACTION:
				String removeItem = changeBankAccountVO.getUseExistingDetails();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						saveBankDetails(changeBankAccountVO);
						view = AllowancesHelper.showBankAccountSummaryPage(request);

					} else {
						view = AllowancesHelper.showAddBankAccountPage(request);
					}
				} else {
					bindingResult.rejectValue("useExistingDetails", "bankAccount.selection.required");
					view = CHOOSE_BANK_ACCOUNT_VIEW;
				}
				break;
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}
		return view;

	}

	@PostMapping(BANK_ACCOUNT_SUMMARY_URI)
	public String viewBankAccount(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		boolean hasMandatoryValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());
		logger.info("bank account summary action: {}", action);
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case "BANK_ACCOUNT_DETAILS_COMPLETED":
				ServiceUtil.updateSectionStatus(applicationService, keyDataVO.getDsaApplicationNumber(),
						Section.STUDENT_DECLARATION, SectionStatus.COMPLETED);
				ServiceUtil.updateOverallApplicationStatus(applicationService, keyDataVO.getDsaApplicationNumber());
				return AllowancesHelper.showDashboardPage(request);
			case "BACK_TO_CHOOSE_BANK_ACCOUNT":
				return AllowancesHelper.showChooseBankAccountPage(request);
			case CHANGE_BANK_ACCOUNT:
			case CANCEL_BANK_CHANGE:
			case SAVE_BANK_ACCOUNT_ACTION:
			case SAVE_AND_CONTINUE_ACTION:
			case REMOVE_BANK_ACCOUNT:
			case BANK_ACCOUNT_SUMMARY:
				BankAccountVO data = bankAccountService.getbankAccount(keyDataVO.getDsaApplicationNumber());
				model.addAttribute("bankAccountData", data);
				model.addAttribute("bankAccountAdded",
						data != null && action.equalsIgnoreCase(SAVE_AND_CONTINUE_ACTION));
				return BANK_ACCOUNT_SUMMARY_VIEW;
			default:
				addErrorMessage(model, action, request);
				break;
			}

		}
		return ERROR_PAGE;
	}

	@PostMapping(CHANGE_BANK_ACCOUNT_URI)
	public String changeBankAccount(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = CHANGE_BANK_ACCOUNT_FORM_VO) AddBankAccountFormVO addBankAccountFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("in change bank account");
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, addBankAccountFormVO.getDsaApplicationNumber(),
				addBankAccountFormVO.getStudentReferenceNumber());
		if (hasMandatoryValues) {
			switch (action.toUpperCase()) {
			case "INT_CHANGEN_BANK":
				BankAccountVO data = bankAccountService.getbankAccount(addBankAccountFormVO.getDsaApplicationNumber());
				addBankAccountFormVO.setNameOnAccount(data.getAccountName());
				addBankAccountFormVO.setSortCode(data.getSortCode());
				addBankAccountFormVO.setAccountNumber(data.getAccountNumber());
				model.addAttribute(CHANGE_BANK_ACCOUNT_FORM_VO, addBankAccountFormVO);
				view = CHANGE_BANK_ACCOUNT_VIEW;
				break;
			case CHANGE_BANK_ACCOUNT:
				validateSortCode(bindingResult, addBankAccountFormVO.getSortCode());
				validateNumber(bindingResult, addBankAccountFormVO.getAccountNumber(), ACCOUNT_NUMBER,
						BANK_ACCOUNT_ACCOUNT_NUMBER_INVALID);
				if (bindingResult.hasErrors()) {
					model.addAttribute(CHANGE_BANK_ACCOUNT_FORM_VO, addBankAccountFormVO);
					view = CHANGE_BANK_ACCOUNT_VIEW;
					break;
				}
				boolean sameBankDetilsEntered = hasSameDetailsInStEPS(addBankAccountFormVO);
				if (sameBankDetilsEntered) {
					bindingResult.rejectValue(DUPLICATE_BANK_DETAILS, "bankAccount.duplicate.details");
					model.addAttribute(CHANGE_BANK_ACCOUNT_FORM_VO, addBankAccountFormVO);
					view = CHANGE_BANK_ACCOUNT_VIEW;
					break;
				}
				if (!bindingResult.hasErrors()) {
					saveBankDetails(addBankAccountFormVO);
					view = AllowancesHelper.showBankAccountSummaryPage(request);
				}
				break;
			case CANCEL_BANK_CHANGE:
				view = AllowancesHelper.showBankAccountSummaryPage(request);
				break;
			default:
				addErrorMessage(model, action, request);
				break;
			}
		}
		return view;

	}

	@PostMapping("removeBankAccount")
	public String removeBankAccount(Model model, @RequestParam(value = ACTION, required = true) String action,
			HttpServletRequest request,
			@Valid @ModelAttribute(name = REMOVE_ITEM_FORM_VO) RemoveItemFormVO removeItemFormVO,
			BindingResult bindingResult) throws Exception {
		logger.info("in remove bank account");

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String view = ERROR_PAGE;
		boolean hasMandatoryValues = hasMandatoryValues(model, removeItemFormVO.getDsaApplicationNumber(),
				removeItemFormVO.getStudentReferenceNumber());
		if (hasMandatoryValues) {
			model.addAttribute(REMOVE_ITEM_FORM_VO, removeItemFormVO);
			switch (action.toUpperCase()) {
			case INIT_REMOVE_BANK_ACCOUNT:
				view = REMOVE_BANK_ACCOUNT_VIEW;
				break;
			case CANCEL_BANK_CHANGE:
			case BANK_ACCOUNT_SUMMARY:
				view = AllowancesHelper.showBankAccountSummaryPage(request);
				break;
			case REMOVE_BANK_ACCOUNT:
				String removeItem = removeItemFormVO.getRemoveItem();
				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						bankAccountService
								.deleteByDSAApplicationNumber(Long.valueOf(removeItemFormVO.getDsaApplicationNumber()));
						view = AllowancesHelper.showChooseBankAccountPage(request);
						break;
					}
					view = AllowancesHelper.showBankAccountSummaryPage(request);
				} else {
					bindingResult.rejectValue(REMOVE_ITEM, ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(NMPHController.REMOVE_ITEM_FORM_VO, removeItemFormVO);
					view = REMOVE_BANK_ACCOUNT_VIEW;
				}
				break;

			default:
				addErrorMessage(model, action, request);
				break;
			}

		}

		return view;
	}

	private void validateSortCode(BindingResult bindingResult, String sortCode) {
		if (StringUtils.hasLength(sortCode)) {
			boolean isValidPattern = ValidationHelper.matches(Pattern.compile(SORTCODE_REGEX), sortCode);
			boolean isSixDigits = ValidationHelper.matches(Pattern.compile(ValidationHelper.SIX_DIGITS), sortCode);
			boolean isValidSortCode = (isValidPattern || isSixDigits)
					&& (!Arrays.asList(ValidationHelper._00_00_00, ValidationHelper._000000).contains(sortCode));

			if (!isValidSortCode) {
				bindingResult.rejectValue(SORT_CODE, BANK_ACCOUNT_SORT_CODE_INVALID);
			}
		}
	}

	private boolean hasSameDetailsInStEPS(@Valid AddBankAccountFormVO addBankAccountFormVO)
			throws IllegalAccessException {
		StudentResultVO studentResultVO = findStudentByReferenceNumber(
				addBankAccountFormVO.getStudentReferenceNumber());
		boolean sameName = addBankAccountFormVO.getNameOnAccount()
				.equalsIgnoreCase(studentResultVO.getFirstName() + " " + studentResultVO.getLastName());
		boolean sameAccountNumber = addBankAccountFormVO.getAccountNumber()
				.equalsIgnoreCase(studentResultVO.getAccountNumber());
		boolean sameSortCode = addBankAccountFormVO.getSortCode().equalsIgnoreCase(studentResultVO.getSortCode());

		return sameName && sameAccountNumber && sameSortCode;

	}

	private StudentResultVO findStudentByReferenceNumber(long studentReferenceNumber) throws IllegalAccessException {
		StudentResultVO

		studentResultVO = findStudentService.findByStudReferenceNumber(studentReferenceNumber);

		return studentResultVO;
	}

	private String initialiseAddBankAccountPage(Model model) throws Exception {
		model.addAttribute(ADD_BANK_ACCOUNT_FORM_VO, new AddBankAccountFormVO());
		String view = ADD_BANK_ACCOUNT_VIEW;
		return view;
	}

	private void saveBankDetails(AddBankAccountFormVO bankAccountInput) {
		BankAccountVO bankAccountVO = new BankAccountVO();
		bankAccountVO.setDsaApplicationNumber(bankAccountInput.getDsaApplicationNumber());
		bankAccountVO.setStudentReferenceNumber(bankAccountInput.getStudentReferenceNumber());
		bankAccountVO.setAccountName(AllowancesHelper.toCapitaliseWord(bankAccountInput.getNameOnAccount()));
		bankAccountVO.setSortCode(bankAccountInput.getSortCode());
		bankAccountVO.setAccountNumber(bankAccountInput.getAccountNumber());
		bankAccountVO.setPaymentFor(PaymentFor.DSA_PAYMENT);
		bankAccountService.savebankAcount(bankAccountVO);
	}

	private void saveBankDetails(ChooseBankAccountFormVO bankAccountInput) {
		BankAccountVO bankAccountVO = new BankAccountVO();
		bankAccountVO.setDsaApplicationNumber(bankAccountInput.getDsaApplicationNumber());
		bankAccountVO.setStudentReferenceNumber(bankAccountInput.getStudentReferenceNumber());
		bankAccountVO.setAccountName(AllowancesHelper.toCapitaliseWord(bankAccountInput.getNameOnAccount()));
		bankAccountVO.setSortCode(bankAccountInput.getSortCode());
		bankAccountVO.setAccountNumber(bankAccountInput.getAccountNumber());
		bankAccountVO.setPaymentFor(PaymentFor.MAIN_FUNDING_AND_DSA_PAYMNET);
		bankAccountService.savebankAcount(bankAccountVO);

	}

}
