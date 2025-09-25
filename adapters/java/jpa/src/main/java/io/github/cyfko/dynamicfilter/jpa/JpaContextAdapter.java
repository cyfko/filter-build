package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.Context;
import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA implementation of the Context interface.
 * This adapter uses JpaConditionAdapterBuilder to create conditions.
 * 
 * @param <T> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public class JpaContextAdapter<T, P extends Enum<P> & PropertyRef> implements Context {
    private final Root<T> root;
    private final CriteriaQuery<T> query;
    private final CriteriaBuilder criteriaBuilder;
    private final Map<String, JpaConditionAdapter<T>> filters;
    private final SpecificationBuilder<T, P> specificationBuilder;
    
    public JpaContextAdapter(Class<T> entityClass, EntityManager entityManager, SpecificationBuilder<T, P> specificationBuilder) {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(entityClass);
        this.root = query.from(entityClass);
        this.filters = new HashMap<>();
        this.specificationBuilder = specificationBuilder;
    }
    
    public void addCondition(String filterKey, FilterDefinition<P> definition) {
        // Get PropertyRef and Operator directly (type-safe, no resolution needed)
        P propertyRef = definition.getRef();
        Operator operator = definition.getOperator();
        
        // Validate that the property supports this operator
        propertyRef.validateOperator(operator);
        
        // Build condition using the builder and store it
        Predicate predicate = specificationBuilder.build(propertyRef, operator, definition.getValue())
                .toPredicate(root,query, criteriaBuilder);

        JpaConditionAdapter<T> condition = new JpaConditionAdapter<>(predicate, criteriaBuilder);
        filters.put(filterKey, condition);
    }
    
    @Override
    public Condition getCondition(String filterKey) {
        JpaConditionAdapter<T> condition = filters.get(filterKey);
        if (condition == null) {
            throw new IllegalArgumentException("No condition found for key: " + filterKey);
        }
        return condition;
    }
}