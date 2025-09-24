using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Reflection;
using DynamicFilter.Core.Interfaces;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.EntityFramework.Adapters
{
    /// <summary>
    /// Entity Framework implementation of the IContext interface.
    /// This adapter creates LINQ expressions from filter definitions.
    /// </summary>
    /// <typeparam name="T">The entity type</typeparam>
    public class EntityFrameworkContextAdapter<T> : IContext
    {
        private readonly Dictionary<string, FilterDefinition> _filters;
        private readonly PropertyRegistry _propertyRegistry;

        /// <summary>
        /// Initializes a new instance of the EntityFrameworkContextAdapter class.
        /// </summary>
        /// <param name="filters">The filter definitions</param>
        /// <param name="propertyRegistry">The property registry</param>
        public EntityFrameworkContextAdapter(
            Dictionary<string, FilterDefinition> filters,
            PropertyRegistry propertyRegistry)
        {
            _filters = filters ?? throw new ArgumentNullException(nameof(filters));
            _propertyRegistry = propertyRegistry ?? throw new ArgumentNullException(nameof(propertyRegistry));
        }

        /// <summary>
        /// Retrieves the condition associated with the given filter key.
        /// </summary>
        /// <param name="filterKey">The unique identifier for the filter</param>
        /// <returns>The condition associated with the filter key, or null if not found</returns>
        public ICondition? GetCondition(string filterKey)
        {
            if (!_filters.TryGetValue(filterKey, out var filter))
            {
                throw new ArgumentException($"No filter found for key: {filterKey}", nameof(filterKey));
            }

            return CreateCondition(filter);
        }

        private ICondition CreateCondition(FilterDefinition filter)
        {
            // Validate property reference
            var propertyRef = _propertyRegistry.GetProperty(filter.Ref);
            if (propertyRef == null)
            {
                throw new ArgumentException($"Property not found: {filter.Ref}");
            }

            // Validate operator
            var operatorEnum = OperatorExtensions.FromString(filter.Operator);
            if (operatorEnum == null)
            {
                throw new ArgumentException($"Invalid operator: {filter.Operator}");
            }

            // Create LINQ expression
            var expression = CreateExpression(filter.Ref, operatorEnum.Value, filter.Value, propertyRef.Type);

            return new EntityFrameworkCondition<T>(expression);
        }

        private Expression<Func<T, bool>> CreateExpression(string propertyRef, Operator operatorEnum, object? value, Type expectedType)
        {
            var parameter = Expression.Parameter(typeof(T), "x");
            var property = GetPropertyExpression(parameter, propertyRef);

            Expression condition = operatorEnum switch
            {
                Operator.Equals => Expression.Equal(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.NotEquals => Expression.NotEqual(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.GreaterThan => Expression.GreaterThan(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.GreaterThanOrEqual => Expression.GreaterThanOrEqual(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.LessThan => Expression.LessThan(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.LessThanOrEqual => Expression.LessThanOrEqual(property, Expression.Constant(ConvertValue(value, expectedType))),
                Operator.Like => CreateLikeExpression(property, value),
                Operator.NotLike => Expression.Not(CreateLikeExpression(property, value)),
                Operator.In => CreateInExpression(property, value),
                Operator.NotIn => Expression.Not(CreateInExpression(property, value)),
                Operator.IsNull => Expression.Equal(property, Expression.Constant(null)),
                Operator.IsNotNull => Expression.NotEqual(property, Expression.Constant(null)),
                Operator.Between => CreateBetweenExpression(property, value),
                Operator.NotBetween => Expression.Not(CreateBetweenExpression(property, value)),
                _ => throw new ArgumentException($"Unsupported operator: {operatorEnum}")
            };

            return Expression.Lambda<Func<T, bool>>(condition, parameter);
        }

        private Expression GetPropertyExpression(ParameterExpression parameter, string propertyRef)
        {
            var parts = propertyRef.Split('.');
            Expression expression = parameter;

            foreach (var part in parts)
            {
                var property = typeof(T).GetProperty(part, BindingFlags.Public | BindingFlags.Instance | BindingFlags.IgnoreCase);
                if (property == null)
                {
                    throw new ArgumentException($"Property '{part}' not found on type '{typeof(T).Name}'");
                }
                expression = Expression.Property(expression, property);
            }

            return expression;
        }

        private Expression CreateLikeExpression(Expression property, object? value)
        {
            // For string properties, use Contains method
            var containsMethod = typeof(string).GetMethod("Contains", new[] { typeof(string) });
            if (containsMethod == null)
            {
                throw new InvalidOperationException("String.Contains method not found");
            }

            return Expression.Call(property, containsMethod, Expression.Constant(value?.ToString()));
        }

        private Expression CreateInExpression(Expression property, object? value)
        {
            if (value is IEnumerable<object> enumerable)
            {
                var containsMethod = typeof(Enumerable).GetMethods()
                    .First(m => m.Name == "Contains" && m.GetParameters().Length == 2)
                    .MakeGenericMethod(property.Type);

                return Expression.Call(containsMethod, Expression.Constant(enumerable), property);
            }
            else
            {
                var equalsMethod = typeof(object).GetMethod("Equals", new[] { typeof(object) });
                return Expression.Call(Expression.Constant(value), equalsMethod, property);
            }
        }

        private Expression CreateBetweenExpression(Expression property, object? value)
        {
            if (value is IEnumerable<object> enumerable && enumerable.Count() == 2)
            {
                var values = enumerable.ToArray();
                var from = Expression.Constant(ConvertValue(values[0], property.Type));
                var to = Expression.Constant(ConvertValue(values[1], property.Type));

                return Expression.AndAlso(
                    Expression.GreaterThanOrEqual(property, from),
                    Expression.LessThanOrEqual(property, to));
            }
            else
            {
                throw new ArgumentException("BETWEEN operator requires exactly 2 values");
            }
        }

        private object? ConvertValue(object? value, Type expectedType)
        {
            if (value == null)
            {
                return null;
            }

            if (expectedType.IsAssignableFrom(value.GetType()))
            {
                return value;
            }

            // Handle string to other type conversions
            if (value is string stringValue)
            {
                if (expectedType == typeof(string))
                {
                    return stringValue;
                }
                else if (expectedType == typeof(int) || expectedType == typeof(int?))
                {
                    return int.Parse(stringValue);
                }
                else if (expectedType == typeof(long) || expectedType == typeof(long?))
                {
                    return long.Parse(stringValue);
                }
                else if (expectedType == typeof(double) || expectedType == typeof(double?))
                {
                    return double.Parse(stringValue);
                }
                else if (expectedType == typeof(bool) || expectedType == typeof(bool?))
                {
                    return bool.Parse(stringValue);
                }
                else if (expectedType == typeof(DateTime) || expectedType == typeof(DateTime?))
                {
                    return DateTime.Parse(stringValue);
                }
                else if (expectedType.IsEnum)
                {
                    return Enum.Parse(expectedType, stringValue);
                }
            }

            throw new ArgumentException($"Cannot convert value {value} to type {expectedType}");
        }
    }
}
