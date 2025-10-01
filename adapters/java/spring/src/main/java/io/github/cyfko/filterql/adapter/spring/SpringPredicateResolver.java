package io.github.cyfko.filterql.adapter.spring;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Spring Data JPA specific predicate resolver.
 * <p>
 * This interface provides a type-safe way to resolve conditions into JPA predicates
 * while maintaining compatibility with the core framework-agnostic PredicateResolver.
 * </p>
 *
 * @param <T> the entity type
 */
@FunctionalInterface
public interface SpringPredicateResolver<T> {

    /**
     * Resolves this condition into a JPA {@link Predicate}.
     *
     * @param root  the root entity
     * @param query the criteria query
     * @param cb    the criteria builder
     * @return the resolved JPA predicate
     */
    Predicate resolve(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}