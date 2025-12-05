package uk.gov.saas.dsa.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.saas.dsa.domain.Authorities;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface AuthoritiesRepository extends CrudRepository<Authorities,Long> {
	
    Authorities findById(int authorityId);
    Authorities findByNameIgnoreCase(String name);
    @Query("SELECT a.id FROM Authorities a WHERE a.name LIKE ':name'")
    ArrayList<Integer> getAuthorityIdListForNameLike(String name);
}
