package uk.gov.saas.dsa.vo;

import lombok.Data;

/**
 * To populate the declarations on the UI
 */
@Data
public class DeclarationTypeVO {
    private long declarationTypeId;
    private String declarationCode;
    private String declarationTypeDesc;

}
