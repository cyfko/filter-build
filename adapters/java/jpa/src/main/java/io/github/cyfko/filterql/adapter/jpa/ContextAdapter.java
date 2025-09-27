package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA implementation of the {@link Context} interface.
 * <p>
 * This adapter uses a {@link SpecificationBuilder} to create conditions for filtering entities.
 * Each filter condition is stored and can be retrieved by its key.
 * </p>
 *
 * @param <E> The entity type (e.g., User, Product)
 * @param <P> The enum type representing filterable property references (must implement {@link PropertyRef})
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class ContextAdapter<E, P extends Enum<P> & PropertyRef> implements Context {

    private final Map<String, ConditionAdapter<E>> filters;
    private final SpecificationBuilder<E, P> specificationBuilder;

    /**
     * Constructs a new JPA context adapter.
     *
     * @param specificationBuilder The specification builder to be used for this entity type.
     */
    public ContextAdapter(SpecificationBuilder<E, P> specificationBuilder) {
        this.filters = new HashMap<>();
        this.specificationBuilder = specificationBuilder;
    }

    /**
     * Adds a condition to this context under the given filter key.
     * <p>
     * This method validates the operator compatibility with the given property and value,
     * then builds a condition and stores it for later retrieval.
     * </p>
     *
     * @param filterKey  The key to identify this condition.
     * @param definition The filter definition holding property enum, operator, and value.
     * @throws IllegalArgumentException if the operator is not supported for the property,
     *                                  or if it's not applicable in the context of input data.
     */
    public void addCondition(String filterKey, FilterDefinition<P> definition) {
        P propertyRef = definition.getRef();
        Operator operator = definition.getOperator();
        Object value = definition.getValue();

        // Validate property supports this operator with given value
        propertyRef.validateOperatorForValue(operator, value);

        // Build the condition and store in map
        ConditionAdapter<E> condition = new ConditionAdapter<>(specificationBuilder.build(propertyRef, operator, value));
        filters.put(filterKey, condition);
    }

    /**
     * Returns the condition associated with the given filter key.
     *
     * @param filterKey The key identifying the requested condition.
     * @return The condition corresponding to the specified key.
     * @throws IllegalArgumentException if no condition is found for the provided key.
     */
    @Override
    public Condition getCondition(String filterKey) {
        ConditionAdapter<E> condition = filters.get(filterKey);
        if (condition == null) {
            throw new IllegalArgumentException("No condition found for key: " + filterKey);
        }
        return condition;
    }
}