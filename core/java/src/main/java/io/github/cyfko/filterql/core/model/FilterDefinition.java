package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

/**
 * Représente une définition de filtre unique avec une référence de propriété,
 * un opérateur, et une valeur.
 * <p>
 * La propriété de référence {@code ref} doit être un enum qui implémente {@link PropertyRef}
 * afin d'assurer la sécurité des types et la performance.
 * </p>
 *
 * @param <P> type de la propriété de référence (enum implémentant {@link PropertyRef})
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterDefinition<P extends Enum<P> & PropertyRef> {

    private final P ref;
    private final Operator operator;
    private final Object value;

    /**
     * Crée une définition de filtre avec la propriété, l'opérateur et la valeur donnés.
     *
     * @param ref       la référence de propriété (enum {@link PropertyRef})
     * @param operator  l'opérateur de comparaison ou logique
     * @param value     la valeur à utiliser dans le filtre (peut être une collection selon l'opérateur)
     */
    public FilterDefinition(P ref, Operator operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Retourne la référence de propriété utilisée dans ce filtre.
     *
     * @return la propriété de référence
     */
    public P getRef() {
        return ref;
    }

    /**
     * Retourne l'opérateur associé à cette définition de filtre.
     *
     * @return l'opérateur
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Retourne la valeur appliquée dans ce filtre.
     * <p>
     * Cette valeur peut être un objet simple ou une collection, dépendant de l'opérateur.
     * </p>
     *
     * @return la valeur du filtre
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("FilterDefinition{ref=%s, operator=%s, value=%s}",
                ref, operator, value);
    }
}

