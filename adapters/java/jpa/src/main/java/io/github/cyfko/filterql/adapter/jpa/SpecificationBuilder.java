package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.function.Function;

/**
 * Builder interface for creating JPA condition adapters.
 * Each implementation defines how to build a JPA predicate from PropertyRef, Operator, and value.
 * 
 * @param <E> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public interface SpecificationBuilder<E, P extends Enum<P> & PropertyRef> {
    
    /**
     * Builds a JPA condition adapter from the given parameters.
     * 
     * @param ref The property reference (type-safe)
     * @param op The operator
     * @param value The value as object
     * @return A JPA specification
     */
    Specification<E> build(P ref, Operator op, Object value);
}
