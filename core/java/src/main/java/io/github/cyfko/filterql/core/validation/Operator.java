package io.github.cyfko.filterql.core.validation;

/**
 * Enumération des opérateurs de filtre supportés.
 * <p>
 * Chaque opérateur définit son propre symbole, code, ainsi que ses règles
 * de validation et les types d'opérations supportées.
 * </p>
 *
 * <p>Cette enum est utilisée pour représenter de manière typée les opérateurs
 * logiques ou comparatifs dans un système de filtrage dynamique.</p>
 *
 * <pre>{@code
 * Operator op = Operator.fromString("=");
 * if (op != null && op.requiresValue()) {
 *     // traitement pour opérateurs nécessitant une valeur
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public enum Operator {

    /** Égalité : "=" */
    EQUALS("=", "EQ"),

    /** Différent : "!=" */
    NOT_EQUALS("!=", "NE"),

    /** Supérieur à : "&gt;" */
    GREATER_THAN(">", "GT"),

    /** Supérieur ou égal à : "&gt;=" */
    GREATER_THAN_OR_EQUAL(">=", "GTE"),

    /** Inférieur à : "&lt;" */
    LESS_THAN("<", "LT"),

    /** Inférieur ou égal à : "&lt;=" */
    LESS_THAN_OR_EQUAL("<=", "LTE"),

    /** Comme (poids) : "LIKE" */
    LIKE("LIKE", "LIKE"),

    /** Pas comme : "NOT LIKE" */
    NOT_LIKE("NOT LIKE", "NOT_LIKE"),

    /** Inclus dans la collection : "IN" */
    IN("IN", "IN"),

    /** Non inclus dans la collection : "NOT IN" */
    NOT_IN("NOT IN", "NOT_IN"),

    /** Est nul : "IS NULL" */
    IS_NULL("IS NULL", "IS_NULL"),

    /** N'est pas nul : "IS NOT NULL" */
    IS_NOT_NULL("IS NOT NULL", "IS_NOT_NULL"),

    /** Entre deux valeurs : "BETWEEN" */
    BETWEEN("BETWEEN", "BETWEEN"),

    /** Pas entre deux valeurs : "NOT BETWEEN" */
    NOT_BETWEEN("NOT BETWEEN", "NOT_BETWEEN");

    private final String symbol;
    private final String code;

    Operator(String symbol, String code) {
        this.symbol = symbol;
        this.code = code;
    }

    /**
     * Retourne le symbole textuel de l'opérateur.
     *
     * @return le symbole (ex. "=", "LIKE")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retourne le code court servant d'identifiant de l'opérateur.
     *
     * @return le code court (ex. "EQ", "LIKE")
     */
    public String getCode() {
        return code;
    }

    /**
     * Recherche un opérateur par son symbole ou son code.
     *
     * @param value symbole ou code à rechercher (non sensible à la casse)
     * @return l'opérateur correspondant, ou {@code null} si non trouvé
     */
    public static Operator fromString(String value) {
        if (value == null) return null;

        String trimmed = value.trim().toUpperCase();

        for (Operator op : values()) {
            if (op.symbol.equalsIgnoreCase(trimmed) || op.code.equalsIgnoreCase(trimmed)) {
                return op;
            }
        }

        return null;
    }

    /**
     * Indique si l'opérateur nécessite impérativement une valeur d'entrée.
     *
     * @return {@code true} si l'opérateur prend une valeur (ex. =, >, IN),
     *         {@code false} s'il s'agit d'un prédicat sans valeur (ex. IS NULL)
     */
    public boolean requiresValue() {
        return this != IS_NULL && this != IS_NOT_NULL;
    }

    /**
     * Indique si l'opérateur supporte plusieurs valeurs (collection ou intervalle).
     *
     * @return {@code true} pour IN, NOT_IN, BETWEEN, NOT_BETWEEN; {@code false} sinon
     */
    public boolean supportsMultipleValues() {
        return this == IN || this == NOT_IN || this == BETWEEN || this == NOT_BETWEEN;
    }
}

