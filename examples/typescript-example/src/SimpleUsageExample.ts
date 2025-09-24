import { PropertyRegistry, Operator } from '@cyfko/dynamic-filter-core';
import { UserPropertyRef } from './UserPropertyRef';

/**
 * Example showing how simple it is to use the PropertyRef class approach.
 * Developers only need to extend PropertyRef and call super() - that's it!
 */
export class SimpleUsageExample {
    
    public static main(): void {
        // Using UserPropertyRef - all methods are inherited from PropertyRef!
        console.log("=== User Properties ===");
        this.demonstratePropertyRef(UserPropertyRef.USER_NAME);
        this.demonstratePropertyRef(UserPropertyRef.USER_AGE);
        this.demonstratePropertyRef(UserPropertyRef.USER_STATUS);
        
        console.log("\n=== Product Properties ===");
        // You can create other property refs similarly
        this.demonstratePropertyRef(UserPropertyRef.USER_NAME);
        this.demonstratePropertyRef(UserPropertyRef.USER_AGE);
        this.demonstratePropertyRef(UserPropertyRef.USER_IS_ACTIVE);
        
        // Test operator validation
        console.log("\n=== Operator Validation ===");
        this.testOperatorValidation();
        
        // Test PropertyRegistry
        console.log("\n=== PropertyRegistry Usage ===");
        this.testPropertyRegistry();
    }
    
    private static demonstratePropertyRef(propertyRef: UserPropertyRef): void {
        console.log(`Property: ${propertyRef}`);
        console.log(`  Entity Field: ${propertyRef.entityField}`);
        console.log(`  Type: ${propertyRef.type}`);
        console.log(`  Supports LIKE: ${propertyRef.supportsOperator(Operator.LIKE)}`);
        console.log(`  Supports >: ${propertyRef.supportsOperator(Operator.GREATER_THAN)}`);
        console.log(`  Description: ${propertyRef.getDescription()}`);
        console.log();
    }
    
    private static testOperatorValidation(): void {
        // Valid operations
        console.log("Testing valid operations:");
        this.assertDoesNotThrow(() => UserPropertyRef.USER_NAME.validateOperator(Operator.LIKE));
        this.assertDoesNotThrow(() => UserPropertyRef.USER_AGE.validateOperator(Operator.GREATER_THAN));
        this.assertDoesNotThrow(() => UserPropertyRef.USER_SALARY.validateOperator(Operator.BETWEEN));
        console.log("✓ All valid operations passed");
        
        // Invalid operations
        console.log("Testing invalid operations:");
        try {
            UserPropertyRef.USER_NAME.validateOperator(Operator.GREATER_THAN);
            console.log("✗ Should have thrown exception");
        } catch (error) {
            console.log("✓ Correctly rejected:", error.message);
        }
        
        try {
            UserPropertyRef.USER_AGE.validateOperator(Operator.LIKE);
            console.log("✗ Should have thrown exception");
        } catch (error) {
            console.log("✓ Correctly rejected:", error.message);
        }
    }
    
    private static testPropertyRegistry(): void {
        const registry = new PropertyRegistry();
        
        // Register all properties from UserPropertyRef
        registry.registerAll(UserPropertyRef);
        
        console.log(`Registered ${registry.size()} properties`);
        console.log("Property names:", registry.getPropertyNames());
        
        // Test property lookup
        const userNameProp = registry.getProperty("userName");
        console.log("Found userName property:", userNameProp?.getDescription());
        
        // Test operator validation through registry
        try {
            registry.validatePropertyOperator("userName", Operator.LIKE);
            console.log("✓ Registry validation passed for userName + LIKE");
        } catch (error) {
            console.log("✗ Registry validation failed:", error.message);
        }
        
        try {
            registry.validatePropertyOperator("userName", Operator.GREATER_THAN);
            console.log("✗ Should have failed for userName + GREATER_THAN");
        } catch (error) {
            console.log("✓ Registry correctly rejected userName + GREATER_THAN");
        }
    }
    
    private static assertDoesNotThrow(fn: () => void): void {
        try {
            fn();
        } catch (error) {
            throw new Error(`Expected no exception, but got: ${error.message}`);
        }
    }
}

// Run the example
SimpleUsageExample.main();
