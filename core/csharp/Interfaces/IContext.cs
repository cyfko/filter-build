using System;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Interface for providing conditions by filter key.
    /// The Context acts as a registry that maps filter tokens to their corresponding conditions.
    /// </summary>
    public interface IContext
    {
        /// <summary>
        /// Retrieves the condition associated with the given filter key.
        /// </summary>
        /// <param name="filterKey">The unique identifier for the filter</param>
        /// <returns>The condition associated with the filter key, or null if not found</returns>
        ICondition? GetCondition(string filterKey);
    }
}
