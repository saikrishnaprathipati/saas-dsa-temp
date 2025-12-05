package uk.gov.saas.dsa.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "INST_GROUPING", schema = "SGAS")
public class InstituteGrouping implements Serializable {

	private static final long serialVersionUID = 19618820928936332L;
	@Id
	@Column(name = "INST_CODE")
	private String instCode;

	@Column(name = "PARENT_INST_CODE")
	private String parentInstCode;

	@Column(name = "PARENT_INST_DISPLAY_NAME")
	private String parentInstDisplayName;

}
