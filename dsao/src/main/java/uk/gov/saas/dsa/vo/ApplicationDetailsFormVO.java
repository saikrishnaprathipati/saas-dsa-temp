package uk.gov.saas.dsa.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import lombok.Getter;

@Data
public class ApplicationDetailsFormVO implements Serializable {
	@Getter(lombok.AccessLevel.NONE)
	@lombok.Setter(lombok.AccessLevel.NONE)
	private static final long serialVersionUID = 1L;

	@Getter(lombok.AccessLevel.NONE)
	@lombok.Setter(lombok.AccessLevel.NONE)
	private static final Logger logger = LogManager.getLogger(ApplicationDetailsFormVO.class);

	@NotBlank(message = "{courseMode.required}")
	private String courseMode;
 
	@NotEmpty(message = "{fundingType.required}")
	private List<String> fundingType;

	private List<String> hideProperties = new ArrayList<>();
	private List<String> completedProperties = new ArrayList<>();
}
