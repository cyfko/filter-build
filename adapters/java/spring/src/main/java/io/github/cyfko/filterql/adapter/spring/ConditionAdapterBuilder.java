package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.mappings.PathMapping;
import io.github.cyfko.filterql.adapter.spring.mappings.SpecificationMapping;
import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * Builder interface for creating Spring condition adapters.
 * <p>
 * Implementations define how to build condition adapters from a property reference,
 * operator, and value, mapping them into Spring Data JPA {@link Specification}s.
 * This pattern supports flexible condition creation based on the structure and mapping
 * capabilities defined by the property reference instance.
 * </p>
 *
 * @param <E> the entity type (e.g., User, Product)
 * @param <P> the enum type representing filterable properties, which must extend {@link Enum}, {@link PropertyRef}, and {@link ReferenceMapping}
 */
public interface ConditionAdapterBuilder<E, P extends Enum<P> & PropertyRef & ReferenceMapping<E>> {

    /**
     * Builds a Spring condition adapter from the given parameters.
     * <p>
     * This default implementation delegates construction to either a
     * {@code SpecificationMapping} or a {@code PathMapping} interface,
     * based on the property reference type.
     * </p>
     *
     * @param ref   the property reference (type-safe)
     * @param op    the operator determining filter logic
     * @param value the value to be compared or used for filtering
     * @return a Spring condition adapter wrapping a JPA specification predicate
     * @throws UnsupportedOperationException if the provided property reference is not supported
     */
    default ConditionAdapter<E> build(@NonNull P ref, @NonNull Operator op, Object value) {

        if (ref instanceof SpecificationMapping) {
            //noinspection unchecked
            Specification<E> specification = ((SpecificationMapping<E>) ref).toSpecification(ref, op, value);
            return new ConditionAdapter<>(specification);
        }

        if (ref instanceof PathMapping) {
            Specification<E> specification = getSpecificationFromPath(ref, op, value);
            return new ConditionAdapter<>(specification);
        }

        throw new UnsupportedOperationException(String.format("Unsupported specification shape %s", ref.getClass()));
    }

    /**
     * Utility method to build a Spring Data JPA specification
     * from a {@code PathMapping}-based property reference.
     *
     * @param ref   the property reference providing path mapping
     * @param op    the operator used for the query predicate
     * @param value the filter value passed to the predicate
     * @param <E>   the entity type
     * @param <P>   the property reference type
     * @return a specification for use in JPA criteria queries
     */
    private static <E, P extends Enum<P> & PropertyRef & ReferenceMapping> Specification<E> getSpecificationFromPath(P ref, Operator op, Object value) {
        return (Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // Ensure value type compatibility for the given operator
            ref.validateOperatorForValue(op, value);

            // Resolve criteria path from path mapping
            String pathName = ((PathMapping) ref).getPath();
            Path<?> path = PathResolverUtils.resolvePath(root, pathName);

            // Switch on supported operators to construct a predicate
            return switch (op) {
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