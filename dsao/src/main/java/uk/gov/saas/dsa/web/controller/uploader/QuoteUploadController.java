package uk.gov.saas.dsa.web.controller.uploader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.saas.dsa.domain.DSAQuotePDF;
import uk.gov.saas.dsa.domain.refdata.YesNoType;
import uk.gov.saas.dsa.domain.validation.QuoteDetailsFormValidator;
import uk.gov.saas.dsa.domain.validation.QuoteFileValidator;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.ConfigDataService;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.service.QuoteUploadService;
import uk.gov.saas.dsa.service.allowances.EquipmentPaymentService;
import uk.gov.saas.dsa.service.allowances.EquipmentService;
import uk.gov.saas.dsa.vo.ChooseQuoteOptionFormVO;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;
import uk.gov.saas.dsa.vo.quote.RemoveQuoteFormVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.SecurityContextHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.*;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

@Controller
public class QuoteUploadController {

	private final Logger logger = LogManager.getLogger(QuoteUploadController.class);

	private static final String REMOVE_QUOTE_FORM_VO = "removeQuoteFormVO";
	private static final String QUOTE_SUMMARY = "/quoteSummary";
	private static final String CHANGE_QUOTE_FORM_VO = "changeQuoteFormVO";
	private static final String REDIRECT = "redirect:/";
	private static final String QUOTES = "quotes";
	private static final String BACK_TO_CHOOSE_QUOTE = "BACK_TO_CHOOSE_QUOTE";
	private static final String REDIRECT_CHOOSE_QUOTE_OPTION = "redirect:/chooseQuoteOption";
	private static final String QUOTE_DETAILS_FORM_VO = "quoteDetailsFormVO";
	private static final String UPLOADER_UPLOAD_QUOTE = "advisor/quote/uploadQuote";
	private static final String UPLOADER_SHOW_QUOTE = "advisor/quote/showQuote";
	private static final String CHANGE_QUOTE = "/changeQuote";
	private static final String UPLOADER_CHANGE_QUOTE = "advisor/quote/changeQuote";
	private static final String REMOVE_QUOTE = "/removeQuote";
	private static final String UPLOADER_REMOVE_QUOTE = "advisor/quote/removeQuote";
	private static final String UPLOADER_ADD_QUOTE = "advisor/quote/addQuote";
	private static final String ADD_QUOTE = "/addQuote";
	private static final String UPLOAD_QUOTE = "/uploadQuote";
	private static final String SHOW_QUOTE_SUMMARY = "/showQuoteSummary";
	private static final String LOGIN_PAGE = "login/login";
	private static final String ADVISOR_EQUIPMENT_SUMMARY_PAGE = "advisor/equipment/equipmentSummary";
	private static final String ADVISOR_CHOOSE_QUOTE_OPTION_PAGE = "advisor/equipment/chooseQuoteOption";

	private final ConfigDataService configDataService;
	private final EquipmentService equipmentService;
	private final QuoteUploadService quoteUploadService;
	private final QuoteFileValidator quoteFileValidator;
	private final List<Long> addedQuoteIds = new ArrayList<>();
	private final EquipmentPaymentService equipmentPaymentService;
	private final CourseDetailsService courseDetailsService;
	private final ApplicationService applicationService;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	public QuoteUploadController(QuoteUploadService quoteUploadService, EquipmentService equipmentService,
								 ConfigDataService configDataService, QuoteFileValidator quoteFileValidator,
								 EquipmentPaymentService equipmentPaymentService, CourseDetailsService courseDetailsService,
								 ApplicationService applicationService) {
		this.quoteUploadService = quoteUploadService;
		this.equipmentService = equipmentService;
		this.configDataService = configDataService;
		this.quoteFileValidator = quoteFileValidator;
		this.equipmentPaymentService = equipmentPaymentService;
		this.courseDetailsService = courseDetailsService;
		this.applicationService = applicationService;
	}

