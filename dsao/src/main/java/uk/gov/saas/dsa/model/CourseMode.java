package uk.gov.saas.dsa.model;

import lombok.Getter;
/**
 * Course type
 * @author Siva Chimpiri
 *
 */
@Getter
public enum CourseMode {

	FULL_TIME("Full-time", "infotext.fulltime"), PART_TIME("Part-time", "infotext.parttime"),
	DISTANCE_LEARNING("Distance learning", "infotext.distanceLearning");

	private String description;
	private String infoTextKey;

	CourseMode(String description, String infoTextKey) {
		this.description = description;
		this.infoTextKey = infoTextKey;
	}

}