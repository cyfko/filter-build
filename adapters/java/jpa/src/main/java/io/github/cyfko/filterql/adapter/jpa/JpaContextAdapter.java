package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
    
    /**
     * Constructs a new JPA context adapter.
     * 
     * @param entityClass The entity class to query
     * @param entityManager The JPA entity manager
     * @param specificationBuilder The specification builder for this entity type
     */
    public JpaContextAdapter(Class<T> entityClass, EntityManager entityManager, SpecificationBuilder<T, P> specificationBuilder) {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(entityClass);
        this.root = query.from(entityClass);
        this.filters = new HashMap<>();
        this.specificationBuilder = specificationBuilder;
    }
    
    /**
     * Adds a condition to the context.
     * 
     * @param filterKey The key to identify this condition
     * @param definition The filter definition containing property, operator, and value
     * @throws IllegalArgumentException if the operator is not supported for the property
     */
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
    
    /**
     * {@inheritDoc}
     * 
     * @param filterKey The key to identify the condition
     * @return The condition for the given key
     * @throws IllegalArgumentException if no condition is found for the key
     */
    @Override
    public Condition getCondition(String filterKey) {
        JpaConditionAdapter<T> condition = filters.get(filterKey);
        if (condition == null) {
            throw new IllegalArgumentException("No condition found for key: " + filterKey);
        }
        return condition;
    }

    /**
     * Gets the JPA criteria query.
     * 
     * @return The criteria query for this context
     */
    public CriteriaQuery<T> getQuery() {
        return query;
    }

}