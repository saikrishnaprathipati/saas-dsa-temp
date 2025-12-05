package uk.gov.saas.dsa.domain.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import uk.gov.saas.dsa.domain.DSAQuotePDF;
import uk.gov.saas.dsa.model.NumberType;
import uk.gov.saas.dsa.service.QuoteUploadService;
import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.validateNumber;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COST;
import static uk.gov.saas.dsa.web.helper.DSAConstants.COST_REGEX;

@Component
public class QuoteDetailsFormValidator {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private QuoteUploadService quoteUploadService;

	@Autowired
	public QuoteDetailsFormValidator(QuoteUploadService quoteUploadService) {
		this.quoteUploadService = quoteUploadService;
	}

	public void validate(String action, Object target, Errors errors, BindingResult bindingResult) {
		QuoteDetailsFormVO quoteDetailsFormVO = (QuoteDetailsFormVO) target;
		validateQuoteDetailsForm(action, errors, quoteDetailsFormVO, bindingResult);
	}

	private void validateQuoteDetailsForm( String action, Errors errors, QuoteDetailsFormVO quoteDetailsFormVO, BindingResult bindingResult) {
		logger.info("QuoteDetailsFormValidator quoteDetailsFormVO {} ", quoteDetailsFormVO);

		String supplier = quoteDetailsFormVO.getSupplier().toLowerCase();
		String quoteReference = quoteDetailsFormVO.getQuoteReference().toLowerCase();
		String cost = quoteDetailsFormVO.getTotalCost();

		if (StringUtils.isBlank(supplier)) {
			errors.rejectValue("supplier", "quote.details.supplier");
		}

		if (StringUtils.isBlank(quoteReference)) {
			errors.rejectValue("quoteReference", "quote.details.reference");
		}

		if (StringUtils.isBlank(cost)) {
			errors.rejectValue("totalCost", "quote.details.cost");
		} else {
			quoteDetailsFormVO.setTotalCost(validateNumber(bindingResult, COST, cost,
					"totalCost", "quote.cost.invalid", NumberType.COST_XXXXX_YY, COST_REGEX));
		}

		DSAQuotePDF quote = quoteUploadService.checkQuoteExists(quoteDetailsFormVO);
		logger.info("QuoteDetailsFormValidator quoteDetailsFormVO {} ", quoteDetailsFormVO);
		logger.info("QuoteDetailsFormValidator quote {} ", quote);

		if(quote != null) {
			if(isChange(quoteDetailsFormVO, quote)) {
				logger.info("Changing existing quote {} ", quote.getQuoteId());
			} else {
				logger.info("quote exists {} ", quote.getQuoteId());
				errors.rejectValue("quoteReference", "quote.details.reference.exists");
			}
		}
	}

	private static boolean isChange(QuoteDetailsFormVO quoteDetailsFormVO, DSAQuotePDF quote) {
		return quote.getQuoteId() == quoteDetailsFormVO.getQuoteId()
				&& null != quoteDetailsFormVO.getExistingAction()
				&& quoteDetailsFormVO.getExistingAction().contains("Change");
	}
}
