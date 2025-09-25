package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

/**
 * JPA implementation of the Condition interface using CriteriaBuilder.
 * This adapter translates core conditions into JPA Predicate objects.
 */
public class JpaConditionAdapter<T> implements Condition {
    
    private final Predicate predicate;
    private final CriteriaBuilder criteriaBuilder;
    
    /**
     * Constructs a new JPA condition adapter.
     * 
     * @param predicate The JPA predicate
     * @param criteriaBuilder The JPA criteria builder
     */
    public JpaConditionAdapter(Predicate predicate, CriteriaBuilder criteriaBuilder) {
        this.predicate = predicate;
        this.criteriaBuilder = criteriaBuilder;
    }
    
    /**
     * Gets the underlying JPA Predicate.
     * 
     * @return The JPA Predicate
     */
    public Predicate getPredicate() {
        return predicate;
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
        if (!(other instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }
        
        JpaConditionAdapter<T> otherJpa = (JpaConditionAdapter<T>) other;
        Predicate combinedPredicate = criteriaBuilder.and(predicate, otherJpa.predicate);
        return new JpaConditionAdapter<T>(combinedPredicate, criteriaBuilder);
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
        if (!(other instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }
        
        JpaConditionAdapter<T> otherJpa = (JpaConditionAdapter<T>) other;
        Predicate combinedPredicate = criteriaBuilder.or(predicate, otherJpa.predicate);
        return new JpaConditionAdapter<T>(combinedPredicate, criteriaBuilder);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return A new condition representing the negation of this condition
     */
    @Override
    public Condition not() {
        Predicate negatedPredicate = criteriaBuilder.not(predicate);
        return new JpaConditionAdapter<T>(negatedPredicate, criteriaBuilder);
    }
}
