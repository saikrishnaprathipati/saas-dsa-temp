package uk.gov.saas.dsa.vo.award;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import uk.gov.saas.dsa.model.DSAAwardProcessedStatus;
import uk.gov.saas.dsa.vo.accommodation.AccommodationVO;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;
import uk.gov.saas.dsa.vo.equipment.EquipmentAllowanceVO;
import uk.gov.saas.dsa.vo.nmph.NMPHAllowanceVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAllowanceVO;
import uk.gov.saas.dsa.vo.travelExp.TravelExpAwardAllowanceVO;

@Data
@Accessors(fluent = true, chain = true)
public class DSAAwardDetailsVO {
	private long studentReferenceNumber;
	private long dsaApplicationNumber;
	private String studentFullName;
	private String studentFirstName;
	private String institution;

	private String academicYear;
	private int currentSession;
	private String awardDate;
	private String awardStatus;
	private DSAAwardProcessedStatus fundStatus;

	private List<ConsumableTypeVO> consumableItems;
	private String consumableItemsTotal;

	private List<EquipmentAllowanceVO> equipments;
	private List<EquipmentAllowanceVO> lineItemEquipments;
	private List<EquipmentAllowanceVO> quoteEquipments;
	private String equipmentsTotal;

	List<NMPHAllowanceVO> nmphItems;
	private String nmphTotal;

	List<TravelExpAwardAllowanceVO> travelExpItems;
	private String travelExpTotal;
	private String travelExpPartTotal;
	
	private String taxiTravelExpText;
	private String otherTravelExpText;
	
	private String paymentToHEI;

	private BigDecimal accomTotal;
	private List<AccommodationVO> accommodations;

}
