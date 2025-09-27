package io.github.cyfko.filterql.adapter.spring;


/**
 * Interface representing access to a path of a nested property in a data structure.
 * <p>
 * The path uses the classic syntax for nested properties, similar to that used
 * by Spring to define predicates on fields in specifications.
 * </p>
 *
 * <p>This type is useful for describing and dynamically manipulating field access paths in
 * complex objects or entity graphs.</p>
 *
 * @author Frank KOSSI
 * @since 1.0
 *
 * @return the full path as a string, for example: "fieldA.fieldB.fieldC"
 */
public interface PathShape {

    /**
     * Returns the full access path of the field in the data structure.
     * <p>
     * The path describes navigation in the object, segmented by dots,
     * allowing precise targeting of a nested property.
     * </p>
     *
     * @return the path as a string (example: "fieldA.fieldB.fieldC")
     */
    String getPath();
}





