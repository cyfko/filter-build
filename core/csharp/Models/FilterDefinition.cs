using System;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Models
{
    /// <summary>
    /// Represents a single filter definition with property reference, operator, and value.
    /// The ref is now type-safe with a specific PropertyRef enum.
    /// </summary>
    /// <typeparam name="T">The PropertyRef type</typeparam>
    public class FilterDefinition<T> where T : PropertyRef
    {
        /// <summary>
        /// Gets the property reference (type-safe).
        /// </summary>
        public T Ref { get; }

        /// <summary>
        /// Gets the operator.
        /// </summary>
        public string Operator { get; }

        /// <summary>
        /// Gets the value.
        /// </summary>
        public object? Value { get; }

        /// <summary>
        /// Initializes a new instance of the FilterDefinition class.
        /// </summary>
        /// <param name="ref">The property reference (type-safe)</param>
        /// <param name="operator">The operator</param>
        /// <param name="value">The value</param>
        public FilterDefinition(T @ref, string @operator, object? value)
        {
            Ref = @ref ?? throw new ArgumentNullException(nameof(@ref));
            Operator = @operator ?? throw new ArgumentNullException(nameof(@operator));
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
