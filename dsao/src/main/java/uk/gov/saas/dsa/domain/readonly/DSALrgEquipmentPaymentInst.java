package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "SGAS", name = "DSA_LRG_EQP_INST_PYMT")
public class DSALrgEquipmentPaymentInst implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 9182589405080234815L;

	@Id
	@Column(name = "ID")
	private long id;

	@Column(name = "INSTITUTE_NAME")
	private String instituteName;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;

}