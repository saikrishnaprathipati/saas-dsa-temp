package uk.gov.saas.dsa.vo;

import lombok.Data;

/**
 * Application Key Data Form VO this will be used to pass the primary keys and
 * other key data to pass in the headers from the html pages
 */
@Data
public class ApplicationKeyDataFormVO {

	private long studentReferenceNumber;
	private long dsaApplicationNumber;
}
