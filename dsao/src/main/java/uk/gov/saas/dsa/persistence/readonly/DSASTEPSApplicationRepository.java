package uk.gov.saas.dsa.persistence.readonly;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import uk.gov.saas.dsa.domain.readonly.DSASTEPSApplication;

@Repository("dsaSTEPSApplicationRepository")
public interface DSASTEPSApplicationRepository extends ReadOnlyRepository<DSASTEPSApplication, Long> {

	List<DSASTEPSApplication> findAllBySessionCodeAndIsOnlineAndLastUpdatedOnAfter(int sessionCode, String isOnline,
			Date successDate);

	List<DSASTEPSApplication> findAllBySessionCodeAndIsOnline(int sessionCode, String isOnline);

	List<DSASTEPSApplication> findByStudentReferenceNumberAndSessionCodeAndIsOnline(long studentReferenceNumber,
			int sessionCode, String isOnline);
}