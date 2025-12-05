package uk.gov.saas.dsa.domain.readonly;

import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "STUD_SESSION", schema = "STEPS")
public class StudSession implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 3409753711843795966L;

	@Id
	@Column(name = "STUD_REF_NO")
	private long studentReferenceNumber;

	/**
	 * The stud.
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STUD_REF_NO")
	@MapsId
	private Stud stud;

	/**
	 * The student session id.
	 */
	@Column(name = "STUD_SESSION_ID")
	private Long studentSessionId;

	/**
	 * The session code.
	 */
	@Column(name = "SESSION_CODE")
	private Integer sessionCode;

	/**
	 * EU Student or Not
	 */
	@Column(name = "EU_FLAG")
	private String euFlag;
}