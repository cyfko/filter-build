package io.github.cyfko.filterql.core.utils;

import io.github.cyfko.filterql.core.validation.Operator;

import java.util.Set;

/**
 * Utilitaires liés aux opérateurs de filtre, notamment des ensembles prédéfinis
 * d'opérateurs adaptés à différents types de données.
 * <p>
 * Cette classe fournit par exemple des collections immuables d'opérateurs
 * applicables aux valeurs textuelles ou numériques.
 * </p>
 * <p>
 * Ces ensembles peuvent être utilisés lors des validations ou des constructions
 * de filtres pour restreindre les opérateurs autorisés selon le type de propriété.
 * </p>
 *
 * @author Frank KOSSI
 * @since 1.2
 */
public final class OperatorUtils {

    /**
     * Ensemble immuable des opérateurs applicables aux propriétés de type texte (String, etc.).
     */
    public static final Set<Operator> FOR_TEXT = Set.of(
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.LIKE, Operator.NOT_LIKE,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
    );

    /**
     * Ensemble immuable des opérateurs applicables aux propriétés de type numérique.
     */
    public static final Set<Operator> FOR_NUMBER = Set.of(
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
            Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL,
            Operator.BETWEEN, Operator.NOT_BETWEEN,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
    );
}

