package uk.gov.saas.dsa.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UserIdAndApplicationId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2176014737179766398L;
	private Long applicationId;
	private String userId;
}
