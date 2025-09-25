using System;
using DynamicFilter.Core.Models;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Context adapter interface for type-safe filter building.
    /// </summary>
    /// <typeparam name="T">The entity type (e.g., User, Product)</typeparam>
    /// <typeparam name="P">The PropertyRef enum for this entity</typeparam>
    public interface IContextAdapter<T, P> : IContext where P : struct, Enum
    {
        /// <summary>
        /// Adds a condition for the given filter key.
        /// </summary>
        /// <param name="filterKey">The filter key</param>
        /// <param name="definition">The filter definition</param>
        void AddCondition(string filterKey, FilterDefinition<P> definition);
    }
}

