using System;
using System.Collections.Generic;
using DynamicFilter.Core.Interfaces;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Entity Framework implementation of the IContext interface using the correct pattern.
    /// This adapter uses EntityFrameworkConditionAdapterBuilder to create conditions.
    /// </summary>
    /// <typeparam name="TEntity">The entity type</typeparam>
    /// <typeparam name="TPropertyRef">The PropertyRef type</typeparam>
    public class EntityFrameworkContextAdapter<TEntity, TPropertyRef> : IContext 
        where TPropertyRef : PropertyRef
    {
        private readonly Dictionary<string, EntityFrameworkConditionAdapter<TEntity>> _filters;
        private readonly IEntityFrameworkConditionAdapterBuilder<TEntity, TPropertyRef> _conditionAdapterBuilder;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkContextAdapter class.
        /// </summary>
        /// <param name="conditionAdapterBuilder">The condition adapter builder</param>
        public EntityFrameworkContextAdapter(IEntityFrameworkConditionAdapterBuilder<TEntity, TPropertyRef> conditionAdapterBuilder)
        {
            _filters = new Dictionary<string, EntityFrameworkConditionAdapter<TEntity>>();
            _conditionAdapterBuilder = conditionAdapterBuilder ?? throw new ArgumentNullException(nameof(conditionAdapterBuilder));
        }

        /// <summary>
        /// Adds a condition using the builder pattern.
        /// </summary>
        /// <param name="filterKey">The filter key</param>
        /// <param name="definition">The filter definition</param>
        public void AddCondition(string filterKey, FilterDefinition<TPropertyRef> definition)
        {
            // Get PropertyRef and Operator directly (type-safe, no resolution needed)
            var propertyRef = definition.Ref;
            var operator = definition.Operator;

            // Validate that the property supports this operator
            propertyRef.ValidateOperator(operator);

            // Build condition using the builder and store it
            var condition = _conditionAdapterBuilder.Build(propertyRef, operator, definition.Value);
            _filters[filterKey] = condition;
        }

        /// <summary>
        /// Retrieves the condition associated with the given filter key.
        /// </summary>
        /// <param name="filterKey">The unique identifier for the filter</param>
        /// <returns>The condition associated with the filter key</returns>
        public ICondition? GetCondition(string filterKey)
        {
            if (!_filters.TryGetValue(filterKey, out var condition))
            {
                throw new ArgumentException($"No condition found for key: {filterKey}", nameof(filterKey));
            }
            return condition;
        }

    }
}