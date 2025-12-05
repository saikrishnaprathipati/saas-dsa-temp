package uk.gov.saas.dsa.domain.refdata;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="NATIONALITY")
public class Nationality implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="NATIONALITY_ID")
	private Long nationalityId;
	
	@Column(name="LAST_UPDATED_BY")
	private String lastUpdatedBy;
	
	@Column(name="LAST_UPDATED_ON")
	private Date lastUpdatedOn;
	
	@Column(name="IS_ACTIVE")
	private String isActive;

	@Column(name="NATIONALITY_REGION")
	private String nationalityRegion;
	
	@Column(name="NATIONALITY_NAME")
	private String nationalityName;	
	
	@Column(name="COUNTRY_CODE")
	private Integer countryCode;

}
