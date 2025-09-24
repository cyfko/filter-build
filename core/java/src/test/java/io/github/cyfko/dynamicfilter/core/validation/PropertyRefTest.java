package io.github.cyfko.dynamicfilter.core.validation;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PropertyRefTest {
    
    @Test
    void testPropertyRefMetadata() {
        // Test USER_NAME property
        assertEquals("userName", PropertyRef.USER_NAME.getEntityField());
        assertEquals(String.class, PropertyRef.USER_NAME.getType());
        assertTrue(PropertyRef.USER_NAME.supportsOperator(Operator.LIKE));
        assertTrue(PropertyRef.USER_NAME.supportsOperator(Operator.EQUALS));
        assertFalse(PropertyRef.USER_NAME.supportsOperator(Operator.GREATER_THAN));
        
        // Test USER_AGE property
        assertEquals("age", PropertyRef.USER_AGE.getEntityField());
        assertEquals(Integer.class, PropertyRef.USER_AGE.getType());
        assertTrue(PropertyRef.USER_AGE.supportsOperator(Operator.GREATER_THAN));
        assertTrue(PropertyRef.USER_AGE.supportsOperator(Operator.BETWEEN));
        assertFalse(PropertyRef.USER_AGE.supportsOperator(Operator.LIKE));
    }
    
    @Test
    void testFromEntityField() {
        assertEquals(PropertyRef.USER_NAME, PropertyRef.fromEntityField("userName"));
        assertEquals(PropertyRef.USER_AGE, PropertyRef.fromEntityField("age"));
        assertNull(PropertyRef.fromEntityField("nonExistentField"));
        assertNull(PropertyRef.fromEntityField(null));
    }
    
    @Test
    void testFromName() {
        assertEquals(PropertyRef.USER_NAME, PropertyRef.fromName("USER_NAME"));
        assertEquals(PropertyRef.USER_AGE, PropertyRef.fromName("USER_AGE"));
        assertNull(PropertyRef.fromName("NON_EXISTENT"));
        assertNull(PropertyRef.fromName(null));
    }
    
    @Test
    void testValidateOperator() {
        // Valid operator
        assertDoesNotThrow(() -> PropertyRef.USER_NAME.validateOperator(Operator.LIKE));
        assertDoesNotThrow(() -> PropertyRef.USER_AGE.validateOperator(Operator.GREATER_THAN));
        
        // Invalid operator
        assertThrows(IllegalArgumentException.class, 
                    () -> PropertyRef.USER_NAME.validateOperator(Operator.GREATER_THAN));
        assertThrows(IllegalArgumentException.class, 
                    () -> PropertyRef.USER_AGE.validateOperator(Operator.LIKE));
    }
    
    @Test
    void testGetSupportedOperators() {
        Set<Operator> userNameOperators = PropertyRef.USER_NAME.getSupportedOperators();
        assertTrue(userNameOperators.contains(Operator.EQUALS));
        assertTrue(userNameOperators.contains(Operator.LIKE));
        assertTrue(userNameOperators.contains(Operator.IN));
        assertFalse(userNameOperators.contains(Operator.GREATER_THAN));
        
        Set<Operator> ageOperators = PropertyRef.USER_AGE.getSupportedOperators();
        assertTrue(ageOperators.contains(Operator.EQUALS));
        assertTrue(ageOperators.contains(Operator.GREATER_THAN));
        assertTrue(ageOperators.contains(Operator.BETWEEN));
        assertFalse(ageOperators.contains(Operator.LIKE));
    }
    
    @Test
    void testGetDescription() {
        String description = PropertyRef.USER_NAME.getDescription();
        assertTrue(description.contains("USER_NAME"));
        assertTrue(description.contains("userName"));
        assertTrue(description.contains("String"));
    }
    
    @Test
    void testToString() {
        String toString = PropertyRef.USER_NAME.toString();
        assertTrue(toString.contains("PropertyRef"));
        assertTrue(toString.contains("USER_NAME"));
        assertTrue(toString.contains("userName"));
        assertTrue(toString.contains("String"));
    }
    
    @Test
    void testAllPropertiesHaveValidMetadata() {
        for (PropertyRef propertyRef : PropertyRef.values()) {
            // Entity field should not be null or empty
            assertNotNull(propertyRef.getEntityField());
            assertFalse(propertyRef.getEntityField().trim().isEmpty());
            
            // Type should not be null
            assertNotNull(propertyRef.getType());
            
            // Should have at least one supported operator
            assertFalse(propertyRef.getSupportedOperators().isEmpty());
            
            // Description should not be null or empty
            assertNotNull(propertyRef.getDescription());
            assertFalse(propertyRef.getDescription().trim().isEmpty());
        }
    }
}
