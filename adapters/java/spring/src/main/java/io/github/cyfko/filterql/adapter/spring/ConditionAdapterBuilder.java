package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * Builder interface for creating Spring condition adapters.
 * Each implementation defines how to build a Spring condition from PropertyRef, Operator, and value.
 * 
 * @param <T> The entity type (e.g., User, Product)
 * @param <P> The PropertyRef enum for this entity
 */
public interface ConditionAdapterBuilder<T, P extends Enum<P> & PropertyRef & PathShape> {

    /**
     * Builds a Spring condition adapter from the given parameters.
     *
     * @param ref   The property reference (type-safe)
     * @param op    The operator
     * @param value The value as object
     * @return A Spring condition adapter
     */
    default ConditionAdapter<T> build(@NonNull P ref, @NonNull Operator op, Object value) {
        Specification<T> specification = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // Ensure value is of expected type for the given operator
            ref.validateOperatorForValue(op, value);

            // Ensure FieldShape match
            Path<?> path = PathResolverUtils.resolvePath(root, ref.getPath());

            switch (op) {
                case EQUALS:
                    return cb.equal(path, value);
                case NOT_EQUALS:
                    return cb.notEqual(path, value);
                case GREATER_THAN:
                    return cb.gt((Path<Number>) path, (Number) value);
                case GREATER_THAN_OR_EQUAL:
                    return cb.ge((Path<Number>) path, (Number) value);
                case LESS_THAN:
                    return cb.lt((Path<Number>) path, (Number) value);
                case LESS_THAN_OR_EQUAL:
                    return cb.le((Path<Number>) path, (Number) value);
                case LIKE:
                    return cb.like((Path<String>) path, (String) value);
                case NOT_LIKE:
                    return cb.notLike((Path<String>) path, (String) value);
                case IN:
                    return path.in((Collection<?>) value);
                case NOT_IN:
                    return cb.not(path.in((Collection<?>) value));
                case IS_NULL:
                    return cb.isNull(path);
                case IS_NOT_NULL:
                    return cb.isNotNull(path);
                case BETWEEN: {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    return cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]);
                }
                case NOT_BETWEEN:{
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    return cb.not(cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]));
                }
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + op);
            }
        };
        
        return new ConditionAdapter<>(specification);
    }

}
