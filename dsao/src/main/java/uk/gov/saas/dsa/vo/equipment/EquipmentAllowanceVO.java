package uk.gov.saas.dsa.vo.equipment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.saas.dsa.domain.refdata.LargeEquipmentPaymentType;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = {"productName", "description", "cost"})
public class EquipmentAllowanceVO {
    private long id;
    private long dsaApplicationNumber;
    private long studentReferenceNumber;
    private long dsaQuoteId;
    private String productName;
    private String description;
    private BigDecimal cost;
    private String costStr;
    private String itemType;
    private LargeEquipmentPaymentType paymentType;
    private String paytoHEI;
}
