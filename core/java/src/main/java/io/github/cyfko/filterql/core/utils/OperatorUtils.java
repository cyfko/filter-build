package io.github.cyfko.filterql.core.utils;

import io.github.cyfko.filterql.core.validation.Operator;

import java.util.Set;

/**
 * Utilities related to filter operators, including predefined sets of operators
 * suitable for different data types.
 * <p>
 * This class provides, for example, immutable collections of operators
 * applicable to textual or numeric values.
 * </p>
 * <p>
 * These sets can be used during validation or filter construction to restrict
 * the allowed operators according to the property type.
 * </p>
 *
 * @author Frank KOSSI
 * @since 1.2
 */
public final class OperatorUtils {

        /**
         * Immutable set of operators applicable to text properties (String, etc.).
         */
    public static final Set<Operator> FOR_TEXT = Set.of(
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.LIKE, Operator.NOT_LIKE,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
    );

        /**
         * Immutable set of operators applicable to numeric properties.
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