	@GetMapping(ADD_QUOTE)
	public String displayQuoteDetails(Model model, @Valid @ModelAttribute QuoteDetailsFormVO quoteDetailsFormVO) {
		logger.info("display Quote details {}", quoteDetailsFormVO);

		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN_PAGE;
		} else {
			model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
			return UPLOADER_ADD_QUOTE;
		}
	}

	@PostMapping(ADD_QUOTE)
	public String addQuote(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
						   @Valid @ModelAttribute QuoteDetailsFormVO quoteDetailsFormVO, HttpSession httpsession,
						   BindingResult bindingResult, Errors errors) {
		logger.info("add Quote details {}", quoteDetailsFormVO);
		model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN_PAGE;
		} else {
			LoggedinUserUtil.setLoggedinUserInToModel(model);
			switch (action.toUpperCase()) {
				case CONFIRM_AND_CONTINUE_ACTION:
					return validateAddQuote(model, action, quoteDetailsFormVO, bindingResult, errors);
				case BACK_ACTION:
				case BACK_TO_CHOOSE_QUOTE:
					buildChooseQuoteOptionFormVO(model, quoteDetailsFormVO, httpsession);
					return REDIRECT_CHOOSE_QUOTE_OPTION;
				case ADD_ANOTHER_QUOTE:
					model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
					return UPLOADER_ADD_QUOTE;
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}
		return ERROR_PAGE;
	}

	@PostMapping(UPLOAD_QUOTE)
	public String validateAndSaveUploadQuote(Model model, HttpServletRequest request, RedirectAttributes attributes,
											 @RequestParam(value = ACTION) String action, @RequestParam("files") MultipartFile[] files,
											 QuoteDetailsFormVO quoteDetailsFormVO, BindingResult bindingResult, Errors errors) {
		logger.info("upload Quote details {}", quoteDetailsFormVO);

		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN_PAGE;
		} else {
			LoggedinUserUtil.setLoggedinUserInToModel(model);
			String user = SecurityContextHelper.getLoggedInUser();
			quoteDetailsFormVO.setAdvisorId(user);
			model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);

			switch (action.toUpperCase()) {
				case UPLOAD_ACTION:
				case UPLOAD_FROM_SHOW_QUOTE_ACTION:
				case CONFIRM_AND_CONTINUE_ACTION:
					return validateAndSaveQuoteFiles(model, attributes, files, quoteDetailsFormVO, bindingResult, errors,
							action);
				case BACK_ACTION:
					if (Objects.equals(quoteDetailsFormVO.getExistingAction(), "Change")) {
						model.addAttribute(CHANGE_QUOTE_FORM_VO, quoteDetailsFormVO);
						return UPLOADER_CHANGE_QUOTE;
					}
					return UPLOADER_ADD_QUOTE;
				case DASHBOARD_ACTION:
					return redirectToView(request, APPLICATION_DASHBOARD_PATH);
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}
		return ERROR_PAGE;
	}

	private String validateAndSaveQuoteFiles(Model model, RedirectAttributes attributes, MultipartFile[] files,
											 QuoteDetailsFormVO quoteDetailsFormVO, BindingResult bindingResult, Errors errors, String action) {
		if (files.length == 0) {
			attributes.addFlashAttribute("message", "Please select a file to upload.");
			return REDIRECT;
		}
		String quoteReference = quoteDetailsFormVO.getQuoteReference();
		if (null != quoteReference) {
			quoteDetailsFormVO.setQuoteReference(AllowancesHelper.sanetizeSpecialCharacters(quoteReference));
		}

		for (MultipartFile file : files) {
			quoteFileValidator.validateQuoteFiles(errors, file);

			if (bindingResult.hasErrors() && action.equalsIgnoreCase(UPLOAD_ACTION)) {
				model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
				return UPLOADER_UPLOAD_QUOTE;
			} else {
				if (!bindingResult.hasErrors()) {
					quoteUploadService.uploadQuoteForStudentApplication(files, file, quoteDetailsFormVO);
					DSAQuotePDF dsaQuotePDF = quoteUploadService.fetchQuoteByReferenceAndSupplier(quoteDetailsFormVO);

					addedQuoteIds.add(dsaQuotePDF.getQuoteId());
				}
				return showUploadedQuotes(model);
			}
		}
		return UPLOADER_UPLOAD_QUOTE;
	}

	private String showUploadedQuotes(Model model) {
		List<QuoteResultVO> quotes = quoteUploadService.fetchUploadedQuotesForStudentApplication(addedQuoteIds);
		model.addAttribute(SHOW_ADD_QUOTE_LINK, EQUIPMENT_ITEMS_LIMIT > quotes.size() ? "Y" : "N");
		model.addAttribute(QUOTES, quotes);
		return UPLOADER_SHOW_QUOTE;
	}

	private String showSummaryPage(Model model, QuoteDetailsFormVO quoteDetailsFormVO) {
		logger.info("Remove equipment allowances call request: {}", quoteDetailsFormVO);
		List<QuoteResultVO> quotes = quoteUploadService.fetchAllQuotesForStudentApplication(quoteDetailsFormVO.getDsaApplicationNumber());
		model.addAttribute(SHOW_ADD_QUOTE_LINK, QUOTE_ITEMS_LIMIT > quotes.size() ? "Y" : "N");
		model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
		model.addAttribute(QUOTES, quotes);
		model.addAttribute("institutionName", getInstitutionName(quoteDetailsFormVO.getStudentReferenceNumber(), quoteDetailsFormVO.getSessionCode()));

		setEquipmentSummaryData(model, equipmentService, configDataService, quoteUploadService,
				quoteDetailsFormVO.getDsaApplicationNumber(), 1, 5);
		setQuoteSummaryData(model, quoteUploadService, quoteDetailsFormVO, 1, 5, configDataService);
		AllowancesHelper.setEquipmentPaymentForData(model, equipmentPaymentService, configDataService,
				quoteDetailsFormVO.getDsaApplicationNumber(), quoteDetailsFormVO.getStudentReferenceNumber());
		AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, this.applicationService,
				quoteDetailsFormVO.getDsaApplicationNumber(), quoteDetailsFormVO.getStudentReferenceNumber());
		addedQuoteIds.clear();
		return ADVISOR_EQUIPMENT_SUMMARY_PAGE;
	}

	@PostMapping(path = {CHANGE_QUOTE})
	public String changeQuote(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
							  RedirectAttributes redirectAttributes,
							  @Valid @ModelAttribute(name = CHANGE_QUOTE_FORM_VO) QuoteDetailsFormVO changeQuoteFormVO,
							  BindingResult bindingResult, Errors errors) throws Exception {
		logger.info("changeQuoteFormVO Quote action: {}, request: {}", action, changeQuoteFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = changeQuoteFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = changeQuoteFormVO.getStudentReferenceNumber();
		hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		changeQuoteFormVO.setExistingAction(action);

		switch (action.toUpperCase()) {
			case CHANGE_ACTION:
				model.addAttribute(CHANGE_QUOTE_FORM_VO, changeQuoteFormVO);
				return validateChangeQuote(model, action, changeQuoteFormVO, bindingResult, errors);
			case CHANGE_ACTION_FROM_SUMMARY:
				model.addAttribute(CHANGE_QUOTE_FORM_VO, changeQuoteFormVO);
				return UPLOADER_CHANGE_QUOTE;
			case CANCEL_ACTION:
				return showEquipmentSummary(request);
			case DASHBOARD_ACTION:
				return redirectToView(request, APPLICATION_DASHBOARD_PATH);
			default:
				addErrorMessage(model, action, request);
				break;
		}
		return ERROR_PAGE;
	}

	private String validateChangeQuote(Model model, String action, QuoteDetailsFormVO changeQuoteFormVO,
									   BindingResult bindingResult, Errors errors) {
		logger.info("validate change Quote request: {}", changeQuoteFormVO);
		QuoteDetailsFormValidator quoteDetailsFormValidator = new QuoteDetailsFormValidator(quoteUploadService);
		quoteDetailsFormValidator.validate(action, changeQuoteFormVO, errors, bindingResult);
		changeQuoteFormVO.setExistingAction(action);
		if (bindingResult.hasErrors()) {
			model.addAttribute(QUOTE_DETAILS_FORM_VO, changeQuoteFormVO);
			return UPLOADER_CHANGE_QUOTE;
		} else {
			model.addAttribute(QUOTE_DETAILS_FORM_VO, changeQuoteFormVO);
			return UPLOADER_UPLOAD_QUOTE;
		}
	}

	private String validateAddQuote(Model model, String action, QuoteDetailsFormVO quoteDetailsFormVO,
									BindingResult bindingResult, Errors errors) {
		logger.info("validate Add Quote request: {}", quoteDetailsFormVO);
		QuoteDetailsFormValidator quoteDetailsFormValidator = new QuoteDetailsFormValidator(quoteUploadService);
		quoteDetailsFormValidator.validate(action, quoteDetailsFormVO, errors, bindingResult);
		if (bindingResult.hasErrors()) {
			model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
			return UPLOADER_ADD_QUOTE;
		} else {
			model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
			return UPLOADER_UPLOAD_QUOTE;
		}
	}

	@PostMapping(path = {QUOTE_SUMMARY})
	public String quoteSummary(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
							   RedirectAttributes redirectAttributes, @Valid @ModelAttribute QuoteDetailsFormVO quoteDetailsFormVO,
							   @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size,
							   BindingResult bindingResult) throws Exception {
		logger.info(" Quote summary action: {}, request: {}", action, quoteDetailsFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long dsaApplicationNumber = quoteDetailsFormVO.getDsaApplicationNumber();
		long studentReferenceNumber = quoteDetailsFormVO.getStudentReferenceNumber();
		hasMandatoryValues(model, dsaApplicationNumber, studentReferenceNumber);
		List<QuoteResultVO> quotes = quoteUploadService.fetchAllQuotesForStudentApplication(quoteDetailsFormVO.getDsaApplicationNumber());
		model.addAttribute(SHOW_ADD_QUOTE_LINK, QUOTE_ITEMS_LIMIT > quotes.size() ? "Y" : "N");
		model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
		model.addAttribute(QUOTES, quotes);

		int currentPage = page.orElse(1);
		int pageSize = size.orElse(PAGINATION_SIZE);
		setEquipmentSummaryData(model, equipmentService, configDataService, quoteUploadService,
				dsaApplicationNumber, currentPage, pageSize);
		setQuoteSummaryData(model, quoteUploadService, quoteDetailsFormVO, currentPage, pageSize, configDataService);
		AllowancesHelper.setEquipmentPaymentForData(model, equipmentPaymentService, configDataService, dsaApplicationNumber, studentReferenceNumber);
		switch (action.toUpperCase()) {
			case BACK_ACTION:
			case SAVE_AND_CONTINUE_ACTION:
			case CANCEL_ACTION:
				model.addAttribute("institutionName", getInstitutionName(quoteDetailsFormVO.getStudentReferenceNumber(), quoteDetailsFormVO.getSessionCode()));
				AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, this.applicationService,
						quoteDetailsFormVO.getDsaApplicationNumber(), quoteDetailsFormVO.getStudentReferenceNumber());
				return ADVISOR_EQUIPMENT_SUMMARY_PAGE;
			case DASHBOARD_ACTION:
				return redirectToView(request, APPLICATION_DASHBOARD_PATH);
			default:
				addErrorMessage(model, action, request);
				break;
		}

		return ERROR_PAGE;
	}

	@PostMapping(REMOVE_QUOTE)
	public String removeQuote(Model model, HttpServletRequest request, @RequestParam(value = ACTION) String action,
							  RedirectAttributes redirectAttributes, @ModelAttribute RemoveQuoteFormVO removeQuoteFormVO,
							  BindingResult bindingResult) throws IllegalAccessException {
		logger.info("Remove equipment allowances call action: {}, request: {}", action, removeQuoteFormVO);

		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		hasMandatoryValues(model, removeQuoteFormVO.getDsaApplicationNumber(),
				removeQuoteFormVO.getStudentReferenceNumber());
		model.addAttribute(REMOVE_QUOTE_FORM_VO, removeQuoteFormVO);
		QuoteDetailsFormVO quoteDetailsFormVO = getQuoteDetailsFromRemoveForm(removeQuoteFormVO);

		switch (action.toUpperCase()) {
			case REMOVE_ACTION:
				return UPLOADER_REMOVE_QUOTE;
			case REMOVE_FROM_SHOW_QUOTE_ACTION:
				addedQuoteIds.remove(removeQuoteFormVO.getQuoteId());
				quoteUploadService.deleteQuoteByQuoteId(removeQuoteFormVO.getQuoteId());
				List<QuoteResultVO> quotes = quoteUploadService
						.fetchUploadedQuotesForStudentApplication(addedQuoteIds);
				model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
				model.addAttribute(QUOTES, quotes);
				return UPLOADER_SHOW_QUOTE;
			case BACK_ACTION:
				return showEquipmentSummary(request);
			case DASHBOARD_ACTION:
				return redirectToView(request, APPLICATION_DASHBOARD_PATH);
			case SAVE_AND_CONTINUE_ACTION:
				String removeItem = removeQuoteFormVO.getRemoveQuote();

				if (AllowancesHelper.optionHasCorrectValue(removeItem)) {
					if (removeItem.equals(YesNoType.YES.name())) {
						quoteUploadService.deleteQuoteByQuoteId(removeQuoteFormVO.getQuoteId());
						return showSummaryPage(model, quoteDetailsFormVO);
					} else if (removeItem.equals(YesNoType.NO.name())) {
						return showSummaryPage(model, quoteDetailsFormVO);
					} else {
						bindingResult.rejectValue(REMOVE_QUOTE_FIELD, ALLOWANCES_REMOVE_OPTION_REQUIRED);
						model.addAttribute(REMOVE_QUOTE_FORM_VO, removeQuoteFormVO);
						return UPLOADER_REMOVE_QUOTE;
					}
				} else {
					bindingResult.rejectValue(REMOVE_QUOTE_FIELD, ALLOWANCES_REMOVE_OPTION_REQUIRED);
					model.addAttribute(REMOVE_QUOTE_FORM_VO, removeQuoteFormVO);
					return UPLOADER_REMOVE_QUOTE;
				}
			default:
				addErrorMessage(model, action, request);
				break;
		}
		return ERROR_PAGE;
	}

	private static QuoteDetailsFormVO getQuoteDetailsFromRemoveForm(RemoveQuoteFormVO removeQuoteFormVO) {
		QuoteDetailsFormVO quoteDetailsFormVO = new QuoteDetailsFormVO();
		quoteDetailsFormVO.setQuoteReference(AllowancesHelper.sanetizeSpecialCharacters(removeQuoteFormVO.getQuoteReference()));
		quoteDetailsFormVO.setQuoteId(removeQuoteFormVO.getQuoteId());
		quoteDetailsFormVO.setStudentReferenceNumber(removeQuoteFormVO.getStudentReferenceNumber());
		quoteDetailsFormVO.setDsaApplicationNumber(removeQuoteFormVO.getDsaApplicationNumber());
		quoteDetailsFormVO.setFirstName(removeQuoteFormVO.getFirstName());
		quoteDetailsFormVO.setSessionCode(removeQuoteFormVO.getSessionCode());
		return quoteDetailsFormVO;
	}

	@PostMapping(SHOW_QUOTE_SUMMARY)
	public String showQuoteSummary(Model model, HttpServletRequest request, RedirectAttributes attributes,
								   @RequestParam(value = ACTION) String action, QuoteDetailsFormVO quoteDetailsFormVO,
								   BindingResult bindingResult, Errors errors) {
		logger.info("Show  Quote summary {}", quoteDetailsFormVO);
		SecurityContext securityContext = securityContext();

		if (securityContext == null) {
			return LOGIN_PAGE;
		} else {
			LoggedinUserUtil.setLoggedinUserInToModel(model);
			String user = SecurityContextHelper.getLoggedInUser();
			quoteDetailsFormVO.setAdvisorId(user);

			switch (action.toUpperCase()) {
				case CONFIRM_AND_CONTINUE_ACTION:
					return showSummaryPage(model, quoteDetailsFormVO);
				case BACK_ACTION:
					if (addedQuoteIds.isEmpty()) {
						return showSummaryPage(model, quoteDetailsFormVO);
					}
					model.addAttribute(QUOTE_DETAILS_FORM_VO, quoteDetailsFormVO);
					return UPLOADER_ADD_QUOTE;
				case DASHBOARD_ACTION:
					return redirectToView(request, APPLICATION_DASHBOARD_PATH);
				default:
					addErrorMessage(model, action, request);
					break;
			}
		}
		return ERROR_PAGE;
	}

	@GetMapping(value = "/document/download/{name}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> downloadFile(@PathVariable(value = "name") String fileName) {
		Resource dir = resourceLoader.getResource("classpath:file/download/" + fileName);

		try {
			if (dir.exists()) {
				Resource file = new UrlResource(dir.getFile().toURI());
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(file);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	private void buildChooseQuoteOptionFormVO(Model model, QuoteDetailsFormVO quoteDetailsFormVO,
											  HttpSession httpsession) {
		ChooseQuoteOptionFormVO chooseQuoteOptionFormVO = new ChooseQuoteOptionFormVO();
		chooseQuoteOptionFormVO.setStudentReferenceNumber(quoteDetailsFormVO.getStudentReferenceNumber());
		chooseQuoteOptionFormVO.setDsaApplicationNumber(quoteDetailsFormVO.getDsaApplicationNumber());
		model.addAttribute("chooseQuoteOptionFormVO", chooseQuoteOptionFormVO);
		httpsession.setAttribute("chooseQuoteOptionFormVO", chooseQuoteOptionFormVO);
		logger.info("redirect to {}", ADVISOR_CHOOSE_QUOTE_OPTION_PAGE);
	}

	// Get Institution Name
	private String getInstitutionName(long studentReferenceNumber, int sessionCode) {
		CourseDetailsVO courseDetails;
		try {
			courseDetails = courseDetailsService.findCourseDetailsFromDB(studentReferenceNumber, sessionCode);
			return " " + courseDetails.getInstitutionName() + " ";
		} catch (IllegalAccessException e) {
			logger.info(e.getMessage());
		}
		return " ";
	}
}
