package io.github.cyfko.filterql.adapter.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Functional interface for building JPA predicates.
 * This interface allows for the creation of type-safe JPA predicates using the Criteria API.
 * 
 * @param <T> The entity type
 */
@FunctionalInterface
public interface Specification<T> {
    /**
     * Creates a JPA predicate from the given parameters.
     * 
     * @param root The root entity
     * @param query The criteria query
     * @param cb The criteria builder
     * @return The JPA predicate
     */
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
