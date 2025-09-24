using System;

namespace DynamicFilter.Core.Interfaces
{
    /// <summary>
    /// Interface for parsing DSL expressions into FilterTree structures.
    /// This is the core parsing contract that all implementations must follow.
    /// </summary>
    public interface IParser
    {
        /// <summary>
        /// Parses a DSL expression string into a FilterTree.
        /// </summary>
        /// <param name="dslExpression">The DSL expression to parse (e.g., "(f1 & f2) | !f3")</param>
        /// <returns>A FilterTree representing the parsed expression</returns>
        /// <exception cref="DSLSyntaxException">Thrown when the DSL expression is invalid</exception>
        IFilterTree Parse(string dslExpression);
    }
}
