package io.github.cyfko.filterql.core.mappings;

/**
 * Marker interface representing a mapping between a logical property reference
 * (usually an enum constant) and its representation in the target entity of type {@code E}
 *
 * <p>A logical reference is a stable key defined in an enum, pointing to
 * either a direct or nested property of an entity. This mapping describes
 * how that reference translates technically in JPA (path navigation, Specification, etc.).</p>
 *
 * <p>Sub-interfaces like {@link PathMapping} or {@link SpecificationMapping} provide
 * concrete strategies for that translation.</p>
 *
 * @param <E> the entity type this mapping applies to
 */
public interface ReferenceMapping<E> {
}
