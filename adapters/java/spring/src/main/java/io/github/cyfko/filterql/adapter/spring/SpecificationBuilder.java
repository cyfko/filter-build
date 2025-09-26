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
 * Constructeur de {@link Specification} Spring Data JPA à partir d'une requête de filtrage
 * comportant une expression DSL et plusieurs définitions de filtres.
 * <p>
 * Cette classe traduit l'expression DSL en arbre de filtres ({@link FilterTree})
 * en utilisant {@link DSLParser}, remplit un contexte de conditions adapté à Spring,
 * puis génère la {@link Specification} résultante.
 * </p>
 *
 * <p>Cette approche permet de construire dynamiquement des requêtes complexes avec une syntaxe DSL lisible et extensible.</p>
 *
 * @throws DSLSyntaxException en cas d'erreur de parsing ou de syntaxe dans l'expression DSL
 *
 * @see DSLParser
 * @see FilterTree
 * @see Specification
 */
public class SpecificationBuilder {

    /**
     * Construit une {@link Specification} JPA à partir d'une requête de filtres.
     *
     * @param filterRequest la requête contenant les définitions de filtres et la logique combinatoire DSL
     * @param <T>           type d'entité sur lequel porte la Specification
     * @param <P>           type d'enum des propriétés supportées (implémentant {@link PropertyRef} et {@link PathShape})
     * @return une {@link Specification} traduisant la requête de filtrage
     * @throws DSLSyntaxException en cas de syntaxe invalide dans l'expression DSL
     */
    public static <T, P extends Enum<P> & PropertyRef & PathShape> Specification<T> toSpecification(FilterRequest<P> filterRequest) throws DSLSyntaxException, FilterValidationException {
        // Parse the filter DSL
        DSLParser dslParser = new DSLParser();
        FilterTree filterTree = dslParser.parse(filterRequest.getCombineWith());

        // Fill the Context
        SpringContextAdapter<T, P> context = new SpringContextAdapter<>(new SpringConditionAdapterBuilder<T, P>() {});
        filterRequest.getFilters().forEach(context::addCondition);

        // Return Spring boot specification from the global Condition
        Condition condition = filterTree.generate(context);
        return ((SpringConditionAdapter<T>) condition).getSpecification();
    }
}


