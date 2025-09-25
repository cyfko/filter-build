package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

import java.util.Set;

/**
 * Example PropertyRef enum for Product entity.
 * This shows how developers can define different property references for different entities.
 * They only need to call the parent constructor - all methods are inherited!
 */
public enum ProductPropertyRef extends PropertyRef {
    
    // Product entity properties
    PRODUCT_NAME("name", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    PRODUCT_PRICE("price", Double.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    PRODUCT_CATEGORY("category", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN)),
    PRODUCT_IN_STOCK("inStock", Boolean.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS)),
    PRODUCT_QUANTITY("quantity", Integer.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    PRODUCT_DESCRIPTION("description", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE)),
    PRODUCT_SKU("sku", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN));
    
    ProductPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators);
    }
    
    /**
     * Finds a ProductPropertyRef by its entity field name.
     * 
     * @param entityField The entity field name to search for
     * @return The matching ProductPropertyRef, or null if not found
     */
    public static ProductPropertyRef fromEntityField(String entityField) {
        if (entityField == null) {
            return null;
        }
        
        for (ProductPropertyRef propertyRef : values()) {
            if (propertyRef.entityField.equals(entityField)) {
                return propertyRef;
            }
        }
        
        return null;
    }
    
    /**
     * Finds a ProductPropertyRef by its name (enum name).
     * 
     * @param name The property reference name
     * @return The matching ProductPropertyRef, or null if not found
     */
    public static ProductPropertyRef fromName(String name) {
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
