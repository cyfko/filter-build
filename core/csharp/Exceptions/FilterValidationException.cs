using System;

namespace DynamicFilter.Core.Exceptions
{
    /// <summary>
    /// Exception thrown when a filter condition cannot be validated or constructed.
    /// </summary>
    public class FilterValidationException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the FilterValidationException class.
        /// </summary>
        /// <param name="message">The error message</param>
        public FilterValidationException(string message) : base(message)
        {
        }

        /// <summary>
        /// Initializes a new instance of the FilterValidationException class.
        /// </summary>
        /// <param name="message">The error message</param>
        /// <param name="innerException">The inner exception</param>
        public FilterValidationException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
