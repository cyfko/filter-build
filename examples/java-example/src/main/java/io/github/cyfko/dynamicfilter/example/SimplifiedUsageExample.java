package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;

/**
 * Example showing the simplified PropertyRegistry approach.
 * The registry now maps PropertyRef instances to entity field names,
 * providing a clean separation between filter references and database fields.
 */
public class SimplifiedUsageExample {
    
    public static void main(String[] args) {
        // Create registry
        PropertyRegistry registry = new PropertyRegistry();
        
        // Register all properties from UserPropertyRef
        registry.registerAll(UserPropertyRef.class);
        
        System.out.println("=== Simplified PropertyRegistry Usage ===");
        demonstrateRegistryUsage(registry);
        
        System.out.println("\n=== Type-Safe Property Access ===");
        demonstrateTypeSafeAccess(registry);
        
        System.out.println("\n=== Entity Field Mapping ===");
        demonstrateEntityFieldMapping(registry);
    }
    
    private static void demonstrateRegistryUsage(PropertyRegistry registry) {
        System.out.println("Registered " + registry.size() + " properties");
        System.out.println("All properties: " + registry.getAllProperties());
        System.out.println("All entity fields: " + registry.getAllEntityFields());
        
        // Check if specific properties are registered
        System.out.println("USER_NAME registered: " + registry.hasProperty(UserPropertyRef.USER_NAME));
        System.out.println("USER_AGE registered: " + registry.hasProperty(UserPropertyRef.USER_AGE));
        System.out.println("Non-existent registered: " + registry.hasProperty(UserPropertyRef.USER_NAME)); // This will be true
    }
    
    private static void demonstrateTypeSafeAccess(PropertyRegistry registry) {
        // Type-safe property access
        PropertyRef userNameProp = UserPropertyRef.USER_NAME;
        PropertyRef userAgeProp = UserPropertyRef.USER_AGE;
        
        System.out.println("User Name Property:");
        System.out.println("  Enum Name: " + userNameProp.name());
        System.out.println("  Entity Field: " + userNameProp.getEntityField());
        System.out.println("  Type: " + userNameProp.getType().getSimpleName());
        System.out.println("  Supports LIKE: " + userNameProp.supportsOperator(Operator.LIKE));
        System.out.println("  Supports >: " + userNameProp.supportsOperator(Operator.GREATER_THAN));
        
        System.out.println("\nUser Age Property:");
        System.out.println("  Enum Name: " + userAgeProp.name());
        System.out.println("  Entity Field: " + userAgeProp.getEntityField());
        System.out.println("  Type: " + userAgeProp.getType().getSimpleName());
        System.out.println("  Supports LIKE: " + userAgeProp.supportsOperator(Operator.LIKE));
        System.out.println("  Supports >: " + userAgeProp.supportsOperator(Operator.GREATER_THAN));
    }
    
    private static void demonstrateEntityFieldMapping(PropertyRegistry registry) {
        // Get entity field for a PropertyRef
        String userNameField = registry.getEntityField(UserPropertyRef.USER_NAME);
        String userAgeField = registry.getEntityField(UserPropertyRef.USER_AGE);
        
        System.out.println("Entity Field Mappings:");
        System.out.println("  USER_NAME -> " + userNameField);
        System.out.println("  USER_AGE -> " + userAgeField);
        
        // Get PropertyRef by entity field
        PropertyRef foundByName = registry.getPropertyByEntityField("userName");
        PropertyRef foundByAge = registry.getPropertyByEntityField("age");
        
        System.out.println("\nReverse Lookups:");
        System.out.println("  'userName' -> " + foundByName);
        System.out.println("  'age' -> " + foundByAge);
        
        // Show the complete mapping
        System.out.println("\nComplete PropertyRef -> EntityField Mapping:");
        registry.getPropertyToEntityFieldMapping().forEach((prop, field) -> 
            System.out.println("  " + prop.name() + " -> " + field)
        );
    }
    
    /**
     * Example of how the registry would be used in a filter context.
     */
    public static void demonstrateFilterUsage(PropertyRegistry registry) {
        System.out.println("\n=== Filter Usage Example ===");
        
        // Simulate a filter request
        String filterRef = "USER_NAME";  // This comes from JSON
        String operator = "LIKE";        // This comes from JSON
        String value = "Smith";          // This comes from JSON
        
        // Find the PropertyRef by name
        PropertyRef propertyRef = registry.getAllProperties().stream()
                .filter(prop -> prop.name().equals(filterRef))
                .findFirst()
                .orElse(null);
        
        if (propertyRef != null) {
            System.out.println("Found property: " + propertyRef);
            
            // Get the entity field name for database operations
            String entityField = registry.getEntityField(propertyRef);
            System.out.println("Entity field: " + entityField);
            
            // Validate operator
            Operator op = Operator.fromString(operator);
            if (op != null) {
                try {
                    propertyRef.validateOperator(op);
                    System.out.println("✓ Operator " + op + " is valid for " + propertyRef.name());
                } catch (IllegalArgumentException e) {
                    System.out.println("✗ " + e.getMessage());
                }
            }
        } else {
            System.out.println("✗ Property not found: " + filterRef);
        }
    }
}
