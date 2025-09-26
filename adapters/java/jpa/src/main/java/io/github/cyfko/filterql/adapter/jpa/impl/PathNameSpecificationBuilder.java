package io.github.cyfko.filterql.adapter.jpa.impl;

import io.github.cyfko.filterql.adapter.jpa.Specification;
import io.github.cyfko.filterql.adapter.jpa.SpecificationBuilder;
import io.github.cyfko.filterql.adapter.jpa.annotations.NotNull;
import io.github.cyfko.filterql.adapter.jpa.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implémentation d'un {@link SpecificationBuilder} construisant des spécifications JPA
 * basées sur un générateur de chemin de propriété (path name) à partir d'une propriété enum.
 * <p>
 * Cette classe utilise un {@link Function} pour transformer l'enum de propriété {@code P}
 * en nom de chemin (string) utilisé dans les critères JPA.
 * </p>
 * <p>
 * Chaque filtre est traduit en prédicat en fonction de l'opérateur fourni.
 * </p>
 *
 * @param <E> Type d'entité sur lequel portent les spécifications.
 * @param <P> Enum des propriétés filtrables (implémente {@link PropertyRef}).
 *
 * @author Frank
 * @since 1.0
 */
public class PathNameSpecificationBuilder<E, P extends Enum<P> & PropertyRef> implements SpecificationBuilder<E, P> {

    private final Function<P, String> pathNameGenerator;

    /**
     * Construit un générateur de spécification avec un générateur de chemin de propriété.
     *
     * @param pathNameGenerator Fonction convertissant une propriété enum en chemin String.
     * @throws NullPointerException si {@code pathNameGenerator} est null.
     */
    public PathNameSpecificationBuilder(@NotNull Function<P, String> pathNameGenerator) {
        Objects.requireNonNull(pathNameGenerator, "pathNameGenerator must not be null");
        this.pathNameGenerator = pathNameGenerator;
    }

    /**
     * Construit une spécification JPA à partir d'une référence de propriété, un opérateur, et une valeur.
     *
     * @param ref  La propriété enum sur laquelle appliquer le filtre.
     * @param op   L'opérateur de filtre.
     * @param value La valeur du filtre, dont le type doit être compatible avec la propriété et l'opérateur.
     * @return Une spécification JPA correspondant au filtre demandé.
     * @throws IllegalArgumentException si l’opérateur n’est pas supporté.
     * @throws NullPointerException si {@code ref} ou {@code op} sont null.
     */
    @Override
    public Specification<E> build(@NotNull P ref, @NotNull Operator op, Object value) {
        return (Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            String pathName = pathNameGenerator.apply(ref);
            if (pathName == null || pathName.isEmpty()) {
                throw new IllegalArgumentException(String.format("Generated pathName for property reference <%s> must not be null or empty", ref));
            }

            // Validation du type de valeur selon l'opérateur
            ref.validateOperatorForValue(op, value);

            // Résolution du chemin dans l'entité
            Path<?> path = PathResolverUtils.resolvePath(root, pathName);

            switch (op) {
                case EQUALS:
                    return cb.equal(path, value);
                case NOT_EQUALS:
                    return cb.notEqual(path, value);
                case GREATER_THAN:
                    return cb.gt((Path<Number>) path, (Number) value);
                case GREATER_THAN_OR_EQUAL:
                    return cb.ge((Path<Number>) path, (Number) value);
                case LESS_THAN:
                    return cb.lt((Path<Number>) path, (Number) value);
                case LESS_THAN_OR_EQUAL:
                    return cb.le((Path<Number>) path, (Number) value);
                case LIKE:
                    return cb.like((Path<String>) path, (String) value);
                case NOT_LIKE:
                    return cb.notLike((Path<String>) path, (String) value);
                case IN:
                    return path.in((Collection<?>) value);
                case NOT_IN:
                    return cb.not(path.in((Collection<?>) value));
                case IS_NULL:
                    return cb.isNull(path);
                case IS_NOT_NULL:
                    return cb.isNotNull(path);
                case BETWEEN: {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    return cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]);
                }
                case NOT_BETWEEN: {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    return cb.not(cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]));
                }
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + op);
            }
        };
    }
}
