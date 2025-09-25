using System;
using System.Collections.Generic;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Models
{
    /// <summary>
    /// Represents a complete filter request containing multiple filter definitions
    /// and a DSL expression for combining them.
    /// </summary>
    /// <typeparam name="P">The PropertyRef enum type</typeparam>
    public class FilterRequest<P> where P : struct, Enum, IPropertyRef
    {
        /// <summary>
        /// Gets the filter definitions.
        /// </summary>
        public Dictionary<string, FilterDefinition<P>> Filters { get; }

        /// <summary>
        /// Gets the combination operator.
        /// </summary>
        public string CombineWith { get; }

        /// <summary>
        /// Initializes a new instance of the FilterRequest class.
        /// </summary>
        /// <param name="filters">The filter definitions</param>
        /// <param name="combineWith">The combination operator</param>
        public FilterRequest(Dictionary<string, FilterDefinition<P>> filters, string combineWith)
        {
            Filters = filters ?? throw new ArgumentNullException(nameof(filters));
            CombineWith = combineWith ?? throw new ArgumentNullException(nameof(combineWith));
        }

        /// <summary>
        /// Returns a string representation of the FilterRequest.
        /// </summary>
        /// <returns>A string representation of the FilterRequest</returns>
        public override string ToString()
        {
            return $"FilterRequest{{Filters={Filters.Count}, CombineWith='{CombineWith}'}}";
        }
    }
}