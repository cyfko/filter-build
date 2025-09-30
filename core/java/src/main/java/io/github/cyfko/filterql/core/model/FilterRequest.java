package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.PropertyReference;

import java.util.HashMap;
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
 * @param <P> type of the reference property, an enum implementing {@link PropertyReference}
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterRequest<P extends Enum<P> & PropertyReference> {

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
     * @return the combination DSL expression (example: "(f1 &amp; f2) | f3")
     */
    public String getCombineWith() {
        return combineWith;
    }

    @Override
    public String toString() {
        return String.format("FilterRequest{filters=%s, combineWith='%s'}", filters, combineWith);
    }

    /**
     * Static factory method providing a new instance of the {@link Builder}.
     * <p>
     * This is the recommended entry point to obtain a {@code Builder} instance
     * for constructing {@link FilterRequest} objects with a fluent API.
     * </p>
     *
     * @param <R> the enum type representing filterable properties, which must extend {@code Enum} and implement {@link PropertyReference}
     * @return a new builder instance
     */
    public static <R extends Enum<R> & PropertyReference> Builder<R> builder() {
        return new Builder<>();
    }

    /**
     * Builder for {@link FilterRequest} instances.
     * <p>
     * Provides a fluent API to progressively add filter definitions and set the
     * logical operator used to combine those filters.
     * </p>
     *
     * @param <P> the enum type representing filterable properties,
     *            which must extend {@code Enum} and implement {@link PropertyReference}
     */
    public static class Builder<P extends Enum<P> & PropertyReference> {

        private final Map<String, FilterDefinition<P>> filters = new HashMap<>();
        private String combineWith;

        /**
         * Adds a single filter definition to the builder.
         *
         * @param property   the key identifying the property to filter on
         * @param definition the filter definition containing property, operator, and value
         * @return this builder instance for chaining
         */
        public Builder<P> filter(String property, FilterDefinition<P> definition) {
            this.filters.put(property, definition);
            return this;
        }

        /**
         * Adds multiple filter definitions to the builder.
         *
         * @param filters a map of property keys to filter definitions; if null, ignored
         * @return this builder instance for chaining
         */
        public Builder<P> filters(Map<String, FilterDefinition<P>> filters) {
            if (filters != null) {
                this.filters.putAll(filters);
            }
            return this;
        }

        /**
         * Sets the logical expression string used to combine filters,
         * such as "AND" or "OR".
         *
         * @param expression the filter combination expression
         * @return this builder instance for chaining
         */
        public Builder<P> combineWith(String expression) {
            this.combineWith = expression;
            return this;
        }

        /**
         * Builds a new immutable {@link FilterRequest} instance with the
         * accumulated filters and combination expression.
         *
         * @return a new {@link FilterRequest} instance
         */
        public FilterRequest<P> build() {
            return new FilterRequest<>(filters, combineWith);
        }
    }

}

