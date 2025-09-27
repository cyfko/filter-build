package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;

import io.github.cyfko.filterql.core.utils.ClassUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Objects;

/**
 * JPA implementation of the Condition interface using CriteriaBuilder.
 * This adapter translates core conditions into JPA Predicate objects.
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class ConditionAdapter<T> implements Condition {
    private final Specification<T> specification;
    
    /**
     * Constructs a new JPA condition adapter.
     * 
     * @param specification The {@link Specification} of this condition
     */
    public ConditionAdapter(Specification<T> specification) {
        Objects.requireNonNull(specification, "specification cannot be null");
        this.specification = specification;
    }

    /**
     * Gets the underlying JPA Predicate.
     * 
     * @return The JPA Predicate
     */
    public Specification<T> getSpecification() {
        return specification;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param other The other condition to combine with
     * @return A new condition representing the AND combination
     * @throws IllegalArgumentException if the other condition is not a JPA condition
     */
    @Override
    public Condition and(Condition other) {
        if (!(other instanceof ConditionAdapter<?>)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }
        
        Specification<T> andSpecification = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = this.specification.toPredicate(root, query, cb);
            Predicate otherPredicate = ((ConditionAdapter<T>) other).specification.toPredicate(root, query, cb);
            return cb.and(predicate, otherPredicate);
        };
        return new ConditionAdapter<>(andSpecification);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param other The other condition to combine with
     * @return A new condition representing the OR combination
     * @throws IllegalArgumentException if the other condition is not a JPA condition
     */
    @Override
    public Condition or(Condition other) {
        if (!(other instanceof ConditionAdapter)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }

        Specification<T> andSpecification = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = this.specification.toPredicate(root, query, cb);
            Predicate otherPredicate = ((ConditionAdapter<T>) other).specification.toPredicate(root, query, cb);
            return cb.or(predicate, otherPredicate);
        };
        return new ConditionAdapter<>(andSpecification);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return A new condition representing the negation of this condition
     */
    @Override
    public Condition not() {
        Specification<T> andSpecification = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.not(this.specification.toPredicate(root, query, cb));
        return new ConditionAdapter<>(andSpecification);
    }
}
