package uk.gov.saas.dsa.web.controller.declaration;

import static uk.gov.saas.dsa.model.OverallApplicationStatus.SUBMITTED;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.hasMandatoryValues;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ACTION;
import static uk.gov.saas.dsa.web.helper.DSAConstants.APPLICATION_KEY_DATA_FORM_VO;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_MESSAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.ERROR_PAGE;
import static uk.gov.saas.dsa.web.helper.DSAConstants.LOGIN;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_FULL_NAME;
import static uk.gov.saas.dsa.web.helper.SecurityContextHelper.securityContext;

import java.util.HashMap;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.DSAApplicationCompleteService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.notification.EmailSenderService;
import uk.gov.saas.dsa.service.pdf.PDFGenerationService;
import uk.gov.saas.dsa.vo.ApplicationKeyDataFormVO;
import uk.gov.saas.dsa.vo.StudentResultVO;
import uk.gov.saas.dsa.web.controller.LoggedinUserUtil;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;
import uk.gov.saas.dsa.web.helper.DSAConstants;
import uk.gov.saas.dsa.web.helper.FindStudentHelper;

@Controller
public class ApplicationSubmitController {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private ApplicationService applicationService;
	private PDFGenerationService pdfGenerationService;
	private final FindStudentService findStudentService;
	private EmailSenderService emailNotificataionService;
	private DSAApplicationCompleteService dsaApplicationCompleteService;
	public static final String STUD_WHAT_HAPPENS_VIEW = "student/whathappensNextForStudent";

	public ApplicationSubmitController(EmailSenderService emailNotificataionService,
									   ApplicationService applicationService, PDFGenerationService pdfGenerationService,
									   FindStudentService findStudentService, DSAApplicationCompleteService dsaApplicationCompleteService) {

		this.emailNotificataionService = emailNotificataionService;
		this.applicationService = applicationService;
		this.pdfGenerationService = pdfGenerationService;
		this.findStudentService = findStudentService;
		this.dsaApplicationCompleteService = dsaApplicationCompleteService;

	}

	@PostMapping("submitApplication")
	public String submitApplication(Model model, @RequestParam(value = ACTION, required = true) String action,
									@Valid @ModelAttribute(name = APPLICATION_KEY_DATA_FORM_VO) ApplicationKeyDataFormVO keyDataVO)
			throws Exception {

		if (securityContext() == null) {
			return LOGIN;
		}

		LoggedinUserUtil.setLoggedinUserInToModel(model);

		logger.info("submitApplication call {}", keyDataVO);
		String view = ERROR_PAGE;

		boolean hasMandatoryValues = hasMandatoryValues(model, keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber());

		if (hasMandatoryValues) {

			ApplicationResponse applicationResponse = applicationService
					.findApplication(keyDataVO.getDsaApplicationNumber(), keyDataVO.getStudentReferenceNumber());
			FindStudentHelper.setStudentDetails(findStudentService, applicationResponse);
			boolean pdfCompleted = pdfGenerationService.generatePDF(applicationResponse);
			if (pdfCompleted) {
				applicationService.updateOverallApplciationStatus(keyDataVO.getDsaApplicationNumber(),
						keyDataVO.getStudentReferenceNumber(), SUBMITTED);
				saveCompleteWebApplciation(keyDataVO, applicationResponse);
				sendSubmitEmailNotification(model, applicationResponse);
				view = STUD_WHAT_HAPPENS_VIEW;
			}

		}

		return view;
	}

	private void saveCompleteWebApplciation(ApplicationKeyDataFormVO keyDataVO,
											ApplicationResponse applicationResponse) {
		dsaApplicationCompleteService.saveCompleteWeAppData(keyDataVO.getDsaApplicationNumber(),
				keyDataVO.getStudentReferenceNumber(), applicationResponse.getSessionCode());

	}

	private boolean sendSubmitEmailNotification(Model model, ApplicationResponse applicationResponse) {
		boolean isSuccess = true;

		try {

			HashMap<String, Object> modelMap = new HashMap<String, Object>();
			StudentResultVO studentResultVO = AllowancesHelper.getStudentResultWithSuid(findStudentService,
					applicationResponse.getStudentReferenceNumber(), applicationResponse.getSessionCode());
			modelMap.put(STUDENT_FULL_NAME, studentResultVO.getFirstName() + " " + studentResultVO.getLastName());
 
			emailNotificataionService.sendEmailNotification(studentResultVO, "We have received your DSA application",
					DSAConstants.STUDENT_SUBMIT_NOTIFICATION_TEMPLATE_HTML, modelMap);

		} catch (IllegalAccessException e) {
			model.addAttribute(ERROR_MESSAGE, e.getMessage());
			isSuccess = false;
		}
		return isSuccess;
	}
}
