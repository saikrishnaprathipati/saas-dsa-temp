package uk.gov.saas.dsa.domain;

import java.io.Serializable;
import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Data;
import uk.gov.saas.dsa.domain.converters.EmailNotificationTypeConverter;
import uk.gov.saas.dsa.model.EmailNotificationType;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Entity
@Table(name = "DSA_EMAIL_NOTIFICATION", schema = "SGAS")
@Data
public class DSAEmailNotification implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 19618820928936332L;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "emailerJobRunsSeq", sequenceName = "emailer_job_runs_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emailerJobRunsSeq")
	private long id;

	@Column(name = "STUD_REF_NO", columnDefinition = DSAConstants.STUDENT_REFERENCE_NUMBER)
	private long studentReferenceNumber;

	@Column(name = "FAILURE_DATE")
	private Date failureDate;

	@Column(name = "FAILURE_REASON")
	private String failureReason;

	@Column(name = "SESSION_CODE")
	private int sessionCode;

	@Column(name = "SUCCESS_DATE")
	private Date successDate;

	@Column(name = "NOTIFICATION_TYPE")
	@Enumerated(EnumType.STRING)
	@Convert(converter = EmailNotificationTypeConverter.class)
	private EmailNotificationType notificationType;

}
