package uk.gov.saas.dsa.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.saas.dsa.domain.DSAQuotePDF;

import java.util.List;

@Repository("dsaQuotePDFRepository")
public interface DsaQuotePDFRepository extends CrudRepository<DSAQuotePDF, String> {
	List<DSAQuotePDF> findQuotesByStudentRefNumber(long studentRefNumber);
	List<DSAQuotePDF> findQuotesByDsaApplicationNumber(long dsaApplicationNumber);

	DSAQuotePDF findQuoteByQuoteId(long quoteId);

	@Query(value = "select * from DSA_QUOTE_PDF where QUOTE_ID in (:quoteIds)", nativeQuery = true)
	List<DSAQuotePDF> findQuotesByQuoteIds(@Param("quoteIds") List<Long> quoteIds);

	DSAQuotePDF findQuoteByQuoteReferenceIgnoreCaseAndSupplierIgnoreCase(String quoteReference, String supplier);

	DSAQuotePDF findQuoteByFileName(String fileName);
}
