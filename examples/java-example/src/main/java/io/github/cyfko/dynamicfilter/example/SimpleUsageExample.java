package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

/**
 * Example showing how simple it is to use the PropertyRef enum approach.
 * Developers only need to extend PropertyRef and call super() - that's it!
 */
public class SimpleUsageExample {
    
    public static void main(String[] args) {
        // Using UserPropertyRef - all methods are inherited from PropertyRef!
        System.out.println("=== User Properties ===");
        demonstratePropertyRef(UserPropertyRef.USER_NAME);
        demonstratePropertyRef(UserPropertyRef.USER_AGE);
        demonstratePropertyRef(UserPropertyRef.USER_STATUS);
        
        System.out.println("\n=== Product Properties ===");
        demonstratePropertyRef(ProductPropertyRef.PRODUCT_NAME);
        demonstratePropertyRef(ProductPropertyRef.PRODUCT_PRICE);
        demonstratePropertyRef(ProductPropertyRef.PRODUCT_IN_STOCK);
        
        // Test operator validation
        System.out.println("\n=== Operator Validation ===");
        testOperatorValidation();
    }
    
    private static void demonstratePropertyRef(PropertyRef propertyRef) {
        System.out.println("Property: " + propertyRef);
        System.out.println("  Entity Field: " + propertyRef.getEntityField());
        System.out.println("  Type: " + propertyRef.getType().getSimpleName());
        System.out.println("  Supports LIKE: " + propertyRef.supportsOperator(Operator.LIKE));
        System.out.println("  Supports >: " + propertyRef.supportsOperator(Operator.GREATER_THAN));
        System.out.println("  Description: " + propertyRef.getDescription());
        System.out.println();
    }
    
    private static void testOperatorValidation() {
        // Valid operations
        System.out.println("Testing valid operations:");
        assertDoesNotThrow(() -> UserPropertyRef.USER_NAME.validateOperator(Operator.LIKE));
        assertDoesNotThrow(() -> UserPropertyRef.USER_AGE.validateOperator(Operator.GREATER_THAN));
        assertDoesNotThrow(() -> ProductPropertyRef.PRODUCT_PRICE.validateOperator(Operator.BETWEEN));
        System.out.println("✓ All valid operations passed");
        
        // Invalid operations
        System.out.println("Testing invalid operations:");
        try {
            UserPropertyRef.USER_NAME.validateOperator(Operator.GREATER_THAN);
            System.out.println("✗ Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly rejected: " + e.getMessage());
        }
        
        try {
            UserPropertyRef.USER_AGE.validateOperator(Operator.LIKE);
            System.out.println("✗ Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly rejected: " + e.getMessage());
        }
    }
    
    private static void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Expected no exception, but got: " + e.getMessage(), e);
        }
    }
}
