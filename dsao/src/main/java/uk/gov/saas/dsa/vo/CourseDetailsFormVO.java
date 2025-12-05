package uk.gov.saas.dsa.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;


@Data
public class CourseDetailsFormVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long applicationId;
	
	private String instCode;
	
	@NotBlank(message = "{instName.required}")
	@Pattern(regexp = "(?!SELECT$).*", message = "{instName.required}")
	@Pattern(regexp = "[a-zA-Z0-9 \\-\\(\\)\']+", message = "{instName.invalid}")
	@Size(max = 50, message = "{instName.invalid}")
	private String instName;
	
	@NotBlank(message = "{courseName.required}")
	@Pattern(regexp = "[a-zA-Z0-9 \\-\\(\\)\']+", message = "{courseName.invalid}")
	@Size(max = 50, message = "{courseName.invalid}")
	private String courseName;
	
	private String startMonth;

	private String startYear;
	
	@NotBlank(message = "{currentYear.required}")
	@Pattern(regexp = "[1-9]|10", message = "{currentYear.invalid}")
	private String currentYear;
	
	@NotBlank(message = "{yearsToCompleteCourse.required}")
	@Pattern(regexp = "[1-9]|10", message = "{yearsToCompleteCourse.invalid}")
	private String yearsToCompleteCourse;
	
	@NotBlank(message = "{courseMode.required}")
	private String courseMode;
	
	@NotBlank(message = "{dsaQualificationType.required}")
	private String dsaQualificationType;
	
	private String customQualType;
	
	private String dsaOnlyCourseName;
	
	private List<String> hideProperties = new ArrayList<>();
	private List<String> completedProperties = new ArrayList<>();

}
