using System;
using System.Linq;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Example
{
    /// <summary>
    /// Example showing how simple it is to use the PropertyRef class approach.
    /// Developers only need to extend PropertyRef and call super() - that's it!
    /// </summary>
    public class SimpleUsageExample
    {
        public static void Main()
        {
            // Using UserPropertyRef - all methods are inherited from PropertyRef!
            Console.WriteLine("=== User Properties ===");
            DemonstratePropertyRef(UserPropertyRef.UserName);
            DemonstratePropertyRef(UserPropertyRef.UserAge);
            DemonstratePropertyRef(UserPropertyRef.UserStatus);
            
            Console.WriteLine("\n=== Product Properties ===");
            // You can create other property refs similarly
            DemonstratePropertyRef(UserPropertyRef.UserName);
            DemonstratePropertyRef(UserPropertyRef.UserAge);
            DemonstratePropertyRef(UserPropertyRef.UserIsActive);
            
            // Test operator validation
            Console.WriteLine("\n=== Operator Validation ===");
            TestOperatorValidation();
            
            // Test PropertyRegistry
            Console.WriteLine("\n=== PropertyRegistry Usage ===");
            TestPropertyRegistry();
        }
        
        private static void DemonstratePropertyRef(UserPropertyRef propertyRef)
        {
            Console.WriteLine($"Property: {propertyRef}");
            Console.WriteLine($"  Entity Field: {propertyRef.EntityField}");
            Console.WriteLine($"  Type: {propertyRef.Type}");
            Console.WriteLine($"  Supports LIKE: {propertyRef.SupportsOperator(Operator.Like)}");
            Console.WriteLine($"  Supports >: {propertyRef.SupportsOperator(Operator.GreaterThan)}");
            Console.WriteLine($"  Description: {propertyRef.GetDescription()}");
            Console.WriteLine();
        }
        
        private static void TestOperatorValidation()
        {
            // Valid operations
            Console.WriteLine("Testing valid operations:");
            AssertDoesNotThrow(() => UserPropertyRef.UserName.ValidateOperator(Operator.Like));
            AssertDoesNotThrow(() => UserPropertyRef.UserAge.ValidateOperator(Operator.GreaterThan));
            AssertDoesNotThrow(() => UserPropertyRef.UserSalary.ValidateOperator(Operator.Between));
            Console.WriteLine("✓ All valid operations passed");
            
            // Invalid operations
            Console.WriteLine("Testing invalid operations:");
            try
            {
                UserPropertyRef.UserName.ValidateOperator(Operator.GreaterThan);
                Console.WriteLine("✗ Should have thrown exception");
            }
            catch (ArgumentException error)
            {
                Console.WriteLine($"✓ Correctly rejected: {error.Message}");
            }
            
            try
            {
                UserPropertyRef.UserAge.ValidateOperator(Operator.Like);
                Console.WriteLine("✗ Should have thrown exception");
            }
            catch (ArgumentException error)
            {
                Console.WriteLine($"✓ Correctly rejected: {error.Message}");
            }
        }
        
        private static void TestPropertyRegistry()
        {
            var registry = new PropertyRegistry();
            
            // Register all properties from UserPropertyRef
            registry.RegisterAll(typeof(UserPropertyRef));
            
            Console.WriteLine($"Registered {registry.Count} properties");
            Console.WriteLine($"Property names: {string.Join(", ", registry.GetPropertyNames())}");
            
            // Test property lookup
            var userNameProp = registry.GetProperty("userName");
            Console.WriteLine($"Found userName property: {userNameProp?.GetDescription() ?? "Not found"}");
            
            // Test operator validation through registry
            try
            {
                registry.ValidatePropertyOperator("userName", Operator.Like);
                Console.WriteLine("✓ Registry validation passed for userName + LIKE");
            }
            catch (ArgumentException error)
            {
                Console.WriteLine($"✗ Registry validation failed: {error.Message}");
            }
            
            try
            {
                registry.ValidatePropertyOperator("userName", Operator.GreaterThan);
                Console.WriteLine("✗ Should have failed for userName + GREATER_THAN");
            }
            catch (ArgumentException error)
            {
                Console.WriteLine($"✓ Registry correctly rejected userName + GREATER_THAN");
            }
        }
        
        private static void AssertDoesNotThrow(Action action)
        {
            try
            {
                action();
            }
            catch (Exception error)
            {
                throw new InvalidOperationException($"Expected no exception, but got: {error.Message}", error);
            }
        }
    }
}
