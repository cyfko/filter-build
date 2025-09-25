using System;

namespace DynamicFilter.Core.Exceptions
{
    /// <summary>
    /// Exception thrown when a DSL expression contains syntax errors or invalid references.
    /// </summary>
    public class DSLSyntaxException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the DSLSyntaxException class.
        /// </summary>
        /// <param name="message">The error message</param>
        public DSLSyntaxException(string message) : base(message)
        {
        }

        /// <summary>
        /// Initializes a new instance of the DSLSyntaxException class.
        /// </summary>
        /// <param name="message">The error message</param>
        /// <param name="innerException">The inner exception</param>
        public DSLSyntaxException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
