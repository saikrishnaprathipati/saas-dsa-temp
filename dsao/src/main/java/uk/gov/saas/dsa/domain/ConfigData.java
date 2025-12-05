package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "CONFIG_DATA")
public class ConfigData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ITEM_NAME", columnDefinition = "itemName")
	private String itemName;

	@Column(name = "CVAL")
	private String stringValue;

	@Column(name = "NVAL")
	private Integer numericalValue;

	@Column(name = "DVAL")
	private Date dateValue;
}
