using System;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Models
{
    /// <summary>
    /// Represents a single filter definition with property reference, operator, and value.
    /// The ref must be an enum implementing IPropertyRef for type safety and performance.
    /// </summary>
    /// <typeparam name="P">The PropertyRef enum type</typeparam>
    public class FilterDefinition<P> where P : struct, Enum
    {
        /// <summary>
        /// Gets the property reference (type-safe enum).
        /// </summary>
        public P Ref { get; }

        /// <summary>
        /// Gets the operator (type-safe).
        /// </summary>
        public Operator Operator { get; }

        /// <summary>
        /// Gets the value.
        /// </summary>
        public object? Value { get; }

        /// <summary>
        /// Initializes a new instance of the FilterDefinition class.
        /// </summary>
        /// <param name="ref">The property reference (type-safe enum)</param>
        /// <param name="operator">The operator (type-safe)</param>
        /// <param name="value">The value</param>
        public FilterDefinition(P @ref, Operator @operator, object? value)
        {
            Ref = @ref;
            Operator = @operator;
            Value = value;
        }

        /// <summary>
        /// Returns a string representation of the FilterDefinition.
        /// </summary>
        /// <returns>A string representation of the FilterDefinition</returns>
        public override string ToString()
        {
            return $"FilterDefinition{{Ref={Ref}, Operator='{Operator}', Value={Value}}}";
        }
    }
}
