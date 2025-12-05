package uk.gov.saas.dsa.domain.refdata;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "PROVIDER", schema = "SGAS")
public class Provider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PROVIDER_ID")
	private Long providerId;

	@Column(name = "PROVIDER_NAME")
	private String providerName;
}