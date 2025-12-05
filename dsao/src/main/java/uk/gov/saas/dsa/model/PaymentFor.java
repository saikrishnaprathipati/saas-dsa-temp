package uk.gov.saas.dsa.model;

import lombok.Getter;
import uk.gov.saas.dsa.web.helper.DSAConstants;

@Getter
public enum PaymentFor {

	MAIN_FUNDING_AND_DSA_PAYMNET(DSAConstants.MAIN_FUNDING_AND_DISABLED_STUDENT_ALLOWANCES,
			DSAConstants.MAIN_FUNDING),
	DSA_PAYMENT(DSAConstants.DISABLED_STUDENT_ALLOWANCES, DSAConstants.CREATE_NEW_NOMINEE);

	private String descriptionForPortal;
	private String descriptionForPDF;

	PaymentFor(String descriptionForPortal, String descriptionForPDF) {

		this.descriptionForPortal = descriptionForPortal;
		this.descriptionForPDF = descriptionForPDF;
	}

}