package dev.patricklehmann.fhirlab.shared.domain;

import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Shared repository contract for the project's aggregates.
 *
 * <p>Extends the bare {@link Repository} marker rather than {@code CrudRepository} so the exposed
 * surface is chosen explicitly: Spring Data implements only the methods declared here, which keeps
 * accidental capability out of the persistence layer.
 */
@NoRepositoryBean
public interface EntityRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S entity);

    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(Iterable<ID> ids);

    long count();

    void deleteById(ID id);

    void delete(T entity);

    void deleteAllById(Iterable<? extends ID> ids);

    void deleteAll(Iterable<? extends T> entities);

    void deleteAll();
}
