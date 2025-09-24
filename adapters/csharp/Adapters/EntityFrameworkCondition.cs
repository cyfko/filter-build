using System;
using System.Linq.Expressions;
using DynamicFilter.Core.Interfaces;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Entity Framework implementation of the ICondition interface using LINQ expressions.
    /// </summary>
    /// <typeparam name="T">The entity type</typeparam>
    public class EntityFrameworkCondition<T> : ICondition
    {
        private readonly Expression<Func<T, bool>> _expression;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkCondition class.
        /// </summary>
        /// <param name="expression">The LINQ expression</param>
        public EntityFrameworkCondition(Expression<Func<T, bool>> expression)
        {
            _expression = expression ?? throw new ArgumentNullException(nameof(expression));
        }

        /// <summary>
        /// Gets the underlying LINQ expression.
        /// </summary>
        public Expression<Func<T, bool>> Expression => _expression;

        /// <summary>
        /// Creates a new condition representing the logical AND of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this AND other)</returns>
        public ICondition And(ICondition other)
        {
            if (other is not EntityFrameworkCondition<T> otherEf)
            {
                throw new ArgumentException("Cannot combine with non-Entity Framework condition", nameof(other));
            }

            var combinedExpression = Expression.Lambda<Func<T, bool>>(
                Expression.AndAlso(_expression.Body, otherEf._expression.Body),
                _expression.Parameters);

            return new EntityFrameworkCondition<T>(combinedExpression);
        }

        /// <summary>
        /// Creates a new condition representing the logical OR of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this OR other)</returns>
        public ICondition Or(ICondition other)
        {
            if (other is not EntityFrameworkCondition<T> otherEf)
            {
                throw new ArgumentException("Cannot combine with non-Entity Framework condition", nameof(other));
            }

            var combinedExpression = Expression.Lambda<Func<T, bool>>(
                Expression.OrElse(_expression.Body, otherEf._expression.Body),
                _expression.Parameters);

            return new EntityFrameworkCondition<T>(combinedExpression);
        }

        /// <summary>
        /// Creates a new condition representing the logical negation of this condition.
        /// </summary>
        /// <returns>A new condition representing NOT(this)</returns>
        public ICondition Not()
        {
            var negatedExpression = Expression.Lambda<Func<T, bool>>(
                Expression.Not(_expression.Body),
                _expression.Parameters);

            return new EntityFrameworkCondition<T>(negatedExpression);
        }
    }
}
