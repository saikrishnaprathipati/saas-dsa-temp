package uk.gov.saas.dsa.domain.readonly;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "DSA_APP_STATUS", schema = "SGAS")
public class DSAApplicationStatus implements Serializable {
	/**
	 * 
	 * 1 Offline Application 2 Received 3 Awarded 4 Pended with HEI/Student 5 Pended
	 * with SAAS 6 Rejected 7 Application Withdrawn
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "DSA_APP_STATUS_ID")
	private int dsaAppStatusId;

	@Column(name = "DSA_APP_STATUS")
	private String dsaAppStatus;

	@Column(name = "IS_ACTIVE")
	private String isActive;

}
