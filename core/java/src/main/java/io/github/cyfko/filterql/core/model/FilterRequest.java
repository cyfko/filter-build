package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.PropertyRef;
import java.util.Map;

/**
 * Représente une requête de filtrage complète contenant plusieurs définitions de filtres
 * et une expression DSL pour leur combinaison.
 * <p>
 * Chaque définition de filtre associe une propriété, un opérateur, et une valeur,
 * tandis que l'expression DSL {@code combineWith} définit la manière dont ces filtres sont combinés
 * logiquement (par exemple, à l'aide d'opérateurs AND, OR).
 * </p>
 *
 * @param <P> type de la propriété de référence, un enum implémentant {@link PropertyRef}
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterRequest<P extends Enum<P> & PropertyRef> {

    private final Map<String, FilterDefinition<P>> filters;
    private final String combineWith;

    /**
     * Construit une nouvelle requête de filtrage.
     *
     * @param filters     une map des définitions de filtres identifiées par leurs clés
     * @param combineWith une expression DSL combinant les filtres
     */
    public FilterRequest(Map<String, FilterDefinition<P>> filters, String combineWith) {
        this.filters = filters;
        this.combineWith = combineWith;
    }

    /**
     * Retourne la map des définitions de filtres.
     *
     * @return la map immuable ou mutable des filtres
     */
    public Map<String, FilterDefinition<P>> getFilters() {
        return filters;
    }

    /**
     * Retourne l'expression DSL définissant la combinaison logique des filtres.
     *
     * @return l'expression DSL de combinaison (exemple : "(f1 &amp; f2) | f3")
     */
    public String getCombineWith() {
        return combineWith;
    }

    @Override
    public String toString() {
        return String.format("FilterRequest{filters=%s, combineWith='%s'}", filters, combineWith);
    }
}

