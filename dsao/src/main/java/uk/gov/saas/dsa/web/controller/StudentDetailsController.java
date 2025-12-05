package uk.gov.saas.dsa.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.service.StudentDetailsService;

import jakarta.validation.Valid;

import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

/**
 * Student details controller
 */
@Controller
public class StudentDetailsController {
	private static final String STUDENT_DETAILS = "/studentDetails";
	private static final String STUDENT_DETAILS_HTML = "advisor/studentDetails";
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final StudentDetailsService studentDetailsService;

	public StudentDetailsController(StudentDetailsService studentDetailsService) {
		this.studentDetailsService = studentDetailsService;
	}

	/**
	 * @param model     Model for the template
	 * @param keyDataVO Student Details with student reference number
	 * @return advisor/studentDetails
	 * @throws Exception For null student
	 */
	@PostMapping(STUDENT_DETAILS)
	public String studentDetails(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO)
			throws Exception {
		logger.info("initAboutDetails call");
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		StudentResultVO studentResultVO = studentDetailsService.findStudentDetailsFromDB(studentReferenceNumber);
		model.addAttribute("studentDetails", studentResultVO);
		model.addAttribute("dsaApplicationNumber", keyDataVO.getDsaApplicationNumber());
		return STUDENT_DETAILS_HTML;
	}
}
