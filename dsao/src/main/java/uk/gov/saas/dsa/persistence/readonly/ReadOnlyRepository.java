package uk.gov.saas.dsa.persistence.readonly;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface ReadOnlyRepository<T, I extends Serializable> extends Repository<T, I> {

	// I equals ID
    T findById(I primaryKey);

}
