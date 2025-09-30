package io.github.cyfko.filterql.core.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Functional interface for resolving a logical condition into a JPA predicate.
 *
 * <p>
 * A {@code PredicateResolver} represents a deferred predicate: it does not
 * directly hold a JPA predicate, but knows how to create one when
 * given the appropriate JPA context (root, query, builder).
 * </p>
 * 
 * <p>
 * This interface bridges FilterQL conditions with JPA Criteria API.
 * </p>
 *
 * @param <E> the entity type
 */
@FunctionalInterface
public interface PredicateResolver<E> {

    /**
     * Resolves this condition into a JPA predicate.
     *
     * @param root  the root entity
     * @param query the criteria query
     * @param criteriaBuilder the criteria builder
     * @return the resolved JPA predicate
     */
    Predicate resolve(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}

