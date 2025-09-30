package io.github.cyfko.filterql.core.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Functional interface for resolving a logical condition into a JPA {@link Predicate}.
 *
 * <p>
 * A {@code PredicateResolver} represents a deferred predicate: it does not
 * directly hold a JPA {@link Predicate}, but knows how to create one when
 * given a JPA context ({@link Root}, {@link CriteriaQuery}, {@link CriteriaBuilder}).
 *
 * @param <T> the entity type
 */
@FunctionalInterface
public interface PredicateResolver<T> {

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

