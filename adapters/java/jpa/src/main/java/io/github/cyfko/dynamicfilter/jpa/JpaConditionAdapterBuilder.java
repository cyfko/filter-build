package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

/**
 * Builder interface for creating JPA condition adapters.
 * Each implementation defines how to build a JPA predicate from PropertyRef, Operator, and value.
 * 
 * @param <T> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public interface JpaConditionAdapterBuilder<T, P extends Enum<P> & PropertyRef> {
    
    /**
     * Builds a JPA condition adapter from the given parameters.
     * 
     * @param ref The property reference (type-safe)
     * @param op The operator
     * @param value The value as object
     * @return A JPA condition adapter
     */
    JpaConditionAdapter<T> build(P ref, Operator op, Object value);
}
