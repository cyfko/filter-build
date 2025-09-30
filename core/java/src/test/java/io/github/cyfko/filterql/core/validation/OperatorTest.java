package io.github.cyfko.filterql.core.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {
    
    @Test
    void testFromStringWithSymbol() {
        assertEquals(Op.EQUALS, Op.fromString("="));
        assertEquals(Op.NOT_EQUALS, Op.fromString("!="));
        assertEquals(Op.GREATER_THAN, Op.fromString(">"));
        assertEquals(Op.LIKE, Op.fromString("LIKE"));
    }
    
    @Test
    void testFromStringWithCode() {
        assertEquals(Op.EQUALS, Op.fromString("EQ"));
        assertEquals(Op.NOT_EQUALS, Op.fromString("NE"));
        assertEquals(Op.GREATER_THAN, Op.fromString("GT"));
        assertEquals(Op.LIKE, Op.fromString("LIKE"));
    }
    
    @Test
    void testFromStringCaseInsensitive() {
        assertEquals(Op.EQUALS, Op.fromString("eq"));
        assertEquals(Op.LIKE, Op.fromString("like"));
        assertEquals(Op.IN, Op.fromString("in"));
    }
    
    @Test
    void testFromStringWithWhitespace() {
        // Test avec des espaces autour des codes/symboles valides
        assertEquals(Op.EQUALS, Op.fromString(" = "));
        assertEquals(Op.EQUALS, Op.fromString(" EQ "));
        assertEquals(Op.LIKE, Op.fromString(" LIKE "));
    }
    
    @Test
    void testFromStringNull() {
        assertNull(Op.fromString(null));
    }
    
    @Test
    void testFromStringInvalid() {
        assertNull(Op.fromString("INVALID"));
        assertNull(Op.fromString("=="));
    }
    
    @Test
    void testRequiresValue() {
        assertTrue(Op.EQUALS.requiresValue());
        assertTrue(Op.LIKE.requiresValue());
        assertTrue(Op.IN.requiresValue());
        assertFalse(Op.IS_NULL.requiresValue());
        assertFalse(Op.IS_NOT_NULL.requiresValue());
    }
    
    @Test
    void testSupportsMultipleValues() {
        assertTrue(Op.IN.supportsMultipleValues());
        assertTrue(Op.NOT_IN.supportsMultipleValues());
        assertTrue(Op.BETWEEN.supportsMultipleValues());
        assertTrue(Op.NOT_BETWEEN.supportsMultipleValues());
        assertFalse(Op.EQUALS.supportsMultipleValues());
        assertFalse(Op.LIKE.supportsMultipleValues());
    }
    
    @Test
    void testGetSymbol() {
        assertEquals("=", Op.EQUALS.getSymbol());
        assertEquals("!=", Op.NOT_EQUALS.getSymbol());
        assertEquals("LIKE", Op.LIKE.getSymbol());
    }
    
    @Test
    void testGetCode() {
        assertEquals("EQ", Op.EQUALS.getCode());
        assertEquals("NE", Op.NOT_EQUALS.getCode());
        assertEquals("LIKE", Op.LIKE.getCode());
    }
}
