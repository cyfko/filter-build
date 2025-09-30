package io.github.cyfko.filterql.core.mappings;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyReference;

/**
 * Defines how a logical property reference is resolved into a {@link PredicateResolver}
 * for the entity type {@code E}.
 *
 * <p>
 * The mapping bridges the gap between a filter definition (property reference,
 * operator, and value) and the corresponding {@link PredicateResolver}.
 *
 * <p>Example:
 * <ul>
 *   <li>Property reference: {@code USER_NAME}</li>
 *   <li>Operator: {@code EQUAL}</li>
 *   <li>Value: {@code "Alice"}</li>
 *   <li>Result: a {@link PredicateResolver} producing
 *       {@code cb.equal(root.get("name"), "Alice")}</li>
 * </ul>
 *
 * @param <E> the entity type
 * @param <P> the enum type representing logical property references
 */
public interface PredicateResolverMapping<E, P extends Enum<P> & PropertyReference>
        extends ReferenceMapping<E> {

    /**
     * Resolves the given filter definition into a {@link PredicateResolver}.
     *
     * @param definition the filter definition containing the property, operator, and value
     * @return a {@link PredicateResolver} that can produce a JPA {@link jakarta.persistence.criteria.Predicate}
     */
    PredicateResolver<E> resolve(FilterDefinition<P> definition);
}

