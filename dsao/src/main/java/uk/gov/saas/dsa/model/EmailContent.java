package uk.gov.saas.dsa.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class EmailContent {

	private String fromAddress;
	private String fromName;
	private String toAddress;
	private String subject;
	private String content;
	private String body;
	private String emailTemplate;
	private Map<String, Object> model = new HashMap<>();
	private String homePage;
	private String fullName;
	private boolean isHTML = true;

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean isHTML() {
		return isHTML;
	}
	public void setHTML(boolean isHTML) {
		this.isHTML = isHTML;
	}

	public String getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	public String getHomePage() {
		return homePage;
	}

	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

 

}
