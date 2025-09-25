using System;

namespace DynamicFilter.Core.Validation
{
    /// <summary>
    /// Enumeration of supported filter operators.
    /// </summary>
    public enum Operator
    {
        Equals,
        NotEquals,
        GreaterThan,
        GreaterThanOrEqual,
        LessThan,
        LessThanOrEqual,
        Like,
        NotLike,
        In,
        NotIn,
        IsNull,
        IsNotNull,
        Between,
        NotBetween
    }

    /// <summary>
    /// Extension methods for the Operator enum.
    /// </summary>
    public static class OperatorExtensions
    {
        /// <summary>
        /// Gets the string representation of an operator.
        /// </summary>
        /// <param name="operator">The operator</param>
        /// <returns>The string representation</returns>
        public static string ToString(this Operator @operator)
        {
            return @operator switch
            {
                Operator.Equals => "=",
                Operator.NotEquals => "!=",
                Operator.GreaterThan => ">",
                Operator.GreaterThanOrEqual => ">=",
                Operator.LessThan => "<",
                Operator.LessThanOrEqual => "<=",
                Operator.Like => "LIKE",
                Operator.NotLike => "NOT LIKE",
                Operator.In => "IN",
                Operator.NotIn => "NOT IN",
                Operator.IsNull => "IS NULL",
                Operator.IsNotNull => "IS NOT NULL",
                Operator.Between => "BETWEEN",
                Operator.NotBetween => "NOT BETWEEN",
                _ => throw new ArgumentOutOfRangeException(nameof(@operator))
            };
        }

        /// <summary>
        /// Parses a string to an operator.
        /// </summary>
        /// <param name="value">The string value</param>
        /// <returns>The operator, or null if not found</returns>
        public static Operator? Parse(string value)
        {
            if (string.IsNullOrWhiteSpace(value))
                return null;

            var trimmed = value.Trim().ToUpperInvariant();

            return trimmed switch
            {
                "=" or "EQUALS" => Operator.Equals,
                "!=" or "NOT_EQUALS" or "NOT EQUALS" => Operator.NotEquals,
                ">" or "GREATER_THAN" or "GREATER THAN" => Operator.GreaterThan,
                ">=" or "GREATER_THAN_OR_EQUAL" or "GREATER THAN OR EQUAL" => Operator.GreaterThanOrEqual,
                "<" or "LESS_THAN" or "LESS THAN" => Operator.LessThan,
                "<=" or "LESS_THAN_OR_EQUAL" or "LESS THAN OR EQUAL" => Operator.LessThanOrEqual,
                "LIKE" => Operator.Like,
                "NOT LIKE" or "NOT_LIKE" => Operator.NotLike,
                "IN" => Operator.In,
                "NOT IN" or "NOT_IN" => Operator.NotIn,
                "IS NULL" or "IS_NULL" => Operator.IsNull,
                "IS NOT NULL" or "IS_NOT_NULL" => Operator.IsNotNull,
                "BETWEEN" => Operator.Between,
                "NOT BETWEEN" or "NOT_BETWEEN" => Operator.NotBetween,
                _ => null
            };
        }

        /// <summary>
        /// Checks if this operator requires a value.
        /// </summary>
        /// <param name="operator">The operator</param>
        /// <returns>True if the operator requires a value, false otherwise</returns>
        public static bool RequiresValue(this Operator @operator)
        {
            return @operator != Operator.IsNull && @operator != Operator.IsNotNull;
        }

        /// <summary>
        /// Checks if this operator supports multiple values.
        /// </summary>
        /// <param name="operator">The operator</param>
        /// <returns>True if the operator supports multiple values, false otherwise</returns>
        public static bool SupportsMultipleValues(this Operator @operator)
        {
            return @operator == Operator.In || @operator == Operator.NotIn || 
                   @operator == Operator.Between || @operator == Operator.NotBetween;
        }
    }
}
