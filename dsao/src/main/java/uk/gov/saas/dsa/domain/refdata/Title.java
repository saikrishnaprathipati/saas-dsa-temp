package uk.gov.saas.dsa.domain.refdata;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * Title entiry 
 * @author Siva Chimpiri
 *
 */
@Getter
@Setter
@ToString
@Entity
@Table(name="TITLE")
public class Title implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="TITLE_ID")
	private Long titleId;
	
	@Column(name="LEGACY_CODE")
	private String legacyCode;
	
	@Column(name="DESCRIPT")
	private String description;
	
	@Column(name="LAST_UPDATED_BY")
	private String lastUpdatedBy;
	
	@Column(name="LAST_UPDATED_ON")
	private Date lastUpdatedOn;
	
	@Column(name="IS_ACTIVE")
	private String isActive;
}