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
@Table(name="COUNTRY")
public class Country implements Serializable{

	private static final long serialVersionUID = -6434104903687186784L;
	
	@Id
	@Column(name = "COUNTRY_CODE")
	private Long countryCode;

	@Column(name = "LONG_NAME")
	private String longName;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "IS_ACTIVE")
	private String isActive;

	@Column(name = "UK_COUNTRY")
	private String ukCountry;

}
