package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Spring Data JPA Filter Service for dynamic filtering.
 * This service provides convenient methods for applying filters to Spring Data JPA repositories.
 * 
 * @param <T> The entity type
 * @param <P> The PropertyRef enum for this entity
 */
public class SpringFilterService<T, P extends Enum<P> & PropertyRef & PathShape> {
    
    private final SpringContextAdapter<T, P> contextAdapter;
    private final JpaRepository<T, ?> repository;

    /**
     * Constructs a new Spring filter service.
     *
     * @param contextAdapter The context adapter for managing conditions
     * @param repository The JPA repository for data access
     */
    public SpringFilterService(SpringContextAdapter<T, P> contextAdapter, JpaRepository<T, ?> repository) {
        this.contextAdapter = contextAdapter;
        this.repository = repository;
    }

    /**
     * Adds a filter condition to the context.
     *
     * @param filterKey The key to identify this condition
     * @param definition The filter definition
     */
    public void addFilter(String filterKey, FilterDefinition<P> definition) {
        contextAdapter.addCondition(filterKey, definition);
    }

    /**
     * Finds all entities matching the specified filter.
     *
     * @param filterKey The key of the filter to apply
     * @return List of matching entities
     */
    public List<T> findAll(String filterKey) {
        Specification<T> specification = contextAdapter.getSpecification(filterKey);
        if (specification == null) {
            return repository.findAll();
        }
        return ((JpaSpecificationExecutor<T>) repository).findAll(specification);
    }

    /**
     * Finds all entities matching the specified filter with pagination.
     *
     * @param filterKey The key of the filter to apply
     * @param pageable The pagination information
     * @return Page of matching entities
     */
    public Page<T> findAll(String filterKey, Pageable pageable) {
        Specification<T> specification = contextAdapter.getSpecification(filterKey);
        if (specification == null) {
            return repository.findAll(pageable);
        }
        return ((JpaSpecificationExecutor<T>) repository).findAll(specification, pageable);
    }

    /**
     * Counts entities matching the specified filter.
     *
     * @param filterKey The key of the filter to apply
     * @return Number of matching entities
     */
    public long count(String filterKey) {
        Specification<T> specification = contextAdapter.getSpecification(filterKey);
        if (specification == null) {
            return repository.count();
        }
        return ((JpaSpecificationExecutor<T>) repository).count(specification);
    }

    /**
     * Checks if any entities match the specified filter.
     *
     * @param filterKey The key of the filter to apply
     * @return True if at least one entity matches
     */
    public boolean exists(String filterKey) {
        Specification<T> specification = contextAdapter.getSpecification(filterKey);
        if (specification == null) {
            return repository.count() > 0;
        }
        return ((JpaSpecificationExecutor<T>) repository).count(specification) > 0;
    }

    /**
     * Gets the context adapter for advanced usage.
     *
     * @return The context adapter
     */
    public SpringContextAdapter<T, P> getContextAdapter() {
        return contextAdapter;
    }
}
