package io.github.cyfko.filterql.core.validation;

import io.github.cyfko.filterql.core.utils.ClassUtils;

import java.util.Collection;
import java.util.Set;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface représentant une référence de propriété dans un contexte de filtrage dynamique.
 * <p>
 * Les développeurs doivent créer leurs propres enums implémentant cette interface pour définir
 * les propriétés accessibles et leurs caractéristiques sur leurs entités.
 * </p>
 * <p>
 * {@code PropertyRef} contient uniquement la définition logique de la propriété (type, opérateurs supportés).
 * Chaque adaptateur est responsable d'interpréter cette interface et de construire les conditions appropriées.
 * Cette distinction offre une grande flexibilité, permettant par exemple qu'une seule référence de propriété
 * corresponde à plusieurs champs ou conditions complexes.
 * </p>
 * <p><b>Exemple d'utilisation :</b></p>
 * <pre>{@code
 * public enum UserPropertyRef implements PropertyRef {
 *     USER_NAME(String.class, Set.of(LIKE, EQUALS, IN)),
 *     USER_AGE(Integer.class, Set.of(EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, BETWEEN)),
 *     USER_STATUS(UserStatus.class, Set.of(EQUALS, NOT_EQUALS, IN));
 *
 *     private final Class<?> type;
 *     private final Set<Operator> supportedOperators;
 *
 *     UserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
 *         this.type = type;
 *         this.supportedOperators = Set.copyOf(supportedOperators);
 *     }
 *
 *     @Override
 *     public Class<?> getType() { return type; }
 *
 *     @Override
 *     public Set<Operator> getSupportedOperators() { return supportedOperators; }
 * }
 * }</pre>
 *
 * @author Cyfko
 * @since 1.0
 */
public interface PropertyRef {

    /**
     * Retourne le type Java de la propriété représentée.
     *
     * @return la classe Java représentant le type de la propriété (ex: String.class, Integer.class)
     */
    Class<?> getType();

    /**
     * Retourne la collection non modifiable des opérateurs supportés par cette propriété.
     *
     * @return un {@link Set} immuable des opérateurs que la propriété supporte
     */
    Set<Operator> getSupportedOperators();

    /**
     * Indique si l'opérateur donné est supporté pour cette propriété.
     *
     * @param operator l'opérateur à tester, non nul
     * @return {@code true} si l'opérateur est supporté, {@code false} sinon
     * @throws NullPointerException si {@code operator} est {@code null}
     */
    default boolean supportsOperator(Operator operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        return getSupportedOperators().contains(operator);
    }

    /**
     * Valide que l'opérateur donné est bien supporté par cette propriété.
     * Lance une {@link IllegalArgumentException} si ce n'est pas le cas.
     *
     * @param operator l'opérateur à valider, non nul
     * @throws IllegalArgumentException si l'opérateur n'est pas supporté
     * @throws NullPointerException     si {@code operator} est {@code null}
     */
    default void validateOperator(Operator operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        if (!supportsOperator(operator)) {
            throw new IllegalArgumentException(
                    String.format("Operator %s is not supported for property %s. Supported operators: %s",
                            operator, this, getSupportedOperators()));
        }
    }

    /**
     * Valide que l'opérateur donné est applicable pour la valeur fournie,
     * en se basant sur le type attendu de la propriété et la nature de la valeur.
     * <p>
     * Pour les opérateurs simples (comparaisons, like), la valeur doit être assignable au type de la propriété.
     * Pour les opérateurs de vérification de nullité (IS_NULL, IS_NOT_NULL), aucune valeur n'est requise.
     * Pour les opérateurs impliquant des collections ou des intervalles (IN, BETWEEN), la valeur
     * doit être une collection compatible avec le type attendu.
     * </p>
     *
     * @param operator l'opérateur à valider, non nul
     * @param value    la valeur sur laquelle l'opérateur s'applique, peut être nulle pour certains opérateurs
     * @throws IllegalArgumentException si l'opérateur n'est pas applicable pour la valeur donnée
     * @throws NullPointerException     si {@code operator} est {@code null}
     */
    default void validateOperatorForValue(Operator operator, Object value) {
        Objects.requireNonNull(operator, "Operator cannot be null");

        // Validation de l'opérateur pour cette propriété
        validateOperator(operator);

        // Validation spécifique selon le type d'opérateur
        ValidationResult result = validateValueForOperator(operator, value);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
    }

