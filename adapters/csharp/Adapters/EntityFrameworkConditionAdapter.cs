using System;
using System.Linq.Expressions;
using DynamicFilter.Core.Interfaces;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Entity Framework implementation of the ICondition interface using LINQ expressions.
    /// This adapter wraps Expression&lt;Func&lt;T, bool&gt;&gt; for Entity Framework queries.
    /// </summary>
    /// <typeparam name="T">The entity type</typeparam>
    public class EntityFrameworkConditionAdapter<T> : ICondition
    {
        private readonly Expression<Func<T, bool>> _expression;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkConditionAdapter class.
        /// </summary>
        /// <param name="expression">The LINQ expression</param>
        public EntityFrameworkConditionAdapter(Expression<Func<T, bool>> expression)
        {
            _expression = expression ?? throw new ArgumentNullException(nameof(expression));
        }

        /// <summary>
        /// Gets the underlying LINQ expression.
        /// </summary>
        /// <returns>The LINQ expression</returns>
        public Expression<Func<T, bool>> GetExpression()
        {
            return _expression;
        }

        /// <summary>
        /// Creates a new condition representing the logical AND of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this AND other)</returns>
        public ICondition And(ICondition other)
        {
            if (!(other is EntityFrameworkConditionAdapter<T> otherEf))
            {
                throw new ArgumentException("Cannot combine with non-Entity Framework condition", nameof(other));
            }

            var combinedExpression = CombineExpressions(_expression, otherEf._expression, ExpressionType.AndAlso);
            return new EntityFrameworkConditionAdapter<T>(combinedExpression);
        }

        /// <summary>
        /// Creates a new condition representing the logical OR of this condition and another.
        /// </summary>
        /// <param name="other">The other condition to combine with</param>
        /// <returns>A new condition representing (this OR other)</returns>
        public ICondition Or(ICondition other)
        {
            if (!(other is EntityFrameworkConditionAdapter<T> otherEf))
            {
                throw new ArgumentException("Cannot combine with non-Entity Framework condition", nameof(other));
            }

            var combinedExpression = CombineExpressions(_expression, otherEf._expression, ExpressionType.OrElse);
            return new EntityFrameworkConditionAdapter<T>(combinedExpression);
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
            return new EntityFrameworkConditionAdapter<T>(negatedExpression);
        }

        private static Expression<Func<T, bool>> CombineExpressions(
            Expression<Func<T, bool>> left,
            Expression<Func<T, bool>> right,
            ExpressionType expressionType)
        {
            var parameter = Expression.Parameter(typeof(T), "x");
            var leftBody = ReplaceParameter(left.Body, left.Parameters[0], parameter);
            var rightBody = ReplaceParameter(right.Body, right.Parameters[0], parameter);
            var combinedBody = Expression.MakeBinary(expressionType, leftBody, rightBody);
            return Expression.Lambda<Func<T, bool>>(combinedBody, parameter);
        }

        private static Expression ReplaceParameter(Expression expression, ParameterExpression oldParameter, ParameterExpression newParameter)
        {
            return new ParameterReplacer(oldParameter, newParameter).Visit(expression);
        }

        private class ParameterReplacer : ExpressionVisitor
        {
            private readonly ParameterExpression _oldParameter;
            private readonly ParameterExpression _newParameter;

            public ParameterReplacer(ParameterExpression oldParameter, ParameterExpression newParameter)
            {
                _oldParameter = oldParameter;
                _newParameter = newParameter;
            }

            protected override Expression VisitParameter(ParameterExpression node)
            {
                return node == _oldParameter ? _newParameter : base.VisitParameter(node);
            }
        }
    }
}
