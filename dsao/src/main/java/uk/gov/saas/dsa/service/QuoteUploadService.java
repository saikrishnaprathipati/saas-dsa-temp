package uk.gov.saas.dsa.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.saas.dsa.domain.DSAQuotePDF;
import uk.gov.saas.dsa.domain.readonly.Stud;
import uk.gov.saas.dsa.persistence.DsaQuotePDFRepository;
import uk.gov.saas.dsa.vo.quote.QuoteDetailsFormVO;
import uk.gov.saas.dsa.vo.quote.QuoteResultVO;
import uk.gov.saas.dsa.web.helper.AllowancesHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.saas.dsa.web.helper.AllowancesHelper.currencyLocalisation;

@Service
public class QuoteUploadService {
	private static final String SPACE = " ";
	private static final String UNDER_SCORE = "_";
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final DsaQuotePDFRepository dsaQuotePDFRepository;
	private final FindStudentService findStudentService;

	@Autowired
	public QuoteUploadService(DsaQuotePDFRepository dsaQuotePDFRepository, FindStudentService findStudentService) {
		this.dsaQuotePDFRepository = dsaQuotePDFRepository;
		this.findStudentService = findStudentService;
	}

	public DSAQuotePDF checkQuoteExists(QuoteDetailsFormVO quoteDetailsFormVO) {
		return dsaQuotePDFRepository.findQuoteByQuoteReferenceIgnoreCaseAndSupplierIgnoreCase(AllowancesHelper.sanetizeSpecialCharacters(quoteDetailsFormVO.getQuoteReference()), quoteDetailsFormVO.getSupplier());
	}

	public DSAQuotePDF checkQuoteWithFileNameExists(String fileName) {
		return dsaQuotePDFRepository.findQuoteByFileName(fileName);
	}

	public DSAQuotePDF uploadQuoteForStudentApplication(MultipartFile[] files, MultipartFile quoteFile,
														QuoteDetailsFormVO quoteDetailsFormVO) {
		DSAQuotePDF dsaQuotePDF = new DSAQuotePDF();
		dsaQuotePDF.setDsaApplicationNumber(quoteDetailsFormVO.getDsaApplicationNumber());
		dsaQuotePDF.setStudentRefNumber(quoteDetailsFormVO.getStudentReferenceNumber());
		dsaQuotePDF.setSessionCode(quoteDetailsFormVO.getSessionCode());
		dsaQuotePDF.setAdvisorId(quoteDetailsFormVO.getAdvisorId());
		dsaQuotePDF.setSupplier(quoteDetailsFormVO.getSupplier());
		dsaQuotePDF.setQuoteCost(new BigDecimal(quoteDetailsFormVO.getTotalCost()));
		dsaQuotePDF.setQuoteReference(AllowancesHelper.sanetizeSpecialCharacters(quoteDetailsFormVO.getQuoteReference()));
		dsaQuotePDF.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaQuotePDF.setLastUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
		dsaQuotePDF.setFileName(quoteFile.getOriginalFilename());

		try {
			javax.sql.rowset.serial.SerialBlob quote = new javax.sql.rowset.serial.SerialBlob(quoteFile.getBytes());
			dsaQuotePDF.setQuote(quote);
			logger.info("PDF BLOB data generated successfully");

			if (quoteDetailsFormVO.getQuoteId() != 0) {
				dsaQuotePDF.setQuoteId(quoteDetailsFormVO.getQuoteId());
			}
			dsaQuotePDF = dsaQuotePDFRepository.save(dsaQuotePDF);
			return dsaQuotePDF;
		} catch (Exception e) {
			logger.error("Error creating PDF blob", e);
			return new DSAQuotePDF();
		}
	}

	public List<QuoteResultVO> fetchAllQuotesForStudentApplication(long dsaApplicationNumber) {
		return getAllQuotaions(dsaApplicationNumber);
	}

	public DSAQuotePDF fetchQuoteByReferenceAndSupplier(QuoteDetailsFormVO quoteDetailsFormVO) {
		return dsaQuotePDFRepository.findQuoteByQuoteReferenceIgnoreCaseAndSupplierIgnoreCase(AllowancesHelper.sanetizeSpecialCharacters(quoteDetailsFormVO.getQuoteReference()), quoteDetailsFormVO.getSupplier());
	}

