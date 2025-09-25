using System;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Interface representing a parsed DSL expression tree.
    /// The FilterTree can generate a global condition by resolving filter references
    /// through the provided context.
    /// </summary>
    public interface IFilterTree
    {
        /// <summary>
        /// Generates a global condition by resolving all filter references through the context.
        /// </summary>
        /// <param name="context">The context providing conditions for filter keys</param>
        /// <returns>A condition representing the entire filter tree</returns>
        ICondition Generate(IContext context);
    }
}
