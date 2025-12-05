package uk.gov.saas.dsa.web.controller;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

import jakarta.validation.Valid;

import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

/**
 * The course details controller
 */
@Controller
public class CourseDetailsController {
	private static final String COURSE_DETAILS = "courseDetails";
	private static final String ABOUT_COURSE_DETAILS = "/courseDetails";
	private static final String ADVISOR_COURSE_DETAILS = "advisor/courseDetails";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final ApplicationService applicationService;
	private final CourseDetailsService courseDetailsService;

	public CourseDetailsController(CourseDetailsService courseDetailsService, ApplicationService applicationService) {
		this.courseDetailsService = courseDetailsService;
		this.applicationService = applicationService;
	}

	/**
	 * To get the course details
	 *
	 * @param model     Model
	 * @param keyDataVO ApplicationKeyDataFormVO
	 * @return html page with course details data
	 */
	@PostMapping(ABOUT_COURSE_DETAILS)
	public String courseDetails(Model model, @Valid @ModelAttribute ApplicationKeyDataFormVO keyDataVO)
			throws IllegalAccessException {
		logger.info("aboutCourseDetails call {}", keyDataVO);
		if (securityContext() == null) {
			return LOGIN;
		}
		LoggedinUserUtil.setLoggedinUserInToModel(model);

		long studentReferenceNumber = keyDataVO.getStudentReferenceNumber();
		long dsaApplicationNumber = keyDataVO.getDsaApplicationNumber();
		ApplicationResponse applicationResponse = applicationService.findApplication(dsaApplicationNumber, studentReferenceNumber);
		CourseDetailsVO courseDetails = new CourseDetailsVO();
		try {
			// Search course by student reference number and session code
			courseDetails = courseDetailsService.findCourseDetailsFromDB(studentReferenceNumber, applicationResponse.getSessionCode());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
			return ERROR_PAGE;
		}
		courseDetails.setStudentReferenceNumber(studentReferenceNumber);
		courseDetails.setDsaApplicationNumber(dsaApplicationNumber);

		model.addAttribute(COURSE_DETAILS, courseDetails);
		AllowancesHelper.setAllowanceAndDeclarationCompletionStatusIntheModel(model, applicationService,
				dsaApplicationNumber, studentReferenceNumber);
		return ADVISOR_COURSE_DETAILS;
	}
}
