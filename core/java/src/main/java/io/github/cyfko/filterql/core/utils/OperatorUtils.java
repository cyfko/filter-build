package io.github.cyfko.filterql.core.utils;

import io.github.cyfko.filterql.core.validation.Op;

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
 * @since 2.0.0
 */
public final class OperatorUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private OperatorUtils() {
        // Utility class - no instantiation allowed
    }

        /**
         * Immutable set of operators applicable to text properties (String, etc.).
         */
    public static final Set<Op> FOR_TEXT = Set.of(
            Op.EQ, Op.NE,
            Op.MATCHES, Op.NOT_MATCHES,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.NOT_NULL
    );

        /**
         * Immutable set of operators applicable to numeric properties.
         */
    public static final Set<Op> FOR_NUMBER = Set.of(
            Op.EQ, Op.NE,
            Op.GT, Op.GTE,
            Op.LT, Op.LTE,
            Op.RANGE, Op.NOT_RANGE,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.NOT_NULL
    );
}