	public List<QuoteResultVO> getAllQuotaions(long dsaApplicationNumber) {
		List<DSAQuotePDF> quotes = dsaQuotePDFRepository
				.findQuotesByDsaApplicationNumber(dsaApplicationNumber);
		return quotes.stream().map(this::quoteResult).collect(Collectors.toList());
	}

	public List<QuoteResultVO> fetchUploadedQuotesForStudentApplication(List<Long> quoteIds) {
		List<DSAQuotePDF> quotes = dsaQuotePDFRepository.findQuotesByQuoteIds(quoteIds);
		List<QuoteResultVO> quoteResultVOList = quotes.stream().map(this::quoteResult).collect(Collectors.toList());

		// Update first and last names
		for (QuoteResultVO obj : quoteResultVOList) {
			Stud stud = this.findStudentService.findStud(obj.getStudentReferenceNumber());
			String firstName = stud.getForenames();
			String lastName = stud.getSurname();
			obj.setFirstName(firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase());
			obj.setLastName(lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase());
		}

		return quoteResultVOList;
	}

	public void deleteQuoteByQuoteId(long quoteId) {
		DSAQuotePDF dsaQuotePDF = dsaQuotePDFRepository.findQuoteByQuoteId(quoteId);

		if (null != dsaQuotePDF) {
			dsaQuotePDFRepository.delete(dsaQuotePDF);
		}
	}

	private QuoteResultVO quoteResult(DSAQuotePDF dsaQuotePDF) {
		QuoteResultVO quoteResultVO = new QuoteResultVO();
		quoteResultVO.setQuoteId(dsaQuotePDF.getQuoteId());
		quoteResultVO.setDsaApplicationNumber(dsaQuotePDF.getDsaApplicationNumber());
		quoteResultVO.setStudentReferenceNumber(dsaQuotePDF.getStudentRefNumber());
		quoteResultVO.setSessionCode(dsaQuotePDF.getSessionCode());
		quoteResultVO.setAdvisorId(dsaQuotePDF.getAdvisorId());
		quoteResultVO.setCost(dsaQuotePDF.getQuoteCost());
		quoteResultVO.setCostStr(currencyLocalisation(dsaQuotePDF.getQuoteCost().doubleValue()));
		quoteResultVO.setQuoteReference(AllowancesHelper.sanetizeSpecialCharacters(dsaQuotePDF.getQuoteReference()));
		quoteResultVO.setSupplier(dsaQuotePDF.getSupplier());
		try {
			quoteResultVO.setFileName(dsaQuotePDF.getFileName());
			quoteResultVO.setSize(bytesIntoHumanReadable(
					Long.parseLong(String.valueOf(dsaQuotePDF.getQuote().getBinaryStream().available()))));
			quoteResultVO.setQuote(dsaQuotePDF.getQuote().getBinaryStream().readAllBytes());
		} catch (IOException | SQLException e) {
			logger.error("A problem occured while retrieving blob", e);
		}

		return quoteResultVO;
	}

	private String bytesIntoHumanReadable(long bytes) {
		long kilobyte = 1024;
		long megabyte = kilobyte * 1024;
		long gigabyte = megabyte * 1024;
		long terabyte = gigabyte * 1024;

		if ((bytes >= 0) && (bytes < kilobyte)) {
			return bytes + " B";

		} else if ((bytes >= kilobyte) && (bytes < megabyte)) {
			return (bytes / kilobyte) + " KB";

		} else if ((bytes >= megabyte) && (bytes < gigabyte)) {
			return (bytes / megabyte) + " MB";

		} else if ((bytes >= gigabyte) && (bytes < terabyte)) {
			return (bytes / gigabyte) + " GB";

		} else if (bytes >= terabyte) {
			return (bytes / terabyte) + " TB";

		} else {
			return bytes + " Bytes";
		}
	}

	static byte[] convertFileToByteArray(String filePath) throws IOException {
		File file = new File(filePath);
		try (FileInputStream fileInputStream = new FileInputStream(file);
			 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			for (int len; (len = fileInputStream.read(buffer)) != -1; ) {
				byteArrayOutputStream.write(buffer, 0, len);
			}
			return byteArrayOutputStream.toByteArray();
		}
	}
}
