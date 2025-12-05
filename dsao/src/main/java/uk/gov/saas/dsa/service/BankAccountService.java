package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.saas.dsa.domain.DSAApplicationBankAccount;
import uk.gov.saas.dsa.persistence.DSAApplicationStudBankAccountRepository;
import uk.gov.saas.dsa.vo.BankAccountVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

/**
 * DSA Bank account service
 */
@Service
public class BankAccountService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final DSAApplicationStudBankAccountRepository repo;

	public BankAccountService(DSAApplicationStudBankAccountRepository repo) {
		this.repo = repo;
	}

	public void savebankAcount(BankAccountVO bankAccountVO) {
		logger.info("Saving bank account with data {}", bankAccountVO);
		DSAApplicationBankAccount bankAccount = getExistingBankAccount(bankAccountVO.getDsaApplicationNumber());
		if (bankAccount == null) {
			logger.info("No Bank Account in db ");
			bankAccount = new DSAApplicationBankAccount();
			bankAccount.setDsaApplicationNumber(bankAccountVO.getDsaApplicationNumber());
			bankAccount.setStudentReferenceNumber(bankAccountVO.getStudentReferenceNumber());
			bankAccount.setAccountName(AllowancesHelper.toCapitaliseWord(bankAccountVO.getAccountName()));
			bankAccount.setSortCode(bankAccountVO.getSortCode());
			bankAccount.setAccountNumber(bankAccountVO.getAccountNumber());
			bankAccount.setPaymentFor(bankAccountVO.getPaymentFor());

		} else {
			logger.info("Bank Account in db {}", bankAccount);
			bankAccount.setAccountName(bankAccountVO.getAccountName());
			bankAccount.setSortCode(bankAccountVO.getSortCode());
			bankAccount.setAccountNumber(bankAccountVO.getAccountNumber());
			bankAccount.setPaymentFor(bankAccountVO.getPaymentFor());
		}
		repo.save(bankAccount);
	}

	public BankAccountVO getbankAccount(long dsaApplicationNumber) {
		DSAApplicationBankAccount bankAccount = getExistingBankAccount(dsaApplicationNumber);

		BankAccountVO bankAccountVO = ServiceUtil.populateBankDetailsVO(bankAccount);

		return bankAccountVO;
	}

	private DSAApplicationBankAccount getExistingBankAccount(long dsaApplicationNumber) {
		return repo.findByDsaApplicationNumber(dsaApplicationNumber);

	}

	@Transactional
	public void deleteByDSAApplicationNumber(Long dsaApplicationNumber) {
		logger.info("Delte BANK ACCOUNT FOR DSA application numebr:{}", dsaApplicationNumber);
		try {
			repo.deleteByDsaApplicationNumber(dsaApplicationNumber);
			logger.error("BANK ACCOUNT deleted sucessfully for {}" + dsaApplicationNumber);
		} catch (EmptyResultDataAccessException e) {
			logger.error("Item already removed id: " + dsaApplicationNumber);
		}

	}
}
