package uk.gov.saas.dsa.model;

import lombok.Data;

@Data
public class Response {

	private ResponseCode responseCode;
	private Object model;
	
	public Response(ResponseCode responseCode, Object model) {
		super();
		this.responseCode = responseCode;
		this.model = model;
	}
}
