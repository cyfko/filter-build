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
    /// <typeparam name="T">The entity type</typeparam>
    public class EntityFrameworkContextAdapter<T> : IContext
    {
        private readonly Dictionary<string, EntityFrameworkConditionAdapter<T>> _filters;
        private readonly IEntityFrameworkConditionAdapterBuilder<T> _conditionAdapterBuilder;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkContextAdapter class.
        /// </summary>
        /// <param name="conditionAdapterBuilder">The condition adapter builder</param>
        public EntityFrameworkContextAdapter(IEntityFrameworkConditionAdapterBuilder<T> conditionAdapterBuilder)
        {
            _filters = new Dictionary<string, EntityFrameworkConditionAdapter<T>>();
            _conditionAdapterBuilder = conditionAdapterBuilder ?? throw new ArgumentNullException(nameof(conditionAdapterBuilder));
        }

        /// <summary>
        /// Adds a condition using the builder pattern.
        /// </summary>
        /// <param name="filterKey">The filter key</param>
        /// <param name="definition">The filter definition</param>
        public void AddCondition(string filterKey, FilterDefinition definition)
        {
            // Resolve String ref to PropertyRef enum
            var propertyRef = ResolvePropertyRef(definition.Ref);
            if (propertyRef == null)
            {
                throw new ArgumentException($"Property not found: {definition.Ref}", nameof(definition));
            }

            // Validate operator
            var operator = ParseOperator(definition.Operator);
            if (operator == null)
            {
                throw new ArgumentException($"Invalid operator: {definition.Operator}", nameof(definition));
            }

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

        /// <summary>
        /// Resolves a String ref to the appropriate PropertyRef enum.
        /// Each adapter can implement its own resolution logic.
        /// This method should be overridden by concrete implementations to provide
        /// the actual PropertyRef resolution strategy.
        /// </summary>
        /// <param name="ref">The property reference string</param>
        /// <returns>The PropertyRef enum or null if not found</returns>
        private PropertyRef? ResolvePropertyRef(string ref)
        {
            // This is a placeholder implementation.
            // Concrete adapters should override this method to implement
            // their own PropertyRef resolution strategy.
            throw new NotImplementedException(
                "PropertyRef resolution must be implemented by concrete adapter implementations");
        }

        /// <summary>
        /// Parses operator string to Operator enum.
        /// </summary>
        /// <param name="operatorStr">The operator string</param>
        /// <returns>The Operator enum or null if not found</returns>
        private Operator? ParseOperator(string operatorStr)
        {
            // This should use the core ParseOperator function
            // For now, return null to indicate it needs implementation
            return null;
        }
    }
}