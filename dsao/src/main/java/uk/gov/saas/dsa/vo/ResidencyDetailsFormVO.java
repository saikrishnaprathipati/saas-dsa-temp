package uk.gov.saas.dsa.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import lombok.Getter;


@Data
public class ResidencyDetailsFormVO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Getter(lombok.AccessLevel.NONE)
	@lombok.Setter(lombok.AccessLevel.NONE)
	private static final Logger logger = LogManager.getLogger(ResidencyDetailsFormVO.class);

	private Long applicationId;
	
	@NotBlank(message = "{nationalityId.required}")
	private String nationalityId;
	
	private String birthCountry;
	
	private String ukBirthCountryCode;

	@NotBlank(message = "{ordResidentCountry.required}")
	private String ordResidentCountry;
	
	@NotBlank(message = "{dualNationality.required}")
	private String dualNationality;
	
	@NotBlank(message = "{ordResidentScot.required}")
	private String ordResidentScot;
	
	@NotBlank(message = "{inScotYear.required}")
	private String inScotYear;
	
	@NotBlank(message = "{ordResidentUK.required}")
	private String ordResidentUK;
	
	private List<String> hideProperties = new ArrayList<>();
	private List<String> completedProperties = new ArrayList<>();
}
