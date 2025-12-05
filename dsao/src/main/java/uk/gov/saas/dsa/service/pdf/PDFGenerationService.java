package uk.gov.saas.dsa.service.pdf;

import com.lowagie.text.DocumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
import uk.gov.saas.dsa.domain.DSAApplicationPDF;
import uk.gov.saas.dsa.domain.DSALargeEquipemntPaymentFor;
import uk.gov.saas.dsa.domain.DsaAdvisorAuthDetails;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;
import uk.gov.saas.dsa.model.ApplicationResponse;
import uk.gov.saas.dsa.model.Section;
import uk.gov.saas.dsa.model.SectionStatusResponse;
import uk.gov.saas.dsa.model.TravelExpType;
import uk.gov.saas.dsa.persistence.DSAApplicationPDFRepository;
import uk.gov.saas.dsa.persistence.DsaAdvisorAuthRepository;
import uk.gov.saas.dsa.service.CourseDetailsService;
import uk.gov.saas.dsa.service.DisabilitiesService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.QuoteUploadService;
import uk.gov.saas.dsa.service.allowances.EquipmentPaymentService;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.vo.CourseDetailsVO;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.saas.dsa.service.ServiceUtil.getSectionStatusData;
import static uk.gov.saas.dsa.web.helper.AllowancesHelper.*;
import static uk.gov.saas.dsa.web.helper.DSAConstants.SAAS_REFERNECE_NUMBER;
import static uk.gov.saas.dsa.web.helper.DSAConstants.STUDENT_FULL_NAME;

/**
 * DSA Application service
 */
@Service
public class PDFGenerationService {

	public static final String INSTITUTION = "INSTITUTION";
	private static final String PAYMENT_FOR_TEXT_HINT = "PAYMENT_FOR_TEXT_HINT";
	private static final String PAYMENT_FOR_TEXT = "PAYMENT_FOR_TEXT";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private static final String PDF_DSA_APPLICATION_PDF_TEMPLATE_HTML = "pdf/dsaApplicationPDFTemplate.html";

	private final DsaAdvisorAuthRepository dsaAdvisorAuthRepository;
	private final SpringTemplateEngine thymeleafTemplateEngine;

	private final DSAApplicationPDFRepository applicationPDFRepository;
	private final DisabilitiesService disabilitiesService;
	private final CourseDetailsService courseDetailsService;
	private final FindStudentService findStudentService;
	private final QuoteUploadService quoteUploadService;
	private final EquipmentPaymentService equipmentPaymentService;

	@Autowired
	public PDFGenerationService(SpringTemplateEngine thymeleafTemplateEngine,
			ResourceBundleMessageSource emailMessageSource, DSAApplicationPDFRepository applicationPDFRepository,
			DisabilitiesService disabilitiesService, DsaAdvisorAuthRepository dsaAdvisorAuthRepository,
			CourseDetailsService courseDetailsService, FindStudentService findStudentService,
			QuoteUploadService quoteUploadService, EquipmentPaymentService equipmentPaymentService) {
		this.thymeleafTemplateEngine = thymeleafTemplateEngine;
		this.applicationPDFRepository = applicationPDFRepository;
		this.disabilitiesService = disabilitiesService;
		this.dsaAdvisorAuthRepository = dsaAdvisorAuthRepository;
		this.courseDetailsService = courseDetailsService;
		this.findStudentService = findStudentService;
		this.quoteUploadService = quoteUploadService;
		this.equipmentPaymentService = equipmentPaymentService;

	}

	/**
	 * Generate application PDF.
	 */
	public boolean generatePDF(ApplicationResponse applicationResponse) throws IllegalAccessException {
		boolean pdfGenerated;

		Map<String, Object> htmlContentMap = new HashMap<>();
		setStudentDetails(applicationResponse, htmlContentMap);
		setCourseDetails(applicationResponse, htmlContentMap);
		setBankDetailsData(htmlContentMap, applicationResponse.getBankDetails());
		setDisabilitiesData(applicationResponse, htmlContentMap);
		setConsumablesData(applicationResponse, htmlContentMap);
		setEquipmentData(applicationResponse, htmlContentMap);

		setNMPHData(applicationResponse, htmlContentMap);
		setTravelExpData(applicationResponse, htmlContentMap);
		setAccommodationData(applicationResponse, htmlContentMap);
		setAssessmentFeeData(applicationResponse, htmlContentMap);
		setAdditionalInfoData(applicationResponse, htmlContentMap);

		setAdvisorDeclarationData(applicationResponse, htmlContentMap);
		setStudentDeclarationData(applicationResponse, htmlContentMap);
		javax.sql.rowset.serial.SerialBlob pdf = null;
		byte[] pdfBlob = generatePDFBlob(populatePDFHTML(PDF_DSA_APPLICATION_PDF_TEMPLATE_HTML, htmlContentMap),
				htmlContentMap);
		try {
			pdf = new javax.sql.rowset.serial.SerialBlob(pdfBlob);
			logger.info("PDF BLOB data generated successfully");
			pdfGenerated = true;
		} catch (Exception e) {
			logger.error("Error creating PDF blob", e);
			pdfGenerated = false;

		}
		if (pdfGenerated) {
			DSAApplicationPDF appPDF = new DSAApplicationPDF();
			appPDF.setPdf(pdf);
			appPDF.setDsaApplicationNumber(applicationResponse.getDsaApplicationNumber());
			appPDF.setStudentReferenceNumber(applicationResponse.getStudentReferenceNumber());
			applicationPDFRepository.save(appPDF);
			logger.info("PDF BLOB data saved successfully");
		}
		return pdfGenerated;
	}

