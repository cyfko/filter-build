package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * JPA implementation of the Condition interface using CriteriaBuilder.
 * This adapter translates core conditions into JPA Predicate objects.
 */
public class JpaConditionAdapter<T> implements Condition {
    
    private final Predicate predicate;
    private final CriteriaBuilder criteriaBuilder;
    
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
    
    @Override
    public Condition and(Condition other) {
        if (!(other instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }
        
        JpaConditionAdapter<T> otherJpa = (JpaConditionAdapter<T>) other;
        Predicate combinedPredicate = criteriaBuilder.and(predicate, otherJpa.predicate);
        return new JpaConditionAdapter<T>(combinedPredicate, criteriaBuilder);
    }
    
    @Override
    public Condition or(Condition other) {
        if (!(other instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Cannot combine with non-JPA condition");
        }
        
        JpaConditionAdapter<T> otherJpa = (JpaConditionAdapter<T>) other;
        Predicate combinedPredicate = criteriaBuilder.or(predicate, otherJpa.predicate);
        return new JpaConditionAdapter<T>(combinedPredicate, criteriaBuilder);
    }
    
    @Override
    public Condition not() {
        Predicate negatedPredicate = criteriaBuilder.not(predicate);
        return new JpaConditionAdapter<T>(negatedPredicate, criteriaBuilder);
    }
}
