package uk.gov.saas.dsa.vo;

import lombok.Data;

@Data
public class SaveDeviceFormVO {

	private String email;
	private String userId;
	private boolean rememberDevice;
}
