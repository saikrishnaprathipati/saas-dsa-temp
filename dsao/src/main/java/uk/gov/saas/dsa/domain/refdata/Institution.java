package uk.gov.saas.dsa.domain.refdata;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "INST")
public class Institution implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "INST_CODE")
	private String instCode;

	@Column(name = "INST_NAME")
	private String instName;

	@Column(name = "LOCATION_IND")
	private String locationIndicator;
}