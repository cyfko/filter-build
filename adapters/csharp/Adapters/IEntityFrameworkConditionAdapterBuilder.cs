using System;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Builder interface for creating Entity Framework condition adapters.
    /// Each implementation defines how to build an Entity Framework condition from PropertyRef, Operator, and value.
    /// </summary>
    /// <typeparam name="TEntity">The entity type</typeparam>
    /// <typeparam name="TPropertyRef">The PropertyRef enum type</typeparam>
    public interface IEntityFrameworkConditionAdapterBuilder<TEntity, TPropertyRef> 
        where TPropertyRef : struct, Enum, IPropertyRef
    {
        /// <summary>
        /// Builds an Entity Framework condition adapter from the given parameters.
        /// </summary>
        /// <param name="ref">The property reference (type-safe)</param>
        /// <param name="op">The operator</param>
        /// <param name="value">The value as object</param>
        /// <returns>An Entity Framework condition adapter</returns>
        EntityFrameworkConditionAdapter<TEntity> Build(TPropertyRef @ref, Operator op, object? value);
    }
}
