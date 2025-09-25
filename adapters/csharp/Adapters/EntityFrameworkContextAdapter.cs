using System;
using System.Collections.Generic;
using DynamicFilter.Core.Interfaces;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Entity Framework implementation of the IContextAdapter interface using the correct pattern.
    /// This adapter uses EntityFrameworkConditionAdapterBuilder to create conditions.
    /// </summary>
    /// <typeparam name="TEntity">The entity type</typeparam>
    /// <typeparam name="TPropertyRef">The PropertyRef enum type</typeparam>
    public class EntityFrameworkContextAdapter<TEntity, TPropertyRef> : IContextAdapter<TEntity, TPropertyRef>
        where TPropertyRef : struct, Enum
    {
        private readonly Dictionary<string, EntityFrameworkConditionAdapter<TEntity>> _filters;
        private readonly IEntityFrameworkConditionAdapterBuilder<TEntity, TPropertyRef> _conditionAdapterBuilder;
        private readonly object _propertyRefImpl;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkContextAdapter class.
        /// </summary>
        /// <param name="conditionAdapterBuilder">The condition adapter builder</param>
        /// <param name="propertyRefImpl">The PropertyRef implementation</param>
        public EntityFrameworkContextAdapter(
            IEntityFrameworkConditionAdapterBuilder<TEntity, TPropertyRef> conditionAdapterBuilder,
            object propertyRefImpl)
        {
            _filters = new Dictionary<string, EntityFrameworkConditionAdapter<TEntity>>();
            _conditionAdapterBuilder = conditionAdapterBuilder ?? throw new ArgumentNullException(nameof(conditionAdapterBuilder));
            _propertyRefImpl = propertyRefImpl ?? throw new ArgumentNullException(nameof(propertyRefImpl));
        }

        /// <summary>
        /// Adds a condition using the builder pattern.
        /// </summary>
        /// <param name="filterKey">The filter key</param>
        /// <param name="definition">The filter definition</param>
        public void AddCondition(string filterKey, FilterDefinition<TPropertyRef> definition)
        {
            // Get PropertyRef and Operator directly (type-safe, no resolution needed)
            var propertyRefKey = definition.Ref;
            var operator = definition.Operator;

            // Get the PropertyRef implementation
            var propertyRef = GetPropertyRefImplementation(propertyRefKey);
            if (propertyRef == null)
            {
                throw new ArgumentException($"Invalid PropertyRef: {propertyRefKey}");
            }

            // Validate that the property supports this operator
            propertyRef.ValidateOperator(operator);

            // Build condition using the builder and store it
            var condition = _conditionAdapterBuilder.Build(propertyRefKey, operator, definition.Value);
            _filters[filterKey] = condition;
        }

        /// <summary>
        /// Retrieves the condition associated with the given filter key.
        /// </summary>
        /// <param name="filterKey">The unique identifier for the filter</param>
        /// <returns>The condition associated with the filter key</returns>
        public ICondition? GetCondition(string filterKey)
        {
            return _filters.TryGetValue(filterKey, out var condition) ? condition : null;
        }

        private IPropertyRef? GetPropertyRefImplementation(TPropertyRef propertyRefKey)
        {
            var propertyName = propertyRefKey.ToString();
            var propertyInfo = _propertyRefImpl.GetType().GetProperty(propertyName);
            return propertyInfo?.GetValue(_propertyRefImpl) as IPropertyRef;
        }
    }
}