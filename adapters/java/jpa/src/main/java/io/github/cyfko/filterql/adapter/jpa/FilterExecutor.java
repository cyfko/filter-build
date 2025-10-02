package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.PropertyReference;

import java.util.List;

/**
 * Interface defining a filter executor capable of retrieving entities
 * that satisfy a specified filter request.
 * <p>
 * Implementations must provide logic to execute the filtering query
 * based on the given {@link FilterRequest} containing filter definitions
 * and the entity type.
 * </p>
 * <p>
 * The filter request encapsulates filter criteria and condition composition logic.
 * </p>
 *
 * @author Frank
 * @since 1.0
 */
public interface FilterExecutor {

    /**
     * Finds and returns all entities of the specified class that satisfy the filter request criteria.
     *
     * @param <E>       the type of entity to be retrieved
     * @param <P>       the enum type defining the filterable properties (must extend Enum and implement {@link PropertyRef})
     * @param clazz     the {@code Class} object representing the entity type
     * @param request   the filter request containing criteria and filter logic
     * @return a list of entities matching the filter request
     * @throws IllegalArgumentException if the request or underlying conditions are invalid or not applicable
     */
    <E, P extends Enum<P> & PropertyRef> List<E> findAll(Class<E> clazz, FilterRequest<P> request) throws DSLSyntaxException;
}


