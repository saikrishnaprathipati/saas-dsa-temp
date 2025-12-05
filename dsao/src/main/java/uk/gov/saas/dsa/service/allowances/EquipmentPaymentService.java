package uk.gov.saas.dsa.service.allowances;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.DSALargeEquipemntPaymentFor;
import uk.gov.saas.dsa.domain.readonly.DSALrgEquipmentPaymentInst;
import uk.gov.saas.dsa.domain.readonly.StudCourseYear;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;
import uk.gov.saas.dsa.persistence.DSALargeEquipemntPaymentForRepository;
import uk.gov.saas.dsa.persistence.readonly.DSALrgEquipmentPaymentInstRepository;
import uk.gov.saas.dsa.persistence.readonly.StudCourseYearRepository;
import uk.gov.saas.dsa.service.ApplicationService;
import uk.gov.saas.dsa.service.FindStudentService;
import uk.gov.saas.dsa.service.QuoteUploadService;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;

/**
 * Equipment Allowances Service
 */
@Service
public class EquipmentPaymentService {
	private final DSALrgEquipmentPaymentInstRepository dsaLrgEquipmentPaymentInstRepository;
	private final StudCourseYearRepository studCourseYearRepository;
	private final DSALargeEquipemntPaymentForRepository dsaLargeEquipemntPaymentForRepository;
	private final QuoteUploadService quoteUploadService;
	private final EquipmentService equipmentService;
	private final ApplicationService applicationService;
	private final Logger logger = LogManager.getLogger(this.getClass());
	public EquipmentPaymentService(EquipmentService equipmentService, QuoteUploadService quoteUploadService,
			DSALargeEquipemntPaymentForRepository dsaLargeEquipemntPaymentForRepository,
			ApplicationService applicationService, StudCourseYearRepository studCourseYearRepository,
			DSALrgEquipmentPaymentInstRepository dsaLrgEquipmentPaymentInstRepository) {
		this.equipmentService = equipmentService;
		this.quoteUploadService = quoteUploadService;
		this.dsaLrgEquipmentPaymentInstRepository = dsaLrgEquipmentPaymentInstRepository;
		this.studCourseYearRepository = studCourseYearRepository;
		this.dsaLargeEquipemntPaymentForRepository = dsaLargeEquipemntPaymentForRepository;
		this.applicationService = applicationService;
	}

	public LargeEquipmentPaymentType getPaymentType(long dsaApplicationNo) {
		DSALargeEquipemntPaymentFor byDsaApplciationNumber = getpaymentForDetails(dsaApplicationNo);
		if (byDsaApplciationNumber != null) {
			return byDsaApplciationNumber.getPaymentFor();
		} else {
			logger.error("No records found for HW student dsa applciation id - {}", dsaApplicationNo);
			return LargeEquipmentPaymentType.STUDENT;
		}
	}

	public void createPaymentFor(long dsaApplicationNo, LargeEquipmentPaymentType type) {
		DSALargeEquipemntPaymentFor entity = getpaymentForDetails(dsaApplicationNo);
		if (entity != null && !entity.getPaymentFor().equals(type)) {
			entity.setPaymentFor(type);
			entity.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
			dsaLargeEquipemntPaymentForRepository.save(entity);
		} else {
			DSAApplicationsMade applicationsMade = applicationService.findByDsaApplicationNumber(dsaApplicationNo);
			createEntity(dsaApplicationNo, applicationsMade.getStudentReferenceNumber(),
					applicationsMade.getSessionCode(), type);
		}
	}

	private void createEntity(long dsaApplicationNo, long studRefNo, int sessionCode, LargeEquipmentPaymentType type) {

		DSALargeEquipemntPaymentFor newEntity = new DSALargeEquipemntPaymentFor();
		newEntity.setDsaApplicationNumber(dsaApplicationNo);
		newEntity.setStudentRefNumber(studRefNo);
		newEntity.setSessionCode(sessionCode);
		newEntity.setPaymentFor(type);
		newEntity.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		newEntity.setLastUpdatedDate(newEntity.getCreatedDate());
		dsaLargeEquipemntPaymentForRepository.save(newEntity);

	}

	public void deletepaymentType(long dsaApplicationNo) {
		DSALargeEquipemntPaymentFor byDsaApplciationNumber = getpaymentForDetails(dsaApplicationNo);
		if (byDsaApplciationNumber != null) {
			dsaLargeEquipemntPaymentForRepository.deleteById(dsaApplicationNo);
		}

	}

	public boolean isPaymentInstitution(long studentRefNumber, int sessionCode) {
		boolean isSame = false;
		StudCourseYear studCourseYear = studCourseYearRepository
				.findByStudentReferenceNumberAndSessionCodeAndLatestCourseIndicator(studentRefNumber, sessionCode,
						FindStudentService.LATEST_CODE_INDICATOR_YES);
		if (studCourseYear != null) {
			String instituteName = studCourseYear.getInstituteName();

			DSALrgEquipmentPaymentInst paymentInst = findByInstrituteName(instituteName);

			if (paymentInst != null) {
				isSame = true;
			}
		}

		return isSame;

	}

	private DSALrgEquipmentPaymentInst findByInstrituteName(String instituteName) {
		DSALrgEquipmentPaymentInst paymentInst = dsaLrgEquipmentPaymentInstRepository
				.findByInstituteNameIgnoreCase(instituteName);
		return paymentInst;
	}

	public DSALargeEquipemntPaymentFor getpaymentForDetails(long dsaApplicationNumber) {
		DSALargeEquipemntPaymentFor entity = null;
		Optional<DSALargeEquipemntPaymentFor> byId = dsaLargeEquipemntPaymentForRepository
				.findById(dsaApplicationNumber);
		if (byId.isPresent()) {
			entity = byId.get();
		}
		return entity;
	}

	public boolean hasEquipments(long dsaApplicationNumber) {
		boolean hasEquipments = false;
		List<EquipmentAllowanceVO> allEquipmentAllowances = equipmentService
				.getAllEquipmentAllowances(dsaApplicationNumber);

		List<QuoteResultVO> quotaions = quoteUploadService.getAllQuotaions(dsaApplicationNumber);

		if ((allEquipmentAllowances != null && allEquipmentAllowances.size() >= 1)
				|| (quotaions != null && quotaions.size() >= 1)) {
			hasEquipments = true;
		}
		return hasEquipments;
	}

	public DSAApplicationsMade findByDsaApplicationNumberAndStudentReferenceNumber(long dsaApplicationNumber,
			long studentRefNumber) {
		return applicationService.findByDsaApplicationNumberAndStudentReferenceNumber(dsaApplicationNumber,
				studentRefNumber);
	}

}
