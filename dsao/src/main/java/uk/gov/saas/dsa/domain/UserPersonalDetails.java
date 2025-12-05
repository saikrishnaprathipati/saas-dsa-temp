package uk.gov.saas.dsa.domain;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@ToString
@Getter
@Setter
@Entity
@Table(name="USER_PERSONAL_DETAILS")
@DynamicUpdate(value = true)
public class UserPersonalDetails implements Serializable {
	
	private static final long serialVersionUID = 4L;	
	
	@Id	
	@Column(name="USER_ID")
	private String userId;

	@Column(name="TITLE")
	@NotBlank(message = "Title is mandatory")
	private String titleType;
	
	@Column(name="SURNAME")
	@NotBlank(message = "{lastname.required}")
	@Size(max = 25, message = "{lastname.maxLength}")
	@Pattern(regexp = "^[a-zA-Z \\-\\']*$", message = "{lastname.invalid}")
	private String surname;
	
	@Column(name="FORENAME")
	@NotBlank(message = "{forename.required}")
	@Size(max = 25, message = "{forename.maxLength}")
	@Pattern(regexp = "^[a-zA-Z \\-\\']*$", message = "{forename.invalid}")
	private String forename;
	
	@Column(name="EMAIL_ADDR")
	@NotBlank(message = "email is mandatory")
	private String emailAddress;
	
	@Column(name="MOBILE_TEL_NO")
	private String mobileNumber;

 
	@OneToOne(cascade = CascadeType.ALL, mappedBy="user", fetch=FetchType.LAZY)
	@Valid
	private StudentPersonalDetails studentPersonalDetails; 
 	

	public UserPersonalDetails(String userId, String titleType, String surname, String forename, String emailAddress, String mobileNumber) {
		this.userId = userId;
		this.titleType = titleType;
		this.surname = surname;
		this.forename = forename;
		this.emailAddress = emailAddress;
		this.mobileNumber = mobileNumber;
	}
	
	/**
	 * 
	 */
	public UserPersonalDetails() {
		super();
	}
}
