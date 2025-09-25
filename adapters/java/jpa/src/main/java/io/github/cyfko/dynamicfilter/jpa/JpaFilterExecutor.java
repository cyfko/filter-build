package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.FilterExecutor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * JPA implementation of the FilterExecutor interface.
 * This adapter executes filter conditions using JPA CriteriaQuery.
 */
public class JpaFilterExecutor<T> implements FilterExecutor {
    
    private final JpaContextAdapter<T,?> context;
    
    public JpaFilterExecutor(JpaContextAdapter<T,?> context) {
        this.context = context;
    }
    
    @Override
    public <T> List<T> execute(Condition globalCondition) {
        if (!(globalCondition instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Condition must be a JpaConditionAdapter");
        }
        
        JpaConditionAdapter jpaCondition = (JpaConditionAdapter) globalCondition;
        

        
        query.where(jpaCondition.getPredicate());
        
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
}
