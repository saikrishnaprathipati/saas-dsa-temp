package uk.gov.saas.dsa.model;

import java.util.Date;

import lombok.Data;

/**
 * Section Status Response
 */
@Data
public class SectionStatusResponse {
	private Section section;
	private SectionStatus sectionStatus;
	/**
	 * To display hyperlink on the screen.
	 */
	private boolean isEnabeldToView;
	private Date lastUpdatedDate;
	private String lastUpdatedBy;
}
