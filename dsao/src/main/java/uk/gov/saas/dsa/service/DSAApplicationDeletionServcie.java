package uk.gov.saas.dsa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationTravelExp;
import uk.gov.saas.dsa.persistence.DSAApplicationAssessmentFeeRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationPDFRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationSectionStatusRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudBankAccountRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudConsumablesRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudDisabilitiesRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudEquipmentRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudNMPHRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudTravelExpRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationStudTravelProviderRepository;
import uk.gov.saas.dsa.persistence.DSAApplicationsMadeRepository;
import uk.gov.saas.dsa.persistence.EmailNotificationRepository;
import uk.gov.saas.dsa.persistence.readonly.DSAApplicationCompleteRepository;

@Service
public class DSAApplicationDeletionServcie {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private DSAApplicationSectionStatusRepository applicationSectionStatusRepository;
	private DSAApplicationStudConsumablesRepository applicationStudConsumablesRepository;
	private DSAApplicationStudDisabilitiesRepository applicationStudDisabilitiesRepository;
	private DSAApplicationStudEquipmentRepository dsaApplicationStudEquipmentRepository;
	private DSAApplicationStudBankAccountRepository accountRepository;
	private DSAApplicationStudNMPHRepository applicationStudNMPHRepository;
	private DSAApplicationAssessmentFeeRepository applicationAssessmentFeeRepository;
	private DSAApplicationStudTravelProviderRepository applicationStudTravelProviderRepository;
	private DSAApplicationStudTravelExpRepository applicationStudTravelExpRepository;
	private DSAApplicationPDFRepository applicationPDFRepository;
	private EmailNotificationRepository emailNotificationRepository;
	private DSAApplicationCompleteRepository applicationCompleteRepository;
	private DSAApplicationsMadeRepository applicationsMadeRepository;

	public DSAApplicationDeletionServcie(DSAApplicationSectionStatusRepository applicationSectionStatusRepository,
			DSAApplicationStudConsumablesRepository applicationStudConsumablesRepository,
			DSAApplicationStudDisabilitiesRepository applicationStudDisabilitiesRepository,
			DSAApplicationStudEquipmentRepository dsaApplicationStudEquipmentRepository,
			DSAApplicationStudBankAccountRepository accountRepository,
			DSAApplicationStudNMPHRepository applicationStudNMPHRepository,
			DSAApplicationAssessmentFeeRepository applicationAssessmentFeeRepository,
			DSAApplicationStudTravelProviderRepository applicationStudTravelProviderRepository,
			DSAApplicationStudTravelExpRepository applicationStudTravelExpRepository,
			DSAApplicationPDFRepository applicationPDFRepository,
			EmailNotificationRepository emailNotificationRepository,
			DSAApplicationCompleteRepository applicationCompleteRepository,
			DSAApplicationsMadeRepository applicationsMadeRepository) {

		this.applicationSectionStatusRepository = applicationSectionStatusRepository;
		this.applicationStudConsumablesRepository = applicationStudConsumablesRepository;
		this.applicationStudDisabilitiesRepository = applicationStudDisabilitiesRepository;
		this.dsaApplicationStudEquipmentRepository = dsaApplicationStudEquipmentRepository;
		this.accountRepository = accountRepository;
		this.applicationStudNMPHRepository = applicationStudNMPHRepository;
		this.applicationAssessmentFeeRepository = applicationAssessmentFeeRepository;
		this.applicationStudTravelProviderRepository = applicationStudTravelProviderRepository;
		this.applicationStudTravelExpRepository = applicationStudTravelExpRepository;
		this.applicationPDFRepository = applicationPDFRepository;
		this.emailNotificationRepository = emailNotificationRepository;
		this.applicationCompleteRepository = applicationCompleteRepository;
		this.applicationsMadeRepository = applicationsMadeRepository;
	}

	@Transactional
	public void deleteDSAAplicationFootPrints(int sessionCode, long dsaApplicationNo, long studRefNo) {

		deleteSectionStatus(dsaApplicationNo);
		deleteDisabilities(dsaApplicationNo);
		deleteAllowances(dsaApplicationNo);
		deleteBankAccount(dsaApplicationNo);
		deleteAssessmentFee(dsaApplicationNo);

		deletePDF(dsaApplicationNo);
		deleteCompleteDSAAplication(dsaApplicationNo);
		deleteApplciationsMade(dsaApplicationNo);
		deleteNotifications(studRefNo, sessionCode);
		logger.info("Dsa foot prints succesfully deleted for student ref no {}", studRefNo);
	}

	@Transactional
	public void deleteDSAAplicationPDF(int sessionCode, long dsaApplicationNo, long studRefNo) {
		deletePDF(dsaApplicationNo);
		logger.info("Dsa application pdf successfully deleted for student ref no {}", studRefNo);
	}

	private void deleteAllowances(long dsaApplicationNo) {
		deleteConsumables(dsaApplicationNo);
		deleteEquipments(dsaApplicationNo);
		deleteNMPH(dsaApplicationNo);
		deleteTravelExp(dsaApplicationNo);
	}

	private void deleteNotifications(long studRefNo, int sessionCode) {
		emailNotificationRepository.deleteByStudentReferenceNumberAndSessionCode(studRefNo, sessionCode);
	}

	private void deleteApplciationsMade(long dsaApplicationNo) {
		applicationsMadeRepository.deleteById(dsaApplicationNo);
	}

	private void deleteCompleteDSAAplication(long dsaApplicationNo) {
		applicationCompleteRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deletePDF(long dsaApplicationNo) {
		applicationPDFRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteTravelExp(long dsaApplicationNo) {
		List<DSAApplicationTravelExp> travelExpItems = applicationStudTravelExpRepository
				.findByDsaApplicationNumber(dsaApplicationNo);

		applicationStudTravelProviderRepository.deleteByTravelExpIn(travelExpItems);
		applicationStudTravelExpRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteAssessmentFee(long dsaApplicationNo) {
		applicationAssessmentFeeRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteNMPH(long dsaApplicationNo) {
		applicationStudNMPHRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteBankAccount(long dsaApplicationNo) {
		accountRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteEquipments(long dsaApplicationNo) {
		dsaApplicationStudEquipmentRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteDisabilities(long dsaApplicationNo) {
		applicationStudDisabilitiesRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteConsumables(long dsaApplicationNo) {
		applicationStudConsumablesRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

	private void deleteSectionStatus(long dsaApplicationNo) {
		applicationSectionStatusRepository.deleteByDsaApplicationNumber(dsaApplicationNo);
	}

}
