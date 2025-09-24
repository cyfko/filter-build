package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Example PropertyRef enum for User entity.
 * This shows how developers should define their own property references.
 * They only need to call the parent constructor - all methods are inherited!
 * 
 * Note: PropertyRef only contains the logical definition (type, operators).
 * The actual entity field mapping is handled by PropertyRegistry per adapter.
 */
public enum UserPropertyRef extends PropertyRef {
    
    // User entity properties - only logical definition, no entity field
    USER_NAME(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    USER_EMAIL(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    USER_STATUS(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN)),
    USER_AGE(Integer.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    USER_CREATED_DATE(LocalDateTime.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    USER_IS_ACTIVE(Boolean.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS)),
    USER_SALARY(Double.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN));
    
    UserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
        super(type, supportedOperators);
    }
    
    /**
     * Finds a UserPropertyRef by its entity field name.
     * 
     * @param entityField The entity field name to search for
     * @return The matching UserPropertyRef, or null if not found
     */
    public static UserPropertyRef fromEntityField(String entityField) {
        if (entityField == null) {
            return null;
        }
        
        for (UserPropertyRef propertyRef : values()) {
            if (propertyRef.entityField.equals(entityField)) {
                return propertyRef;
            }
        }
        
        return null;
    }
    
    /**
     * Finds a UserPropertyRef by its name (enum name).
     * 
     * @param name The property reference name
     * @return The matching UserPropertyRef, or null if not found
     */
    public static UserPropertyRef fromName(String name) {
        if (name == null) {
            return null;
        }
        
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
