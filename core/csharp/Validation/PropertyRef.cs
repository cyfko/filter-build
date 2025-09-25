using System;
using System.Collections.Generic;
using System.Linq;

namespace DynamicFilter.Core.Validation
{
    /// <summary>
    /// Interface for property references in dynamic filtering.
    /// 
    /// Developers should create their own enums implementing this interface to define
    /// the properties available for their entities.
    /// 
    /// Example usage:
    /// <code>
    /// public enum UserPropertyRef : IPropertyRef
    /// {
    ///     UserName,
    ///     UserAge,
    ///     UserEmail
    /// }
    /// 
    /// // PropertyRef implementation
    /// public class UserPropertyRefImpl
    /// {
    ///     public static readonly IPropertyRef UserName = new PropertyRefImpl("string", new[] { Operator.Like, Operator.Equals });
    ///     public static readonly IPropertyRef UserAge = new PropertyRefImpl("int", new[] { Operator.Equals, Operator.GreaterThan });
    ///     public static readonly IPropertyRef UserEmail = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });
    /// }
    /// </code>
    /// </summary>
    public interface IPropertyRef
    {
        /// <summary>
        /// Gets the type of this property.
        /// </summary>
        string Type { get; }

        /// <summary>
        /// Gets the set of operators supported by this property.
        /// </summary>
        IReadOnlyList<Operator> SupportedOperators { get; }

        /// <summary>
        /// Checks if this property supports the given operator.
        /// </summary>
        /// <param name="operator">The operator to check</param>
        /// <returns>True if the operator is supported, false otherwise</returns>
        bool SupportsOperator(Operator @operator);

        /// <summary>
        /// Validates that the given operator is supported by this property.
        /// </summary>
        /// <param name="operator">The operator to validate</param>
        /// <exception cref="ArgumentException">Thrown if the operator is not supported</exception>
        void ValidateOperator(Operator @operator);
    }

    /// <summary>
    /// Implementation of IPropertyRef interface for enum values.
    /// </summary>
    public class PropertyRefImpl : IPropertyRef
    {
        public string Type { get; }
        public IReadOnlyList<Operator> SupportedOperators { get; }

        public PropertyRefImpl(string type, Operator[] supportedOperators)
        {
            Type = type ?? throw new ArgumentNullException(nameof(type));
            SupportedOperators = supportedOperators?.ToList().AsReadOnly() ?? throw new ArgumentNullException(nameof(supportedOperators));
        }

        public bool SupportsOperator(Operator @operator)
        {
            return SupportedOperators.Contains(@operator);
        }

        public void ValidateOperator(Operator @operator)
        {
            if (!SupportsOperator(@operator))
            {
                throw new ArgumentException(
                    $"Operator '{@operator}' is not supported for this property. " +
                    $"Supported operators: {string.Join(", ", SupportedOperators)}");
            }
        }

        public string GetDescription()
        {
            return $"PropertyRef({Type})";
        }

        public override string ToString()
        {
            return $"PropertyRef{{Type={Type}, SupportedOperators=[{string.Join(", ", SupportedOperators)}]}}";
        }

        public override bool Equals(object obj)
        {
            if (obj is PropertyRefImpl other)
            {
                return Type == other.Type;
            }
            return false;
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(Type);
        }
    }
}
