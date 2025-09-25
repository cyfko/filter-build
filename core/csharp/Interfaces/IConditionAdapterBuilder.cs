using System;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Builder interface for creating condition adapters.
    /// Each implementation defines how to build a condition from PropertyRef, Operator, and value.
    /// </summary>
    /// <typeparam name="T">The entity type (e.g., User, Product)</typeparam>
    /// <typeparam name="P">The PropertyRef enum for this entity</typeparam>
    public interface IConditionAdapterBuilder<T, P> where P : struct, Enum
    {
        /// <summary>
        /// Builds a condition adapter from the given parameters.
        /// </summary>
        /// <param name="ref">The property reference key (type-safe enum key)</param>
        /// <param name="op">The operator</param>
        /// <param name="value">The value as object</param>
        /// <returns>A condition adapter</returns>
        ICondition Build(P @ref, Operator @op, object? value);
    }
}

