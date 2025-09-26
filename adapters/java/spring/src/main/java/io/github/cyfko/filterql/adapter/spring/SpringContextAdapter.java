package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Data JPA Context Adapter implementing the correct pattern.
 * This adapter uses SpringConditionAdapterBuilder to create conditions.
 * 
 * @param <T> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public class SpringContextAdapter<T, P extends Enum<P> & PropertyRef & PathShape> implements Context {
    private final Map<String, SpringConditionAdapter<T>> filters;
    private final SpringConditionAdapterBuilder<T, P> conditionAdapterBuilder;

    /**
     * Constructs a new Spring context adapter.
     *
     * @param conditionAdapterBuilder The condition adapter builder for this entity type
     */
    public SpringContextAdapter(SpringConditionAdapterBuilder<T, P> conditionAdapterBuilder) {
        this.filters = new HashMap<>();
        this.conditionAdapterBuilder = conditionAdapterBuilder;
    }

    /**
     * Adds a condition to the context.
     *
     * @param filterKey The key to identify this condition
     * @param definition The filter definition containing property, operator, and value
     * @throws IllegalArgumentException if the operator is not supported for the property
     */
    public void addCondition(String filterKey, FilterDefinition<P> definition) {
        P propertyRef = definition.getRef();
        var operator = definition.getOperator();

        // Validate that the property supports this operator
        propertyRef.validateOperator(operator);

        // Build condition using the builder and store it
        SpringConditionAdapter<T> condition = conditionAdapterBuilder.build(propertyRef, operator, definition.getValue());
        filters.put(filterKey, condition);
    }

    /**
     * {@inheritDoc}
     *
     * @param filterKey The key to identify the condition
     * @return The condition for the given key
     */
    @Override
    public Condition getCondition(String filterKey) {
        return filters.get(filterKey);
    }

    /**
     * Gets the Spring JPA specification for the given filter key.
     *
     * @param filterKey The key to identify the condition
     * @return The Spring JPA specification for this condition
     */
    public Specification<T> getSpecification(String filterKey) {
        SpringConditionAdapter<T> condition = filters.get(filterKey);
        return condition != null ? condition.getSpecification() : null;
    }
}


