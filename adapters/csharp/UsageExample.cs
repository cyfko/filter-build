using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;
using DynamicFilter.Core.TestTypes;
using DynamicFilter.Core.Interfaces;
using DynamicFilter.EntityFramework.Adapters;

namespace DynamicFilter.EntityFramework
{
    /// <summary>
    /// Example usage of C# adapters with the new type-safe architecture
    /// </summary>
    public class UsageExample
    {
        /// <summary>
        /// Example entity type
        /// </summary>
        public class User
        {
            public int Id { get; set; }
            public string Name { get; set; } = string.Empty;
            public int Age { get; set; }
            public string Email { get; set; } = string.Empty;
            public string Status { get; set; } = string.Empty;
        }

        /// <summary>
        /// Example Entity Framework condition adapter builder implementation
        /// </summary>
        public class ExampleEntityFrameworkConditionAdapterBuilder : IEntityFrameworkConditionAdapterBuilder<User, TestPropertyRef>
        {
            public EntityFrameworkConditionAdapter<User> Build(TestPropertyRef @ref, Operator op, object? value)
            {
                Expression<Func<User, bool>> expression = @ref switch
                {
                    TestPropertyRef.UserName => op switch
                    {
                        Operator.Equals => u => u.Name == (string)value!,
                        Operator.Like => u => u.Name.Contains((string)value!),
                        _ => throw new ArgumentException($"Unsupported operator: {op}")
                    },
                    TestPropertyRef.UserAge => op switch
                    {
                        Operator.Equals => u => u.Age == (int)value!,
                        Operator.GreaterThan => u => u.Age > (int)value!,
                        Operator.LessThan => u => u.Age < (int)value!,
                        _ => throw new ArgumentException($"Unsupported operator: {op}")
                    },
                    TestPropertyRef.UserEmail => op switch
                    {
                        Operator.Equals => u => u.Email == (string)value!,
                        Operator.Like => u => u.Email.Contains((string)value!),
                        _ => throw new ArgumentException($"Unsupported operator: {op}")
                    },
                    TestPropertyRef.UserStatus => op switch
                    {
                        Operator.Equals => u => u.Status == (string)value!,
                        Operator.NotEquals => u => u.Status != (string)value!,
                        _ => throw new ArgumentException($"Unsupported operator: {op}")
                    },
                    _ => throw new ArgumentException($"Unsupported PropertyRef: {@ref}")
                };

                return new EntityFrameworkConditionAdapter<User>(expression);
            }
        }

        /// <summary>
        /// Demonstrates the new type-safe architecture
        /// </summary>
        public static void DemonstrateAdapters()
        {
            // Create filter definitions
            var nameFilter = new FilterDefinition<TestPropertyRef>(
                ref: TestPropertyRef.UserName,
                @operator: Operator.Like,
                value: "John%"
            );

            var ageFilter = new FilterDefinition<TestPropertyRef>(
                ref: TestPropertyRef.UserAge,
                @operator: Operator.GreaterThan,
                value: 18
            );

            // Entity Framework adapter usage
            var efBuilder = new ExampleEntityFrameworkConditionAdapterBuilder();
            var efAdapter = new EntityFrameworkContextAdapter<User, TestPropertyRef>(efBuilder);

            efAdapter.AddCondition("nameFilter", nameFilter);
            efAdapter.AddCondition("ageFilter", ageFilter);

            Console.WriteLine("✅ C# adapters validation successful!");
        }

        /// <summary>
        /// Main entry point
        /// </summary>
        public static void Main()
        {
            try
            {
                DemonstrateAdapters();
                Console.WriteLine("C# adapters architecture validation completed!");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"❌ C# validation failed: {ex.Message}");
            }
        }
    }
}

