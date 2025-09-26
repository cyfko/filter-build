package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;

/**
 * Interface représentant un arbre d'expression parsé d'une expression DSL.
 * <p>
 * Une instance de {@code FilterTree} peut générer une condition globale en résolvant
 * les références de filtres en fonction du contexte fourni.
 * </p>
 *
 * <p>Cette abstraction permet de représenter des expressions booléennes complexes
 * composées de plusieurs filtres combinés.</p>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public interface FilterTree {

    /**
     * Génère une condition globale en résolvant toutes les références de filtres
     * via le contexte donné.
     *
     * @param context Le contexte fournissant les conditions correspondantes aux clés de filtres.
     * @return Une {@link Condition} représentant l'ensemble de l'arbre de filtres.
     * @throws FilterValidationException Si la validation des filtres échoue ou la génération de la condition n'est pas possible.
     * @throws DSLSyntaxException Si la combinaison des filtres fait référence à un filtre non définit.
     */
    Condition generate(Context context) throws FilterValidationException, DSLSyntaxException;
}

