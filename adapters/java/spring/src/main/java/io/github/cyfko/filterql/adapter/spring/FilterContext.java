package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.domain.Specification;
import io.github.cyfko.filterql.core.mappings.SpecificationMapping;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.utils.ClassUtils;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Spring Data JPA Context Adapter implementing the correct pattern.
 * This adapter uses SpringConditionAdapterBuilder to create conditions.
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterContext<E,P extends Enum<P> & PropertyReference> implements Context {
    private final Class<E> entityClass;
    private final Map<String, ConditionAdapter<?>> filters;
    private final Map<String, Function<?, Object>> conditionBuilders;

    /**
     * Constructs a new Spring context adapter.
     *
     * @param entityClass The class of the entity this context belongs to
     */
    public FilterContext(Class<E> entityClass) {
        this.entityClass = entityClass;
        this.filters = new HashMap<>();
        this.conditionBuilders = new HashMap<>();
    }

    // Si l'objet retourné par la fonction mapping est un String non vide alors il est utilisé comme non de propriété
    // Si c'est une instance de SpecificationMapping<E> alors la spécification est directement utilisée
    // Si c'est autre chose alors une exception IllegalStateException est levée
    public FilterContext<E,P> addBuilder(Function<P, Object> mapping) {
        Class<P> propClass = ClassUtils.getClazz();
        conditionBuilders.put(propClass.getName(), mapping);
        return this;
    }

    public Function<P, Object> getBuilder(Class<P> propClass) {
        return (Function<P, Object>) conditionBuilders.get(propClass.getName());
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Adds a condition to the context.
     *
     * @param filterKey The key to identify this condition
     * @param definition The filter definition containing property, operator, and value
     * @throws IllegalArgumentException if the operator is not supported for the property
     */
    @Override
    public void addCondition(String filterKey, FilterDefinition<P> definition) {
        // Build condition using the builder and store it
        String key = ClassUtils.<P>getClazz().getName();
        Function<P, Object> mappingFunction = (Function<P, Object>) conditionBuilders.get(key);

        Object mapping = mappingFunction.apply(definition.getRef());

        if (mapping instanceof SpecificationMapping<?,?>) {
            Specification<E> spec = ((SpecificationMapping<E,P>) mapping).toSpecification(definition);
            filters.put(filterKey, new ConditionAdapter<>(spec::toPredicate));
            return;
        }

        if (mapping instanceof String pathName) {
            Specification<E> spec = getSpecificationFromPath(pathName, definition);
            filters.put(filterKey, new ConditionAdapter<>(spec::toPredicate));
            return;
        }

        throw new IllegalArgumentException("Invalid mapping function unsupported yet");
    }

    @Override
    public Condition getCondition(String filterKey) {
        ConditionAdapter<?> condition = filters.get(filterKey);
        if (condition == null) {
            throw new IllegalArgumentException(filterKey + " not found");
        }
        return condition;
    }

    @Override
    public <U> Specification<U> toSpecification(Class<U> entityClass, Condition condition) {
        org.springframework.data.jpa.domain.Specification<U> specification = ((ConditionAdapter<U>) condition).getSpecification();
        return specification::toPredicate;
    }


    /**
     * Utility method to build a Spring Data JPA specification
     * from a {@code PathMapping}-based property reference.
     *
     * @param pathName name of the property path
     * @param definition FilterDefinition on which to operate
     * @param <E>   the entity type
     * @param <P>   the property reference type
     * @return a specification for use in JPA criteria queries
     */
    private static <E, P extends Enum<P> & PropertyReference> Specification<E> getSpecificationFromPath(String pathName, FilterDefinition<P> definition) {
        return (Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            P ref = definition.getRef();
            Op operator = definition.getOperator();
            Object value = definition.getValue();

            // Ensure value type compatibility for the given operator
            ref.validateOperatorForValue(operator, value);

            // Resolve criteria path from path mapping
            Path<?> path = PathResolverUtils.resolvePath(root, pathName);

            // Switch on supported operators to construct a predicate
            return switch (operator) {
                case EQUALS -> cb.equal(path, value);
                case NOT_EQUALS -> cb.notEqual(path, value);
                case GREATER_THAN -> cb.gt((Path<Number>) path, (Number) value);
                case GREATER_THAN_OR_EQUAL -> cb.ge((Path<Number>) path, (Number) value);
                case LESS_THAN -> cb.lt((Path<Number>) path, (Number) value);
                case LESS_THAN_OR_EQUAL -> cb.le((Path<Number>) path, (Number) value);
                case LIKE -> cb.like((Path<String>) path, (String) value);
                case NOT_LIKE -> cb.notLike((Path<String>) path, (String) value);
                case IN -> path.in((Collection<?>) value);
                case NOT_IN -> cb.not(path.in((Collection<?>) value));
                case IS_NULL -> cb.isNull(path);
                case IS_NOT_NULL -> cb.isNotNull(path);
                case BETWEEN -> {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    yield cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]);
                }
                case NOT_BETWEEN -> {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    yield cb.not(cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]));
                }
            };
        };
    }
}




