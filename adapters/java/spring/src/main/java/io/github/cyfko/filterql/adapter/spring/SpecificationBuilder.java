package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.impl.DSLParser;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builder for Spring Data JPA {@link Specification} from a filter request
 * containing a DSL expression and multiple filter definitions.
 * <p>
 * This class translates the DSL expression into a filter tree ({@link FilterTree})
 * using {@link DSLParser}, fills a Spring-adapted condition context,
 * and then generates the resulting {@link Specification}.
 * </p>
 *
 * <p>This approach allows dynamic construction of complex queries with a readable and extensible DSL syntax.</p>
 *
 * @author Frank KOSSI
 * @since 1.0
 *
 * @see DSLParser
 * @see FilterTree
 * @see Specification
 */
/**
 * Builds Spring Data JPA Specifications from filter requests.
 */
public class SpecificationBuilder {

    /**
     * Builds a JPA {@link Specification} from a filter request.
     *
     * @param filterRequest the request containing filter definitions and the DSL combination logic
     * @param <T>           entity type for the Specification
     * @param <P>           enum type of supported properties (implementing {@link PropertyRef} and {@link PathShape})
     * @return a {@link Specification} representing the filter request
     * @throws DSLSyntaxException if the DSL expression has invalid syntax
     * @throws FilterValidationException if the DSL expression has invalid syntax
     */
    public static <T, P extends Enum<P> & PropertyRef & PathShape> Specification<T> toSpecification(FilterRequest<P> filterRequest) throws DSLSyntaxException, FilterValidationException {
        // Parse the filter DSL
        DSLParser dslParser = new DSLParser();
        FilterTree filterTree = dslParser.parse(filterRequest.getCombineWith());

        // Fill the Context
        ContextAdapter<T, P> context = new ContextAdapter<>(new ConditionAdapterBuilder<T, P>() {});
        filterRequest.getFilters().forEach(context::addCondition);

        // Return Spring boot specification from the global Condition
        Condition condition = filterTree.generate(context);
        return ((ConditionAdapter<T>) condition).getSpecification();
    }
}


