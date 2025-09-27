package io.github.cyfko.filterql.adapter.jpa.impl;

import io.github.cyfko.filterql.adapter.jpa.Specification;
import io.github.cyfko.filterql.adapter.jpa.SpecificationBuilder;
import io.github.cyfko.filterql.adapter.jpa.annotations.NotNull;
import io.github.cyfko.filterql.adapter.jpa.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of {@link SpecificationBuilder} that constructs JPA specifications
 * based on a property path name generator derived from an enum property.
 * <p>
 * This class uses a {@link Function} to convert the property enum {@code P}
 * into a string path used in JPA Criteria queries.
 * </p>
 * <p>
 * Each filter is translated to a predicate according to the provided operator.
 * </p>
 *
 * @param <E> the entity type on which the specifications operate.
 * @param <P> the enum type of filterable properties (implementing {@link PropertyRef}).
 *
 * @author Frank
 * @since 1.0
 */
public class PathNameSpecificationBuilder<E, P extends Enum<P> & PropertyRef> implements SpecificationBuilder<E, P> {

    private final Function<P, String> pathNameSupplier;

    /**
     * Constructs a specification builder with a property path name supplier function.
     * <p>
     * The function converts a property enum value into a dot notation path string
     * (e.g., "fieldA.fieldB.listField.fieldC"). This facilitates navigation through
     * nested entities by successive joins in a Criteria query.
     * </p>
     *
     * @param pathNameSupplier function mapping a property enum to a string path
     * @throws NullPointerException if {@code pathNameGenerator} is null
     */
    public PathNameSpecificationBuilder(@NotNull Function<P, String> pathNameSupplier) {
        this.pathNameSupplier = Objects.requireNonNull(pathNameSupplier, "pathNameGenerator must not be null");
    }

    /**
     * Builds a JPA specification predicate for the given property reference, operator, and filter value.
     * <p>
     * Resolves the path from the root entity using the path name generated from the property enum.
     * Applies the appropriate CriteriaBuilder predicate according to the operator.
     * </p>
     *
     * @param ref   the enum property reference to filter on
     * @param op    the filter operator
     * @param value the filter value, compatible with the property and operator
     * @return a JPA Specification predicate representing the filter
     * @throws IllegalArgumentException if the operator is unsupported or path name is invalid
     * @throws NullPointerException if {@code ref} or {@code op} are null
     */
    @Override
    public final Specification<E> build(@NotNull P ref, @NotNull Operator op, Object value) {
        return (Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // Retrieve the relative path name for the root entity
            String pathName = pathNameSupplier.apply(ref);
            if (pathName == null || pathName.isEmpty()) {
                throw new IllegalArgumentException(String.format("Generated pathName for property reference <%s> must not be null or empty", ref));
            }

            // Resolve the Path object within the entity graph
            Path<?> path = PathResolverUtils.resolvePath(root, pathName);

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
                case NOT_BETWEEN: {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    return cb.not(cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]));
                }
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + op);
            }
        };
    }
}