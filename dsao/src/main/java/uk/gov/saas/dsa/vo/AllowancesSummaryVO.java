package uk.gov.saas.dsa.vo;

import java.util.List;

import lombok.Data;
import uk.gov.saas.dsa.vo.consumables.ConsumableTypeVO;

@Data
public class AllowancesSummaryVO {
	List<ConsumableTypeVO> consumables;
}