    /**
     * Valide la compatibilité entre un opérateur et une valeur.
     * Méthode interne pour une validation fine et extensible.
     *
     * @param operator l'opérateur à valider
     * @param value la valeur à valider
     * @return le résultat de validation
     */
    private ValidationResult validateValueForOperator(Operator operator, Object value) {
        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case LIKE:
            case NOT_LIKE:
                return validateSingleValue(operator, value);

            case IS_NULL:
            case IS_NOT_NULL:
                return validateNullCheck(operator, value);

            case IN:
            case NOT_IN:
                return validateCollectionValue(operator, value, false);

            case BETWEEN:
            case NOT_BETWEEN:
                return validateCollectionValue(operator, value, true);

            default:
                return ValidationResult.failure(
                        String.format("Unsupported operator: %s", operator)
                );
        }
    }

    /**
     * Valide une valeur simple pour les opérateurs de comparaison.
     */
    private ValidationResult validateSingleValue(Operator operator, Object value) {
        if (value == null) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-null value", operator)
            );
        }

        if (!isCompatibleType(value.getClass(), getType())) {
            return ValidationResult.failure(
                    String.format("Value of type %s is not compatible with property type %s for operator %s",
                            value.getClass().getSimpleName(),
                            getType().getSimpleName(),
                            operator)
            );
        }

        return ValidationResult.success();
    }

    /**
     * Valide les opérateurs de vérification de nullité.
     */
    private ValidationResult validateNullCheck(Operator operator, Object value) {
        // Pour IS_NULL et IS_NOT_NULL, la valeur devrait être null ou absente
        // mais on peut être tolérant et accepter toute valeur
        return ValidationResult.success();
    }

    /**
     * Valide une valeur collection pour les opérateurs IN et BETWEEN.
     */
    private ValidationResult validateCollectionValue(Operator operator, Object value, boolean requiresExactlyTwo) {
        if (value == null) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-null collection value", operator)
            );
        }

        if (!(value instanceof Collection<?>)) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a Collection value, got %s",
                            operator, value.getClass().getSimpleName())
            );
        }

        Collection<?> collection = (Collection<?>) value;

        if (collection.isEmpty()) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-empty collection", operator)
            );
        }

        if (requiresExactlyTwo && collection.size() != 2) {
            return ValidationResult.failure(
                    String.format("Operator %s requires exactly 2 values for range, got %d",
                            operator, collection.size())
            );
        }

        // Vérifier la compatibilité des types des éléments
        if (!ClassUtils.allCompatible(getType(), collection)) {
            return ValidationResult.failure(
                    String.format("Collection elements are not compatible with property type %s for operator %s",
                            getType().getSimpleName(), operator)
            );
        }

        return ValidationResult.success();
    }

    /**
     * Vérifie la compatibilité entre deux types.
     * Gère les types primitifs et leurs wrappers.
     */
    private boolean isCompatibleType(Class<?> valueType, Class<?> expectedType) {
        // Assignabilité directe
        if (expectedType.isAssignableFrom(valueType)) {
            return true;
        }

        // Gestion des primitives et leurs wrappers
        return isPrimitiveCompatible(valueType, expectedType);
    }

    /**
     * Vérifie la compatibilité entre types primitifs et leurs wrappers.
     */
    private boolean isPrimitiveCompatible(Class<?> valueType, Class<?> expectedType) {
        Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
                boolean.class, Boolean.class,
                byte.class, Byte.class,
                char.class, Character.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class
        );

        // Primitive -> Wrapper
        if (primitiveToWrapper.get(expectedType) == valueType) {
            return true;
        }

        // Wrapper -> Primitive
        return primitiveToWrapper.get(valueType) == expectedType;
    }

    /**
     * Vérifie si cette propriété supporte un ensemble d'opérateurs.
     * Utile pour valider des configurations ou des requêtes complexes.
     *
     * @param operators collection d'opérateurs à vérifier, non nulle
     * @return true si tous les opérateurs sont supportés
     * @throws NullPointerException si operators est null
     */
    default boolean supportsAllOperators(Collection<Operator> operators) {
        Objects.requireNonNull(operators, "Operators collection cannot be null");
        return getSupportedOperators().containsAll(operators);
    }

    /**
     * Retourne les opérateurs non supportés parmi ceux fournis.
     * Utile pour le debugging et les messages d'erreur détaillés.
     *
     * @param operators collection d'opérateurs à vérifier, non nulle
     * @return ensemble des opérateurs non supportés (peut être vide)
     * @throws NullPointerException si operators est null
     */
    default Set<Operator> getUnsupportedOperators(Collection<Operator> operators) {
        Objects.requireNonNull(operators, "Operators collection cannot be null");
        return operators.stream()
                .filter(op -> !supportsOperator(op))
                .collect(Collectors.toSet());
    }

    /**
     * Vérifie si cette propriété est de type numérique.
     * Utile pour déterminer quels opérateurs de comparaison appliquer.
     *
     * @return true si le type est un nombre
     */
    default boolean isNumeric() {
        Class<?> type = getType();
        return Number.class.isAssignableFrom(type) ||
                isPrimitiveNumber(type);
    }

    /**
     * Vérifie si le type est un primitif numérique.
     */
    private boolean isPrimitiveNumber(Class<?> type) {
        return type == byte.class || type == short.class ||
                type == int.class || type == long.class ||
                type == float.class || type == double.class;
    }

    /**
     * Vérifie si cette propriété est de type textuel.
     * Utile pour déterminer si les opérateurs LIKE sont applicables.
     *
     * @return true si le type est String ou CharSequence
     */
    default boolean isTextual() {
        return CharSequence.class.isAssignableFrom(getType());
    }

    /**
     * Classe interne représentant le résultat d'une validation.
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}