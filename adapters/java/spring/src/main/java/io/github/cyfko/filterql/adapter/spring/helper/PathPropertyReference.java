package io.github.cyfko.filterql.adapter.spring.helper;

import io.github.cyfko.filterql.adapter.spring.mappings.PathMapping;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.Objects;
import java.util.Set;

/**
 * Implementation of {@link PropertyRef} and {@link PathMapping} describing a property reference with a path and type information.
 * <p>
 * This class represents a property reference in an entity graph, accessible via a path expression.
 * It also exposes supported operators for filtering and the Java type of the property.
 * </p>
 *
 * @author Frank
 * @since 1.0
 */
public class PathPropertyReference implements PropertyRef, PathMapping {

    private final String path;
    private final Class<?> clazz;
    private final Set<Operator> supportedOperators;

    /**
     * Constructs a new {@code PathPropertyReference} with the given path, type, and supported operators.
     *
     * @param path              the path expression to access the property (dot notation supported)
     * @param clazz             the Java class type of the property
     * @param supportedOperators the set of supported {@link Operator}s for this property
     */
    protected PathPropertyReference(String path, Class<?> clazz, Set<Operator> supportedOperators) {
        this.path = Objects.requireNonNull(path);
        this.clazz = Objects.requireNonNull(clazz);
        this.supportedOperators = Objects.requireNonNull(supportedOperators);
    }

    /**
     * Returns the Java class type of this property.
     *
     * @return the {@link Class} representing the property type
     */
    @Override
    public Class<?> getType() {
        return clazz;
    }

    /**
     * Returns the set of supported filter operators for this property.
     *
     * @return a {@link Set} of supported {@link Operator}s
     */
    @Override
    public Set<Operator> getSupportedOperators() {
        return supportedOperators;
    }

    /**
     * Returns the property path expression used to access this property from an entity root.
     *
     * @return the property path, typically in dot notation
     */
    @Override
    public String getPath() {
        return path;
    }
}
