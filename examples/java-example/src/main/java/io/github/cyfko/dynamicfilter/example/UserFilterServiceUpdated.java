package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.impl.DSLParser;
import io.github.cyfko.dynamicfilter.core.model.FilterRequest;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;
import io.github.cyfko.dynamicfilter.jpa.JpaFilterService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Example service demonstrating how to use the dynamic filtering system with JPA
 * using the new PropertyRef enum approach.
 */
public class UserFilterServiceUpdated {
    
    private final JpaFilterService filterService;
    
    public UserFilterServiceUpdated(EntityManager entityManager) {
        // Initialize core components
        var parser = new DSLParser();
        var propertyRegistry = createPropertyRegistry();
        
        // Initialize JPA filter service
        this.filterService = new JpaFilterService(parser, propertyRegistry, entityManager);
    }
    
    /**
     * Creates a property registry with the new PropertyRef interface approach.
     * This is much cleaner and type-safe!
     */
    private PropertyRegistry createPropertyRegistry() {
        var registry = new PropertyRegistry();
        
        // Register all PropertyRef enum values from UserPropertyRef
        registry.registerAll(UserPropertyRef.class);
        
        // Or register specific ones if you want to be selective
        // registry.registerProperties(
        //     UserPropertyRef.USER_NAME,
        //     UserPropertyRef.USER_EMAIL,
        //     UserPropertyRef.USER_STATUS,
        //     UserPropertyRef.USER_AGE,
        //     UserPropertyRef.USER_CREATED_DATE
        // );
        
        return registry;
    }
    
    /**
     * Example: Find users with name containing "Smith" and status "ACTIVE".
     * Now uses the PropertyRef enum names instead of arbitrary strings.
     */
    public List<User> findActiveUsersWithName(String namePattern) {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_NAME", "LIKE", namePattern),
                "f2", new FilterDefinition("USER_STATUS", "=", "ACTIVE")
            ),
            "f1 & f2"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: Find users created after a specific date OR with age greater than 25.
     * The system will automatically validate that USER_CREATED_DATE supports >= operator
     * and USER_AGE supports > operator.
     */
    public List<User> findUsersByDateOrAge(LocalDateTime afterDate, Integer minAge) {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_CREATED_DATE", ">=", afterDate.toString()),
                "f2", new FilterDefinition("USER_AGE", ">", minAge)
            ),
            "f1 | f2"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: Complex filter with parentheses and NOT operator.
     * All property references and operators are validated at runtime.
     */
    public List<User> findComplexFilter() {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_NAME", "LIKE", "Smith"),
                "f2", new FilterDefinition("USER_STATUS", "=", "ACTIVE"),
                "f3", new FilterDefinition("USER_EMAIL", "LIKE", "admin")
            ),
            "(f1 & f2) | !f3"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: Filter with IN operator for multiple statuses.
     * The system validates that USER_STATUS supports the IN operator.
     */
    public List<User> findUsersByStatuses(List<String> statuses) {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_STATUS", "IN", statuses)
            ),
            "f1"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: Filter with BETWEEN operator for age range.
     * The system validates that USER_AGE supports the BETWEEN operator.
     */
    public List<User> findUsersByAgeRange(Integer minAge, Integer maxAge) {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_AGE", "BETWEEN", List.of(minAge, maxAge))
            ),
            "f1"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: This will throw an exception because USER_NAME doesn't support > operator.
     * The PropertyRef enum defines that USER_NAME only supports: EQUALS, NOT_EQUALS, LIKE, NOT_LIKE, IN, NOT_IN
     */
    public List<User> findUsersWithInvalidOperator() {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("USER_NAME", ">", "Smith") // This will throw IllegalArgumentException
            ),
            "f1"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: This will throw an exception because INVALID_PROPERTY doesn't exist.
     */
    public List<User> findUsersWithInvalidProperty() {
        var filterRequest = new FilterRequest(
            Map.of(
                "f1", new FilterDefinition("INVALID_PROPERTY", "=", "value") // This will throw IllegalArgumentException
            ),
            "f1"
        );
        
        return filterService.executeFilter(filterRequest, User.class);
    }
    
    /**
     * Example: Get information about available properties and their supported operators.
     */
    public void demonstratePropertyInfo() {
        var registry = createPropertyRegistry();
        
        // Get all properties
        System.out.println("Available properties:");
        for (PropertyRef propertyRef : PropertyRef.values()) {
            System.out.println(propertyRef.getDescription());
            System.out.println("  Supported operators: " + propertyRef.getSupportedOperators());
        }
        
        // Check if a specific property supports an operator
        if (PropertyRef.USER_AGE.supportsOperator(Operator.BETWEEN)) {
            System.out.println("USER_AGE supports BETWEEN operator");
        }
        
        // Get all properties that support LIKE operator
        var propertiesSupportingLike = registry.getPropertiesSupportingOperator(Operator.LIKE);
        System.out.println("Properties supporting LIKE: " + propertiesSupportingLike);
        
        // Get all String properties
        var stringProperties = registry.getPropertiesOfType(String.class);
        System.out.println("String properties: " + stringProperties);
    }
}
