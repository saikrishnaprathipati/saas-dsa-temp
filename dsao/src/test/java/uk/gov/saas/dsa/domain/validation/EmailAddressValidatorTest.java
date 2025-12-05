package uk.gov.saas.dsa.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;

import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.service.AdvisorLookupService;
import uk.gov.saas.dsa.vo.CreateAccountFormVO;

@ExtendWith(SpringExtension.class)
public class EmailAddressValidatorTest {
	
	@MockitoBean
	private AdvisorLookupService advisorLookupService;
	
	private EmailAddressValidator emailAddressValidator;
	
	@BeforeEach
	public void setUp() {
		emailAddressValidator = new EmailAddressValidator(advisorLookupService);
	}

	@Test
	public void test_validate() {
		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("abc@gmail.com");
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(createAccountFormVO, "errors");
		
		DsaAdvisor dsaAdvisor = new DsaAdvisor();
		when(advisorLookupService.findByEmail(any())).thenReturn(dsaAdvisor);
		
		emailAddressValidator.validate(createAccountFormVO, bindingResult);
		List<ObjectError> errors = bindingResult.getAllErrors();
		
		assertEquals(Collections.EMPTY_LIST, errors);
	}
	
	@Test
	public void test_validateErrors() {
		CreateAccountFormVO createAccountFormVO = new CreateAccountFormVO();
		createAccountFormVO.setEmailAddress("");
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(createAccountFormVO, "errors");

		DsaAdvisor dsaAdvisor = new DsaAdvisor();
		when(advisorLookupService.findByEmail(any())).thenReturn(dsaAdvisor);
		
		emailAddressValidator.validate(createAccountFormVO, bindingResult);
		List<ObjectError> errors = bindingResult.getAllErrors();
		List<String> messageCodes = errors.stream().map(ObjectError::getCode).collect(Collectors.toList());
		assertEquals(Collections.singletonList("dsa.emailAddress.required"), messageCodes);
	}
}
