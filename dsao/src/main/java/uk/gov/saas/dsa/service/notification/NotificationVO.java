package uk.gov.saas.dsa.service.notification;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.saas.dsa.model.EmailNotificationType;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = { "studentReferenceNumber", "notificationType", "sessionCode" })
public class NotificationVO {
	private long studentReferenceNumber;
	private long id;
	private EmailNotificationType notificationType;

	private int sessionCode;
	private String updatedBy;
}
