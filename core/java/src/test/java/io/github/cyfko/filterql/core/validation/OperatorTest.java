package io.github.cyfko.filterql.core.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {
    
    @Test
    void testFromStringWithSymbol() {
        assertEquals(Operator.EQUALS, Operator.fromString("="));
        assertEquals(Operator.NOT_EQUALS, Operator.fromString("!="));
        assertEquals(Operator.GREATER_THAN, Operator.fromString(">"));
        assertEquals(Operator.LIKE, Operator.fromString("LIKE"));
    }
    
    @Test
    void testFromStringWithCode() {
        assertEquals(Operator.EQUALS, Operator.fromString("EQ"));
        assertEquals(Operator.NOT_EQUALS, Operator.fromString("NE"));
        assertEquals(Operator.GREATER_THAN, Operator.fromString("GT"));
        assertEquals(Operator.LIKE, Operator.fromString("LIKE"));
    }
    
    @Test
    void testFromStringCaseInsensitive() {
        assertEquals(Operator.EQUALS, Operator.fromString("eq"));
        assertEquals(Operator.LIKE, Operator.fromString("like"));
        assertEquals(Operator.IN, Operator.fromString("in"));
    }
    
    @Test
    void testFromStringWithWhitespace() {
        // Test avec des espaces autour des codes/symboles valides
        assertEquals(Operator.EQUALS, Operator.fromString(" = "));
        assertEquals(Operator.EQUALS, Operator.fromString(" EQ "));
        assertEquals(Operator.LIKE, Operator.fromString(" LIKE "));
    }
    
    @Test
    void testFromStringNull() {
        assertNull(Operator.fromString(null));
    }
    
    @Test
    void testFromStringInvalid() {
        assertNull(Operator.fromString("INVALID"));
        assertNull(Operator.fromString("=="));
    }
    
    @Test
    void testRequiresValue() {
        assertTrue(Operator.EQUALS.requiresValue());
        assertTrue(Operator.LIKE.requiresValue());
        assertTrue(Operator.IN.requiresValue());
        assertFalse(Operator.IS_NULL.requiresValue());
        assertFalse(Operator.IS_NOT_NULL.requiresValue());
    }
    
    @Test
    void testSupportsMultipleValues() {
        assertTrue(Operator.IN.supportsMultipleValues());
        assertTrue(Operator.NOT_IN.supportsMultipleValues());
        assertTrue(Operator.BETWEEN.supportsMultipleValues());
        assertTrue(Operator.NOT_BETWEEN.supportsMultipleValues());
        assertFalse(Operator.EQUALS.supportsMultipleValues());
        assertFalse(Operator.LIKE.supportsMultipleValues());
    }
    
    @Test
    void testGetSymbol() {
        assertEquals("=", Operator.EQUALS.getSymbol());
        assertEquals("!=", Operator.NOT_EQUALS.getSymbol());
        assertEquals("LIKE", Operator.LIKE.getSymbol());
    }
    
    @Test
    void testGetCode() {
        assertEquals("EQ", Operator.EQUALS.getCode());
        assertEquals("NE", Operator.NOT_EQUALS.getCode());
        assertEquals("LIKE", Operator.LIKE.getCode());
    }
}
