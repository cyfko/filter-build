package io.github.cyfko.filterql.adapter.spring.utils;

import io.github.cyfko.filterql.core.utils.ClassUtils;
import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;

import java.lang.reflect.*;
import java.util.*;

/**
 * Utility providing a static method to resolve a property path from a JPA entity root for building dynamic query criteria.
 * <p>
 * This class facilitates navigation through nested entities by splitting a dot notation path (e.g., "fieldA.fieldB.listField.fieldC")
 * into successive joins in a Criteria query.
 * </p>
 *
 * <p>It also handles collections by joining entity relationships and dynamically determining the generic types of collections.</p>
 *
 * @author Frank KOSSI
 * @since 1.0
 *
 * <p><b>Note:</b> This resolution is designed for use in dynamic queries built with the Criteria API (javax.persistence.criteria).</p>
 *
 * <h2>Usage example:</h2>
 * <pre>{@code
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery<MyEntity> query = cb.createQuery(MyEntity.class);
 * Root<MyEntity> root = query.from(MyEntity.class);
 *
 * Path&lt;?&gt; path = PathResolver.resolvePath(root, "orders.items.product.name", MyEntity.class);
 * Predicate predicate = cb.equal(path, "Book");
 * query.where(predicate);
 * }</pre>
 */
public class PathResolverUtils {

    /**
     * Utility class constructor.
     */
    private PathResolverUtils() {
        // Utility class
    }

    /**
     * Resolves the given path into a JPA {@link Path} from the provided entity root.
     * <p>
     * The path is split into segments, each representing a property of the entity or its relationships.
     * If a segment corresponds to a collection, a left join is performed and resolution continues
     * in the generic type of the collection.
     * </p>
     * <p>
     * In case of error (field not found, unable to determine generic type), an exception is thrown.
     * </p>
     *
     * @param <T> the type of the root entity
     * @param root the root of the JPA Criteria query (Root of the base entity)
     * @param path the full property path in dot notation (e.g., "fieldA.fieldB.listField.fieldC")
    * @return a {@link Path} instance corresponding to the resolved path
    * @throws IllegalArgumentException if path is {@code null} or {@code empty} or if a segment does not match any field in the class
    * @throws IllegalStateException    if the generic type of a collection cannot be determined
     */
    public static <T> Path<?> resolvePath(Root<T> root, String path) {
        if (root == null || path == null) throw new IllegalArgumentException("path cannot be null or empty");

        String[] parts = path.split("\\.");
        From<?, ?> current = root;
        Class<?> currentClass = root.getJavaType();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            final Class<?> finalCurrentClass = currentClass;
            Field field = ClassUtils.getAnyField(currentClass, part)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Field not found: %s in %s", part, finalCurrentClass)));

            boolean isCollection = Collection.class.isAssignableFrom(field.getType());

            if (i < parts.length - 1) {
                if (isCollection) {
                    current = current.join(part, JoinType.LEFT);
                    currentClass = (Class<?>) ClassUtils.getCollectionGenericType(field,0)
                            .orElseThrow(() -> new IllegalStateException("Unable to determine the parameter type of the joined collection: " + part));
                } else {
                    try {
                        current = current.join(part, JoinType.LEFT);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("%s is not a property name of %s", part, currentClass),e);
                    }
                    currentClass = field.getType();
                }
            } else {
                // Dernier segment du chemin : récupérer la propriété en tant que Path
                return current.get(part);
            }
        }
        throw new IllegalArgumentException("Invalid path: " + path);
    }

}

