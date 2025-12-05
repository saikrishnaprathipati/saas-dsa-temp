package uk.gov.saas.dsa.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "DSA_CMS_TEXT", schema = "SGAS")
public class DSACMSText implements Serializable {
	
	private static final long serialVersionUID = 19618820928936332L;

	@Id
	private String identifier;

	private String content;

}
