package uk.gov.saas.dsa.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.saas.dsa.domain.DSAApplicationsMade;
import uk.gov.saas.dsa.domain.readonly.DSAApplicationStatus;
import uk.gov.saas.dsa.domain.readonly.DSASTEPSApplication;
import uk.gov.saas.dsa.model.OverallApplicationStatus;
import uk.gov.saas.dsa.persistence.readonly.DSASTEPSApplicationRepository;
import uk.gov.saas.dsa.persistence.refdata.DSAApplicationStatusRepository;
import uk.gov.saas.dsa.service.notification.StepsStatus;

/**
 * DSA Application status service
 */
@Service
public class ApplicationStatusService {
	private static final Logger logger = LogManager.getLogger(ApplicationStatusService.class);
	private static final String IS_ONLINE_YES = "Y";
    private DSAApplicationStatusRepository applicationStatusRepository;
	private DSASTEPSApplicationRepository dsaSTEPSApplicationRepository;
	private ConfigDataService configDataService;
	private ApplicationService applicationService;

	public ApplicationStatusService(DSAApplicationStatusRepository applicationStatusRepository,
			DSASTEPSApplicationRepository dsaSTEPSApplicationRepository, ConfigDataService configDataService,
			ApplicationService applicationService) {

		this.applicationStatusRepository = applicationStatusRepository;
		this.dsaSTEPSApplicationRepository = dsaSTEPSApplicationRepository;
		this.configDataService = configDataService;
		this.applicationService = applicationService;

	}

	public OverallApplicationStatus deriveApplicationStatus(long studRefNo, int sessionCode) {
		logger.info("Deriving the DSA Application status for the student ref no {} and sessionCode {}", studRefNo, sessionCode);
		OverallApplicationStatus dsaStatus = null;
		DSASTEPSApplication stepsApplication = getStepsApplication(studRefNo, sessionCode);
		DSAApplicationsMade dsaApplication = applicationService.findLatestDSAApplicationsMadeByStudentReferenceNumberAndSessionCode(studRefNo, sessionCode);
		logger.info("stepsApplication:{}", stepsApplication);
		if (stepsApplication != null) {
			int statusId = stepsApplication.getDsaAppStatus();

			Optional<DSAApplicationStatus> dbStatusOption = getAplpciationStatuses().stream()
					.filter(t -> t.getDsaAppStatusId() == statusId).findAny();

			if (dbStatusOption.isPresent()) {
				DSAApplicationStatus dsaApplicationStatus = dbStatusOption.get();
				logger.info("dsaApplicationStatus in DB:{}", dsaApplicationStatus);

				String stepsStatusDesc = dsaApplicationStatus.getDsaAppStatus();
				Optional<StepsStatus> statusOption = Arrays.asList(StepsStatus.values()).stream()
						.filter(t -> t.getStepsStatus().equalsIgnoreCase(stepsStatusDesc)).findFirst();
				if (!statusOption.isPresent()) {
					throw new IllegalArgumentException(
							"Something wrong with the steps status:" + stepsStatusDesc
							+ " for studRefNo: " + studRefNo);
				}

				StepsStatus statusType = statusOption.get();

				switch (statusType) {

				case APPLICATION_WITHDRAWN:
					dsaStatus = OverallApplicationStatus.WITHDRAWN;
					break;
				case STEPS_RECEIVED:
					dsaStatus = OverallApplicationStatus.RECEIVED;
					break;

				case PENDED_WITH_HEI_STUDENT:
					dsaStatus = OverallApplicationStatus.PENDING;
					break;
				case AWARDED:
					dsaStatus = OverallApplicationStatus.AWARDED;
					break;
				case REJECTED:
					dsaStatus = OverallApplicationStatus.NOT_AWARDED;
					break;
				case PENDED_WITH_SAAS:
					dsaStatus = OverallApplicationStatus.IN_PROGRESS;
					break;
				default:
					break;
				}
				logger.info("Steps status {} to DSA Status {}", stepsStatusDesc, dsaStatus);
			}
		} else if (dsaApplication != null) {
			dsaStatus = dsaApplication.getOverallApplicationStatus();
		} else {
			dsaStatus = OverallApplicationStatus.NOT_AWARDED;
		}
		logger.info("DSA application Status to show {}", dsaStatus);
		return dsaStatus;
	}

	private DSASTEPSApplication getStepsApplication(long studRefNo, int sessionCode) {
		List<DSASTEPSApplication> stepsApplciation = dsaSTEPSApplicationRepository
				.findByStudentReferenceNumberAndSessionCodeAndIsOnline(studRefNo,
						sessionCode, IS_ONLINE_YES);

		return stepsApplciation.stream().findFirst().orElse(null);
	}

	private List<DSAApplicationStatus> getAplpciationStatuses() {
		List<DSAApplicationStatus> stepsStatuses = applicationStatusRepository.findAll();
		return stepsStatuses;
	}
}
