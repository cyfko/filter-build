package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data JPA Condition Adapter implementing the Condition interface.
 * This adapter wraps Spring JPA Specifications.
 * 
 * @param <T> The entity type
 */
public class FilterCondition<T> implements Condition {
    private final Specification<T> specification;

    /**
     * Constructs a new Spring condition adapter.
     *
     * @param specification The Spring JPA specification
     */
    public FilterCondition(Specification<T> specification) {
        this.specification = specification;
    }

    /**
     * {@inheritDoc}
     *
     * @param other The other condition to combine with
     * @return A new condition representing the AND combination
     * @throws IllegalArgumentException if the other condition is not a Spring condition
     */
    @Override
    public Condition and(Condition other) {
        if (!(other instanceof FilterCondition<?>)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }

        FilterCondition<T> otherSpring = (FilterCondition<T>) other;
        return new FilterCondition<>(Specification.where(specification).and(otherSpring.specification));
    }

    /**
     * {@inheritDoc}
     *
     * @param other The other condition to combine with
     * @return A new condition representing the OR combination
     * @throws IllegalArgumentException if the other condition is not a Spring condition
     */
    @Override
    public Condition or(Condition other) {
        if (!(other instanceof FilterCondition<?>)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }
        
        FilterCondition<T> otherSpring = (FilterCondition<T>) other;
        return new FilterCondition<>(Specification.where(specification).or(otherSpring.specification));
    }

    /**
     * {@inheritDoc}
     *
     * @return A new condition representing the negation of this condition
     */
    @Override
    public Condition not() {
        return new FilterCondition<>(Specification.not(specification));
    }

    /**
     * Gets the underlying Spring JPA specification.
     *
     * @return The Spring JPA specification
     */
    public Specification<T> getSpecification() {
        return specification;
    }
}




