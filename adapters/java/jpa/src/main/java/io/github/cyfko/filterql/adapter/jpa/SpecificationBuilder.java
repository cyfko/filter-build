package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

/**
 * Builder interface for creating JPA condition adapters.
 * Each implementation defines how to build a JPA predicate from PropertyRef, Operator, and value.
 * 
 * @param <T> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public interface SpecificationBuilder<T, P extends Enum<P> & PropertyRef> {
    
    /**
     * Builds a JPA condition adapter from the given parameters.
     * 
     * @param ref The property reference (type-safe)
     * @param op The operator
     * @param value The value as object
     * @return A JPA specification
     */
    Specification<T> build(P ref, Operator op, Object value);
}
