package uk.gov.saas.dsa.vo.award;

import java.util.Date;

import lombok.Data;
import uk.gov.saas.dsa.model.DSAAwardProcessedStatus;

@Data
public class DSAAwardVO {
	private long studentReferenceNumber;
	private int currentSession;
	private String awardDate;
	private Date awarded;
	private String awardStatus;
	private DSAAwardProcessedStatus fundStatus;
	private String institution;
	private String academicYear;
	private boolean isAwardedInPhaseOne;

}
