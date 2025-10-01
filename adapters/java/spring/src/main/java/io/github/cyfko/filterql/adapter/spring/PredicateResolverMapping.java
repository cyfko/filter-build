package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyReference;

/**
 * Interface for creating custom predicate resolvers from filter definitions.
 * <p>
 * This interface allows advanced users to implement custom business logic
 * for complex filtering scenarios that cannot be expressed through simple
 * property path mappings.
 * </p>
 *
 * @param <E> the entity type
 * @param <P> the property reference type
 */
@FunctionalInterface
public interface PredicateResolverMapping<E, P extends Enum<P> & PropertyReference> {

    /**
     * Resolves a filter definition into a predicate resolver.
     *
     * @param definition the filter definition to resolve
     * @return a predicate resolver that can generate the appropriate predicate
     */
    PredicateResolver<E> resolve(FilterDefinition<P> definition);
}