	private void setAdditionalInfoData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		htmlContentMap.put("additionalInfo", applicationResponse.getAdditionalInfoText());
	}

	private void setAccommodationData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {

		List<AccommodationVO> accommodations = applicationResponse.getAccommodations();
		htmlContentMap.put(ACCOMMODATIONS_TOTAL,  accommodationTotalCost(accommodations));
		htmlContentMap.put(ACCOMMODATIONS, accommodations);

	}

	private byte[] generatePDFBlob(String html, Map<String, Object> htmlContentMap) {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ITextRenderer renderer = new ITextRenderer();

		try {
			renderer.setDocumentFromString(html);
			renderer.layout();
			renderer.createPDF(baos);
		} catch (DocumentException e) {
			logger.error("Failed to create the pdf", e);
			return null;
		}

		byte[] bytes = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
			logger.warn("Failed to close byte array my cause resource leak");
			return null;
		}
		logger.info("PDF BLOB generated successfully");

		return bytes;
	}

	private void setCourseDetails(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		try {
			CourseDetailsVO courseDetailsVO = courseDetailsService.findCourseDetailsFromDB(
					applicationResponse.getStudentReferenceNumber(), applicationResponse.getSessionCode());
			htmlContentMap.put(INSTITUTION, courseDetailsVO.getInstitutionName());
			htmlContentMap.put("COURSE", courseDetailsVO.getCourseName());
			htmlContentMap.put("STUDY_MODE", "Full-time");
			htmlContentMap.put("ACADEMIC_YEAR", courseDetailsVO.getAcademicYearFull());
		} catch (IllegalAccessException e) {
			logger.info("No course details found for student");
			e.printStackTrace();
		}

	}

	private void setStudentDetails(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		htmlContentMap.put(SAAS_REFERNECE_NUMBER, applicationResponse.getStudentReferenceNumber());
		htmlContentMap.put(STUDENT_FULL_NAME,
				applicationResponse.getFirstName() + " " + applicationResponse.getLastName());
		htmlContentMap.put("STUDENT_DOB",
				findStudentService
						.findStudentPersonDetailsStudByRefNumber(applicationResponse.getStudentReferenceNumber())
						.getDateOfBirth());
		htmlContentMap.put("STUDENT_FUND_ELIGIBILITY",
				applicationResponse.getFundingEligibilityStatus().getDescription());
	}

	private void setStudentDeclarationData(ApplicationResponse applicationResponse,
			Map<String, Object> htmlContentMap) {
		SectionStatusResponse sectionData = getSectionStatusData(Section.STUDENT_DECLARATION,
				applicationResponse.getSectionPartStatusList());
		htmlContentMap.put("STUDENT_DECLARATION_SUBMITTED_DATE", sectionData.getLastUpdatedDate());
	}

	private void setAdvisorDeclarationData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap)
			throws IllegalAccessException {

		applicationResponse.getSectionPartStatusList();

		SectionStatusResponse sectionData = getSectionStatusData(Section.ADVISOR_DECLARATION,
				applicationResponse.getSectionPartStatusList());

		DsaAdvisorAuthDetails dsaAdvisorAuthDetails = dsaAdvisorAuthRepository
				.findByEmailIgnoreCase(sectionData.getLastUpdatedBy());
		if (dsaAdvisorAuthDetails == null) {
			String message = String.format("No Auth details exist in DSA_ADVISOR_AUTH_DETAILS for email %s",
					sectionData.getLastUpdatedBy());
			logger.error(message);
			throw new IllegalAccessException(message);
		}
		htmlContentMap.put("ADVISOR_NAME", dsaAdvisorAuthDetails.getDsaAdvisor().getFirstName() + " "
				+ dsaAdvisorAuthDetails.getDsaAdvisor().getLastName());

		htmlContentMap.put("ADVISOR_ROLE", dsaAdvisorAuthDetails.getRoleName());
		htmlContentMap.put("ADVISOR_DECLARATION_SUBMITTED_DATE", sectionData.getLastUpdatedDate());

	}

	private void setTravelExpData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		List<TravelExpAllowanceVO> travelExpeses = applicationResponse.getTravelExpeses();
		if (travelExpeses != null) {
			Optional<TravelExpAllowanceVO> travelExp = travelExpeses.stream()
					.filter(t -> t.getTravelExpType().equals(TravelExpType.TAXI)).findFirst();
			htmlContentMap.put("travelExpeses", travelExpeses);
			if (travelExp.isPresent()) {
				htmlContentMap.put("taxiQuotes", travelExp.get().getTaxiProvidersList());
			}
		}

	}

	private void setNMPHData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		List<NMPHAllowanceVO> nmphItems = applicationResponse.getNmphAllowances();
		Double total = AllowancesHelper.nmphTotal(nmphItems);
		htmlContentMap.put("nmphItems", nmphItems);
		htmlContentMap.put("nmphTotal", currencyLocalisation(total));
	}

	private void setAssessmentFeeData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {

		htmlContentMap.put("feeItems", applicationResponse.getAssessmentFeeList());

	}

	private void setEquipmentData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		List<EquipmentAllowanceVO> equipments = applicationResponse.getEquipments();
		Double total = AllowancesHelper.equipmentsTotal(equipments);
		List<QuoteResultVO> allQuotaions = quoteUploadService
				.getAllQuotaions(applicationResponse.getDsaApplicationNumber());
		if (allQuotaions != null) {
			Double quotesTotal = AllowancesHelper.quotesTotal(allQuotaions);
			total = total + quotesTotal;
		}

		htmlContentMap.put("equipments", equipments);
		htmlContentMap.put("hasEquipments", (equipments != null && !equipments.isEmpty()));
		htmlContentMap.put("quotes", allQuotaions);
		htmlContentMap.put("hasQuotes", (allQuotaions != null && !allQuotaions.isEmpty()));

		htmlContentMap.put("equipmentsTotal", currencyLocalisation(total));
		DSALargeEquipemntPaymentFor paymentFor = equipmentPaymentService
				.getpaymentForDetails(applicationResponse.getDsaApplicationNumber());
		if (paymentFor != null) {
			LargeEquipmentPaymentType paymentType = paymentFor.getPaymentFor();

			if (paymentType.equals(LargeEquipmentPaymentType.STUDENT)) {
				htmlContentMap.put(PAYMENT_FOR_TEXT, "The student is responsible for paying the supplier.");
				htmlContentMap.put(PAYMENT_FOR_TEXT_HINT,
						"SAAS must pay the equipment, software, and accessories allowance into the student’s nominated bank account.");
			} else {
				htmlContentMap.put(PAYMENT_FOR_TEXT,
						"The " + htmlContentMap.get(INSTITUTION).toString() + " is responsible for paying supplier.");
				htmlContentMap.put(PAYMENT_FOR_TEXT_HINT,
						"SAAS must pay the  equipment, software, and accessories allowance to the institution.");
			}

		}

	}

	private void setConsumablesData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		List<ConsumableTypeVO> consumables = applicationResponse.getConsumables();
		Double consumablesTotal = getConsumablesTotal(consumables);
		htmlContentMap.put("consumables", consumables);
		htmlContentMap.put("consumablesTotal", currencyLocalisation(consumablesTotal));

	}

	private void setBankDetailsData(Map<String, Object> htmlContentMap, BankAccountVO bankDetails) {
		htmlContentMap.put("bankDetails", bankDetails);

	}

	private void setDisabilitiesData(ApplicationResponse applicationResponse, Map<String, Object> htmlContentMap) {
		htmlContentMap.put("DISABILITIES_MAP",
				populateDisabilitiesData(disabilitiesService, applicationResponse.getDsaApplicationNumber()));
	}

	private String populatePDFHTML(String htmlTemplatePath, Map<String, Object> templateModel) {
		Context thymeleafContext = new Context();
		thymeleafContext.setVariables(templateModel);
		String htmlBody = thymeleafTemplateEngine.process(htmlTemplatePath, thymeleafContext);
		return htmlBody;
	}
}
