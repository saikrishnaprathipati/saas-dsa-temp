package uk.gov.saas.dsa.persistence.refdata;
 

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.saas.dsa.domain.refdata.Gender;
 
/**
 * Gender Respository 
 * @author Siva Chimpiri
 *
 */
public interface GenderRespository extends JpaRepository<Gender, Long> {

}
