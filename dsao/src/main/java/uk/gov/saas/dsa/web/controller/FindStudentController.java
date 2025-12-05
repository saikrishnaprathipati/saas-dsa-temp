package uk.gov.saas.dsa.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.vo.FindStudentByStudrefFormVO;
import uk.gov.saas.dsa.vo.FindStudentFormVO;
import uk.gov.saas.dsa.vo.FindStudentOptionsFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.FindStudentHelper;
import uk.gov.saas.dsa.web.helper.ValidationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import java.sql.Date;
import java.util.List;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.validateNumber;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

@Controller
public class FindStudentController {

	private static final String FIND_STUDENT_BY_STUDREF_FORM_VO = "findStudentByStudrefFormVO";

	public static final String FIND_STUDENT_OPTION_FORM_VO = "findStudentOptionFormVO";

	private final Logger logger = LogManager.getLogger(this.getClass());

	private static final String FIND_STUDENT_URI = "/findStudent";
	public static final String FIND_STUDENT_OPTION = "findStudents";
	public static final String FIND_STUDENT_OPTION_URI = "/" + FIND_STUDENT_OPTION;
	private static final String FIND_STUDENT_BY_STUD_REF_URI = "/findStudentByStudRef";

	private static final String ADVISOR_STUDENT_RESULTS_VIEW = "advisor/studentResults";
	private static final String ADVISOR_FIND_STUDENT_VIEW = "advisor/findStudent";
	private static final String ADVISOR_FIND_STUDENT_BY_STUD_REF_VIEW = "advisor/findStudentbyStudRef";
	private static final String ADVISOR_FIND_STUDENT_OPTIONS_VIEW = "advisor/findStudentOptions";

	private final FindStudentService findStudentService;
	private final MessageSource messageSource;

	public FindStudentController(FindStudentService findStudentService, MessageSource messageSource) {
		this.findStudentService = findStudentService;
		this.messageSource = messageSource;
	}

	@GetMapping(FIND_STUDENT_OPTION_URI)
	public String initFindStudentOptions(Model model) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		FindStudentOptionsFormVO findStudentFormVO = new FindStudentOptionsFormVO();
		findStudentFormVO.setFindOption("STUD_REF");
		model.addAttribute(FIND_STUDENT_OPTION_FORM_VO, findStudentFormVO);
		return ADVISOR_FIND_STUDENT_OPTIONS_VIEW;
	}

	@PostMapping(FIND_STUDENT_OPTION_URI)
	public String findStudentByOptions(Model model,
			@Valid @ModelAttribute(name = FIND_STUDENT_OPTION_FORM_VO) FindStudentOptionsFormVO findStudentOptionFormVO,
			BindingResult bindingResult, HttpServletRequest request) {
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN;
		}
		String view = ERROR_PAGE;
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		if (bindingResult.hasErrors()) {
			model.addAttribute(FIND_STUDENT_OPTION_FORM_VO, findStudentOptionFormVO);
			view = ADVISOR_FIND_STUDENT_OPTIONS_VIEW;
		} else {
			if (findStudentOptionFormVO.getFindOption().equalsIgnoreCase("STUD_REF")) {
				view = initFindStudentByStudRef(model);
			} else {
				view = initFindStudent(model);
			}
		}

		return view;
	}

	@GetMapping(FIND_STUDENT_BY_STUD_REF_URI)
	public String initFindStudentByStudRef(Model model) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);
		FindStudentByStudrefFormVO findStudentByStudrefFormVO = new FindStudentByStudrefFormVO();
		model.addAttribute(FIND_STUDENT_BY_STUDREF_FORM_VO, findStudentByStudrefFormVO);
		return ADVISOR_FIND_STUDENT_BY_STUD_REF_VIEW;
	}

	@PostMapping(FIND_STUDENT_BY_STUD_REF_URI)
	public String findStudentByStudRef(Model model,
			@Valid @ModelAttribute FindStudentByStudrefFormVO findStudentByStudrefFormVO, BindingResult bindingResult,
			HttpServletRequest request, RedirectAttributes redirectAttributes, HttpSession httpsession) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		String studentReferenceNumber = findStudentByStudrefFormVO.getStudentReferenceNumber();
		if (bindingResult.hasErrors()) {
			model.addAttribute(FIND_STUDENT_BY_STUDREF_FORM_VO, findStudentByStudrefFormVO);
			return ADVISOR_FIND_STUDENT_BY_STUD_REF_VIEW;
		} else {
			ValidationHelper.validateNumber(bindingResult, studentReferenceNumber, "studentReferenceNumber",
					"findStudent.studentReferenceNumber.invalid");
			if (bindingResult.hasErrors()) {
				model.addAttribute(FIND_STUDENT_BY_STUDREF_FORM_VO, findStudentByStudrefFormVO);
				return ADVISOR_FIND_STUDENT_BY_STUD_REF_VIEW;
			}
			Long studRefNo = Long.valueOf(studentReferenceNumber);
			List<StudentResultVO> results = findStudentService.findStudentWithStudRefNo(studRefNo);
			return showStudentResults(model, results);
		}
	}
 

	@GetMapping(FIND_STUDENT_URI)
	public String initFindStudent(Model model) {
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("initFindStudent controller call {}", securityContext());
		FindStudentFormVO findStudentFormVO = new FindStudentFormVO();
		model.addAttribute("findStudentFormVO", findStudentFormVO);
		return ADVISOR_FIND_STUDENT_VIEW;
	}

	@PostMapping(FIND_STUDENT_URI)
	public String findStudents(Model model, @Valid @ModelAttribute FindStudentFormVO findStudentFormVO,
			BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirectAttributes,
			HttpSession httpsession) {
		SecurityContext securityContext = securityContext();
		if (securityContext == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("Find student Post call form request: {}", findStudentFormVO);
		Date dateOfBirth = FindStudentHelper.populateDOB(findStudentFormVO, bindingResult);
		findStudentFormVO.setDateOfBirth(dateOfBirth);
		FindStudentHelper.validateDateOfBirth(findStudentFormVO, bindingResult, messageSource);

		if (bindingResult.hasErrors()) {
			model.addAttribute("findStudentFormVO", findStudentFormVO);
			return ADVISOR_FIND_STUDENT_VIEW;
		}

		List<StudentResultVO> results = findStudentService.findByForenamesAndSurnameAndDobStud(
				findStudentFormVO.getFirstName(), findStudentFormVO.getLastName(), findStudentFormVO.getDateOfBirth());

		return showStudentResults(model, results);
	}

	private String showStudentResults(Model model, List<StudentResultVO> results) {
		logger.info("Find student Post call results: {}", results);
		model.addAttribute("studentResults", results);
		model.addAttribute("FindStudentHelper", new FindStudentHelper());
		return ADVISOR_STUDENT_RESULTS_VIEW;
	}
}
