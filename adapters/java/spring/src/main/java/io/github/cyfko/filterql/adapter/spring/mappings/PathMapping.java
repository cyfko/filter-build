package io.github.cyfko.filterql.adapter.spring.mappings;


import io.github.cyfko.filterql.adapter.spring.ReferenceMapping;

/**
 * Represents the mapping between a logical reference (enum constant)
 * and the navigable property path in a JPA entity of type {@code E}.
 *
 * <p>This is useful when properties are nested (embedded objects or relations),
 * allowing the filter system to target a specific field in the entity graph.</p>
 *
 * <p>Example:
 * <ul>
 *   <li>Enum: CITY_NAME</li>
 *   <li>Path: "address.city.name"</li>
 * </ul>
 * The enum constant serves as the logical key, and its implementation
 * of {@code PathMapping} indicates how to reach the property technically.</p>
 *
 * @param <E> the entity type this path mapping applies to
 */
public interface PathMapping<E> extends ReferenceMapping<E> {

    /**
     * Returns the full navigation path to the property in the entity.
     *
     * @return the dot-separated path string (e.g., "address.city.name")
     */
    String getPath();
}






