using System;
using System.Collections.Generic;
using System.Linq;

namespace DynamicFilter.Core.Validation
{
    /// <summary>
    /// Base class for property references.
    /// Developers should create their own enums extending this class to define
    /// the properties available for their entities.
    /// 
    /// Example usage:
    /// <code>
    /// public class UserPropertyRef : PropertyRef
    /// {
    ///     public static readonly UserPropertyRef UserName = new UserPropertyRef("userName", "string", new[] { Operator.Like, Operator.Equals });
    ///     public static readonly UserPropertyRef UserAge = new UserPropertyRef("age", "int", new[] { Operator.Equals, Operator.GreaterThan });
    /// }
    /// </code>
    /// </summary>
    public abstract class PropertyRef
    {
        public string EntityField { get; }
        public string Type { get; }
        public IReadOnlyList<Operator> SupportedOperators { get; }

        protected PropertyRef(string entityField, string type, Operator[] supportedOperators)
        {
            EntityField = entityField ?? throw new ArgumentNullException(nameof(entityField));
            Type = type ?? throw new ArgumentNullException(nameof(type));
            SupportedOperators = supportedOperators?.ToList().AsReadOnly() ?? throw new ArgumentNullException(nameof(supportedOperators));
        }

        /// <summary>
        /// Checks if this property supports the given operator.
        /// </summary>
        /// <param name="operator">The operator to check</param>
        /// <returns>True if the operator is supported, false otherwise</returns>
        public bool SupportsOperator(Operator @operator)
        {
            return SupportedOperators.Contains(@operator);
        }

        /// <summary>
        /// Validates that the given operator is supported by this property.
        /// </summary>
        /// <param name="operator">The operator to validate</param>
        /// <exception cref="ArgumentException">Thrown if the operator is not supported</exception>
        public void ValidateOperator(Operator @operator)
        {
            if (!SupportsOperator(@operator))
            {
                throw new ArgumentException(
                    $"Operator '{@operator}' is not supported for property '{EntityField}'. " +
                    $"Supported operators: {string.Join(", ", SupportedOperators)}");
            }
        }

        /// <summary>
        /// Gets a human-readable description of this property reference.
        /// </summary>
        /// <returns>A description string</returns>
        public string GetDescription()
        {
            return $"{GetType().Name}.{EntityField} ({Type})";
        }

        /// <summary>
        /// Returns a string representation of this property reference.
        /// </summary>
        /// <returns>A string representation</returns>
        public override string ToString()
        {
            return $"PropertyRef{{EntityField='{EntityField}', Type={Type}, SupportedOperators=[{string.Join(", ", SupportedOperators)}]}}";
        }

        /// <summary>
        /// Determines whether the specified object is equal to this property reference.
        /// </summary>
        /// <param name="obj">The object to compare</param>
        /// <returns>True if the objects are equal, false otherwise</returns>
        public override bool Equals(object obj)
        {
            if (obj is PropertyRef other)
            {
                return EntityField == other.EntityField && Type == other.Type;
            }
            return false;
        }

        /// <summary>
        /// Returns a hash code for this property reference.
        /// </summary>
        /// <returns>A hash code</returns>
        public override int GetHashCode()
        {
            return HashCode.Combine(EntityField, Type);
        }
    }
}
