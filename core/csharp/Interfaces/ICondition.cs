using System;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Interface representing a filter condition that can be combined with other conditions
    /// using logical operators (AND, OR, NOT).
    /// </summary>
    public interface ICondition
    {
        /// <summary>
        /// Creates a new condition representing the logical AND of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this AND other)</returns>
        ICondition And(ICondition other);

        /// <summary>
        /// Creates a new condition representing the logical OR of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this OR other)</returns>
        ICondition Or(ICondition other);

        /// <summary>
        /// Creates a new condition representing the logical negation of this condition.
        /// </summary>
        /// <returns>A new condition representing NOT(this)</returns>
        ICondition Not();
    }
}
