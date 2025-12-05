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
@Table(name = "DSA_APPLICATION", schema = "STEPS")
public class DSASTEPSApplication implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ID")
	private long id;

	@Column(name = "STUD_REF_NO")
	private long studentReferenceNumber;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "DSA_APP_STATUS")
	private int dsaAppStatus;

	@Column(name = "LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Column(name = "LAST_UPDATED_ON")
	private Date lastUpdatedOn;

	@Column(name = "IS_ONLINE")
	private String isOnline;
}
