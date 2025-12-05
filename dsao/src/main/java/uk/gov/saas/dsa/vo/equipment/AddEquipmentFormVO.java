package uk.gov.saas.dsa.vo.equipment;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;

@Data
public class AddEquipmentFormVO {
    private long dsaApplicationNumber;
    private long studentReferenceNumber;
    private long id;

    @NotBlank(message = "{equipment.productName.required}")
    private String productName;

    @Size(max = 300, message = "{equipment.description.maxLength}")
    private String description;

    @NotBlank(message = "{equipment.cost.required}")
    private String cost;

    private Set<String> orderedFields = new LinkedHashSet<>();

    public LinkedHashSet<String> getOrderedFields() {
        orderedFields.add("productName");
        orderedFields.add("description");
        orderedFields.add("cost");
        return (LinkedHashSet<String>) orderedFields;
    }
}
