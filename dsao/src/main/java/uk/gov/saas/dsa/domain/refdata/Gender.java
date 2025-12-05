package uk.gov.saas.dsa.domain.refdata;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
@Data
@Entity
@Table(name="GENDER")
public class Gender implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="GENDER_ID")
	private Long genderId;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="LAST_UPDATED_BY")
	private String lastUpdatedBy;
	
	@Column(name="GENDER_CODE")
	private String genderCode;

	
}
