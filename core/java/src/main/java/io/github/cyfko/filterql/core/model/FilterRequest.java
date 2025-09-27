package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.PropertyRef;
import java.util.Map;

/**
 * Represents a complete filter request containing multiple filter definitions
 * and a DSL expression for their combination.
 * <p>
 * Each filter definition associates a property, an operator, and a value,
 * while the DSL expression {@code combineWith} defines how these filters are logically combined
 * (for example, using AND, OR operators).
 * </p>
 *
 * @param <P> type of the reference property, an enum implementing {@link PropertyRef}
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterRequest<P extends Enum<P> & PropertyRef> {

    private final Map<String, FilterDefinition<P>> filters;
    private final String combineWith;

    /**
     * Constructs a new filter request.
     *
     * @param filters     a map of filter definitions identified by their keys
     * @param combineWith a DSL expression combining the filters
     */
    public FilterRequest(Map<String, FilterDefinition<P>> filters, String combineWith) {
        this.filters = filters;
        this.combineWith = combineWith;
    }

    /**
     * Returns the map of filter definitions.
     *
     * @return the immutable or mutable map of filters
     */
    public Map<String, FilterDefinition<P>> getFilters() {
        return filters;
    }

    /**
     * Returns the DSL expression defining the logical combination of filters.
     *
     * @return the combination DSL expression (example: "(f1 & f2) | f3")
     */
    public String getCombineWith() {
        return combineWith;
    }

    @Override
    public String toString() {
        return String.format("FilterRequest{filters=%s, combineWith='%s'}", filters, combineWith);
    }
}

