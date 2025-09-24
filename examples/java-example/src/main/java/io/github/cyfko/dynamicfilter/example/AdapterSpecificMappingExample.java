package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;

/**
 * Example showing how to configure adapter-specific entity field mappings.
 * This demonstrates the separation between logical properties (PropertyRef) 
 * and physical database fields (mapped per adapter).
 */
public class AdapterSpecificMappingExample {
    
    public static void main(String[] args) {
        System.out.println("=== Adapter-Specific Entity Field Mapping ===");
        
        // Create registry and register properties
        PropertyRegistry registry = new PropertyRegistry();
        registry.registerAll(UserPropertyRef.class);
        
        // Configure mappings for different adapters
        configureJpaMappings(registry);
        configurePrismaMappings(registry);
        configureSqlAlchemyMappings(registry);
        
        // Demonstrate usage
        demonstrateUsage(registry);
    }
    
    /**
     * Configure entity field mappings for JPA adapter.
     * JPA typically uses camelCase field names.
     */
    private static void configureJpaMappings(PropertyRegistry registry) {
        System.out.println("\n--- JPA Adapter Mappings ---");
        
        registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "userName");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "status");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "createdDate");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "active");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary");
        
        System.out.println("JPA mappings configured:");
        registry.getPropertyToEntityFieldMapping().forEach((prop, field) -> 
            System.out.println("  " + prop.name() + " -> " + field)
        );
    }
    
    /**
     * Configure entity field mappings for Prisma adapter.
     * Prisma typically uses snake_case field names.
     */
    private static void configurePrismaMappings(PropertyRegistry registry) {
        System.out.println("\n--- Prisma Adapter Mappings ---");
        
        // Clear existing mappings for Prisma
        registry.clear();
        registry.registerAll(UserPropertyRef.class);
        
        registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "user_name");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "status");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "created_date");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "is_active");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary");
        
        System.out.println("Prisma mappings configured:");
        registry.getPropertyToEntityFieldMapping().forEach((prop, field) -> 
            System.out.println("  " + prop.name() + " -> " + field)
        );
    }
    
    /**
     * Configure entity field mappings for SQLAlchemy adapter.
     * SQLAlchemy might use different naming conventions.
     */
    private static void configureSqlAlchemyMappings(PropertyRegistry registry) {
        System.out.println("\n--- SQLAlchemy Adapter Mappings ---");
        
        // Clear existing mappings for SQLAlchemy
        registry.clear();
        registry.registerAll(UserPropertyRef.class);
        
        registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "user_name");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email_address");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "user_status");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "created_at");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "is_active");
        registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary_amount");
        
        System.out.println("SQLAlchemy mappings configured:");
        registry.getPropertyToEntityFieldMapping().forEach((prop, field) -> 
            System.out.println("  " + prop.name() + " -> " + field)
        );
    }
    
    /**
     * Demonstrate how the mappings work in practice.
     */
    private static void demonstrateUsage(PropertyRegistry registry) {
        System.out.println("\n--- Usage Example ---");
        
        // Simulate a filter request
        String filterRef = "USER_NAME";
        String operator = "LIKE";
        String value = "Smith";
        
        // Find the PropertyRef by name
        PropertyRef propertyRef = registry.getAllProperties().stream()
                .filter(prop -> prop.name().equals(filterRef))
                .findFirst()
                .orElse(null);
        
        if (propertyRef != null) {
            System.out.println("Filter: " + filterRef + " " + operator + " '" + value + "'");
            System.out.println("PropertyRef: " + propertyRef);
            System.out.println("Type: " + propertyRef.getType().getSimpleName());
            System.out.println("Supports " + operator + ": " + propertyRef.supportsOperator(Operator.fromString(operator)));
            
            // Get the entity field for the current adapter
            String entityField = registry.getEntityField(propertyRef);
            System.out.println("Entity field: " + entityField);
            System.out.println("SQL: WHERE " + entityField + " LIKE '%" + value + "%'");
        }
    }
    
    /**
     * Example of how different adapters would configure their mappings.
     */
    public static class AdapterConfigurationExamples {
        
        /**
         * JPA adapter configuration
         */
        public static PropertyRegistry createJpaRegistry() {
            PropertyRegistry registry = new PropertyRegistry();
            registry.registerAll(UserPropertyRef.class);
            
            // JPA uses camelCase
            registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "userName");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "status");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "createdDate");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "active");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary");
            
            return registry;
        }
        
        /**
         * Prisma adapter configuration
         */
        public static PropertyRegistry createPrismaRegistry() {
            PropertyRegistry registry = new PropertyRegistry();
            registry.registerAll(UserPropertyRef.class);
            
            // Prisma uses snake_case
            registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "user_name");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "status");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "created_date");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "is_active");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary");
            
            return registry;
        }
        
        /**
         * SQLAlchemy adapter configuration
         */
        public static PropertyRegistry createSqlAlchemyRegistry() {
            PropertyRegistry registry = new PropertyRegistry();
            registry.registerAll(UserPropertyRef.class);
            
            // SQLAlchemy might use different conventions
            registry.mapPropertyToEntityField(UserPropertyRef.USER_NAME, "user_name");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_EMAIL, "email_address");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_STATUS, "user_status");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_AGE, "age");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_CREATED_DATE, "created_at");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_IS_ACTIVE, "is_active");
            registry.mapPropertyToEntityField(UserPropertyRef.USER_SALARY, "salary_amount");
            
            return registry;
        }
    }
}
