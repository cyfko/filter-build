using System;
using System.Collections.Generic;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;
using DynamicFilter.Core.TestTypes;
using DynamicFilter.Core.Interfaces;

namespace DynamicFilter.Core
{
    /// <summary>
    /// Example usage of the new C# architecture
    /// </summary>
    public class UsageExample
    {
        /// <summary>
        /// Demonstrates the new type-safe architecture
        /// </summary>
        public static void DemonstrateArchitecture()
        {
            // Example 1: Creating a FilterDefinition
            var nameFilter = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserName,
                Operator.Like,
                "John%"
            );

            var ageFilter = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserAge,
                Operator.GreaterThan,
                18
            );

            // Example 2: Creating a FilterRequest
            var filterRequest = new FilterRequest<TestPropertyRef>(
                new Dictionary<string, FilterDefinition<TestPropertyRef>>
                {
                    { "nameFilter", nameFilter },
                    { "ageFilter", ageFilter }
                },
                "AND"
            );

            // Example 3: Type safety validation
            ValidateFilter(nameFilter);
            ValidateFilter(ageFilter);

            Console.WriteLine("✅ C# validation successful!");
        }

        /// <summary>
        /// Validates that the filter definition is type-safe
        /// </summary>
        /// <param name="definition">The filter definition to validate</param>
        private static void ValidateFilter(FilterDefinition<TestPropertyRef> definition)
        {
            // Get the property ref implementation based on the enum value
            IPropertyRef propertyRef = definition.Ref switch
            {
                TestPropertyRef.UserName => TestPropertyRefImpl.UserName,
                TestPropertyRef.UserAge => TestPropertyRefImpl.UserAge,
                TestPropertyRef.UserEmail => TestPropertyRefImpl.UserEmail,
                TestPropertyRef.UserStatus => TestPropertyRefImpl.UserStatus,
                _ => throw new ArgumentException($"Unknown property: {definition.Ref}")
            };
            
            if (!propertyRef.SupportsOperator(definition.Operator))
            {
                throw new ArgumentException(
                    $"Operator {definition.Operator} not supported for property {definition.Ref}"
                );
            }
        }

        /// <summary>
        /// Example ContextAdapter usage
        /// </summary>
        public class ExampleContextAdapter : IContextAdapter<object, TestPropertyRef>
        {
            private readonly Dictionary<string, FilterDefinition<TestPropertyRef>> _conditions = new();

            public void AddCondition(string filterKey, FilterDefinition<TestPropertyRef> definition)
            {
                // Validate the filter
                ValidateFilter(definition);
                
                // Store the condition (simplified)
                _conditions[filterKey] = definition;
            }

            public ICondition? GetCondition(string filterKey)
            {
                return _conditions.TryGetValue(filterKey, out var definition) ? null : null; // Simplified
            }
        }

        /// <summary>
        /// Main entry point
        /// </summary>
        public static void Main()
        {
            try
            {
                DemonstrateArchitecture();
                Console.WriteLine("C# core architecture validation completed!");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"❌ C# validation failed: {ex.Message}");
            }
        }
    }
}

