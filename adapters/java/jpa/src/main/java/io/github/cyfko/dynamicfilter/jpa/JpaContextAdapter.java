package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.Context;
import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA implementation of the Context interface.
 * This adapter uses JpaConditionAdapterBuilder to create conditions.
 */
public class JpaContextAdapter<T> implements Context {
    
    private final Map<String, JpaConditionAdapter<T>> filters;
    private final JpaConditionAdapterBuilder<T> conditionAdapterBuilder;
    
    public JpaContextAdapter(JpaConditionAdapterBuilder<T> conditionAdapterBuilder) {
        this.filters = new HashMap<>();
        this.conditionAdapterBuilder = conditionAdapterBuilder;
    }
    
    public void addCondition(String filterKey, FilterDefinition definition) {
        // Resolve String ref to PropertyRef enum
        PropertyRef propertyRef = resolvePropertyRef(definition.getRef());
        if (propertyRef == null) {
            throw new IllegalArgumentException("Property not found: " + definition.getRef());
        }
        
        // Validate operator
        Operator operator = Operator.fromString(definition.getOperator());
        if (operator == null) {
            throw new IllegalArgumentException("Invalid operator: " + definition.getOperator());
        }
        
        // Validate that the property supports this operator
        propertyRef.validateOperator(operator);
        
        // Build condition using the builder and store it
        JpaConditionAdapter<T> condition = conditionAdapterBuilder.build(propertyRef, operator, definition.getValue());
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
    
    /**
     * Resolves a String ref to the appropriate PropertyRef enum.
     * Each adapter can implement its own resolution logic.
     * This method should be overridden by concrete implementations to provide
     * the actual PropertyRef resolution strategy.
     */
    private PropertyRef resolvePropertyRef(String ref) {
        // This is a placeholder implementation.
        // Concrete adapters should override this method to implement
        // their own PropertyRef resolution strategy.
        throw new UnsupportedOperationException(
            "PropertyRef resolution must be implemented by concrete adapter implementations");
    }
}