package uk.gov.saas.dsa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * About you section status
 * 
 * @author Siva Chimpiri
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSection {
	// Initialising it to default start screen
	private String lastAccessedPage;

	// Initialising it to default status
	private ApplicationSummaryStatus sectionStatus;
	@JsonIgnore
	public boolean isCompleted() {
		return ApplicationSummaryStatus.COMPLETED.equals(this.getSectionStatus());
	}
}
