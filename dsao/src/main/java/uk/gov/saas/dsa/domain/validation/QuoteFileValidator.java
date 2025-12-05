package uk.gov.saas.dsa.domain.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.saas.dsa.sophos.SophosConnection;
import uk.gov.saas.dsa.sophos.SophosConnectionManager;
import uk.gov.saas.dsa.web.controller.uploader.QuoteUploadController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class QuoteFileValidator {
	private static Logger logger = LogManager.getLogger(QuoteUploadController.class);

	@Value("${dsa.sophos.destinationDirectory}")
	private String destinationDirectory;

	public void validateQuoteFiles(Errors errors, MultipartFile file) {
		String fileContentType = file.getContentType();
		String fileName = file.getOriginalFilename();

		if(hasSpecialCharacters(fileName)) {
			errors.rejectValue("files", "quote.file.name");
		}

		if(isFileLengthGreaterThan200(fileName)) {
			errors.rejectValue("files", "quote.file.name.length");
		}

		if (!virusCheckFileHasPassed(file)) {
			logger.info("Virus check File");
			errors.rejectValue("files", "quote.file.virus.check.file");
		}

		if (!("application/pdf").equalsIgnoreCase(fileContentType) || file.getSize() == 0) {
			errors.rejectValue("files", "quote.file.invalid.file.type");
		}

		if (file.getSize() > 5010000) {
			errors.rejectValue("files", "quote.file.size");
		}
	}

	public static boolean hasSpecialCharacters(String str) {
		return str != null && str.matches("[^a-zA-Z0-9]");
	}

	public static boolean isFileLengthGreaterThan200(String str) {
		return str != null && str.length() >200;
	}

	public boolean virusCheckFileHasPassed(MultipartFile quote) {
		Path destinationPath = Paths.get(destinationDirectory, quote.getOriginalFilename());
		File file = new File(destinationDirectory, Objects.requireNonNull(quote.getOriginalFilename()));

        try(FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(quote.getBytes());
			fos.close();
			logger.info("Copied file to Sophos server {}", destinationPath);
		} catch (IOException e) {
			logger.info("ERROR : Unable to copy file to Sophos server {}", e.getMessage());
        }

		SophosConnection sc;
		boolean cleanImage = false;
		try {
			sc = SophosConnectionManager.getConnection();
			//sc = new SophosConnection(sophosServer, sophosServerPort);
			logger.info("Connecting to Sophos anti-virus {}", sc.getIpOrName());

			if (sc.isConnected()) {
				logger.info("Connected to Sophos anti-virus {}", sc.isConnected());
				cleanImage = sc.virusCheckFile(destinationPath.toString(), "Param Not used", false); //boolean not used either
				logger.info("Virus check result, image is clean : "+cleanImage);
				sc.disconnect();
			} else {
				logger.info("ERROR : Sophos anti-virus connection failed {}");
			}
		} catch (Exception e) {
			logger.info("ERROR : Unable to connect to Sophos anti-virus {}", e.getMessage());
		} finally {
			if(!file.isDirectory() &&  !file.getAbsolutePath().equalsIgnoreCase(destinationDirectory)) {
				boolean fileDeleted = file.delete();
				logger.info("File deleted from sophos server {}", fileDeleted);
			} else {
				logger.info("Cannot delete File : {} as it is a directory ", file);
			}
		}
		return cleanImage;
	}
}
