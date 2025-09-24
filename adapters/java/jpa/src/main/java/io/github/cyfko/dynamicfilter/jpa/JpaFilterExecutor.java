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
public class JpaFilterExecutor implements FilterExecutor {
    
    private final EntityManager entityManager;
    
    public JpaFilterExecutor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public <T> List<T> execute(Condition globalCondition, Class<T> entityClass) {
        if (!(globalCondition instanceof JpaConditionAdapter)) {
            throw new IllegalArgumentException("Condition must be a JpaConditionAdapter");
        }
        
        JpaConditionAdapter jpaCondition = (JpaConditionAdapter) globalCondition;
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        query.where(jpaCondition.getPredicate());
        
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
}
