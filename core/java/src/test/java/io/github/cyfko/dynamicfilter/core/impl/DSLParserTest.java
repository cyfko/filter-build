package io.github.cyfko.dynamicfilter.core.impl;

import io.github.cyfko.dynamicfilter.core.FilterTree;
import io.github.cyfko.dynamicfilter.core.Parser;
import io.github.cyfko.dynamicfilter.core.exception.DSLSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DSLParserTest {
    
    private Parser parser;
    
    @BeforeEach
    void setUp() {
        parser = new DSLParser();
    }
    
    @Test
    void testParseSimpleExpression() throws DSLSyntaxException {
        FilterTree tree = parser.parse("f1");
        assertNotNull(tree);
    }
    
    @Test
    void testParseAndExpression() throws DSLSyntaxException {
        FilterTree tree = parser.parse("f1 & f2");
        assertNotNull(tree);
    }
    
    @Test
    void testParseOrExpression() throws DSLSyntaxException {
        FilterTree tree = parser.parse("f1 | f2");
        assertNotNull(tree);
    }
    
    @Test
    void testParseNotExpression() throws DSLSyntaxException {
        FilterTree tree = parser.parse("!f1");
        assertNotNull(tree);
    }
    
    @Test
    void testParseComplexExpression() throws DSLSyntaxException {
        FilterTree tree = parser.parse("(f1 & f2) | !f3");
        assertNotNull(tree);
    }
    
    @Test
    void testParseNestedParentheses() throws DSLSyntaxException {
        FilterTree tree = parser.parse("((f1 & f2) | f3) & !f4");
        assertNotNull(tree);
    }
    
    @Test
    void testParseEmptyExpression() {
        assertThrows(DSLSyntaxException.class, () -> parser.parse(""));
    }
    
    @Test
    void testParseNullExpression() {
        assertThrows(DSLSyntaxException.class, () -> parser.parse(null));
    }
    
    @Test
    void testParseInvalidCharacter() {
        assertThrows(DSLSyntaxException.class, () -> parser.parse("f1 @ f2"));
    }
    
    @Test
    void testParseMismatchedParentheses() {
        assertThrows(DSLSyntaxException.class, () -> parser.parse("(f1 & f2"));
        assertThrows(DSLSyntaxException.class, () -> parser.parse("f1 & f2)"));
    }
    
    @Test
    void testParseInvalidOperatorUsage() {
        assertThrows(DSLSyntaxException.class, () -> parser.parse("& f1"));
        assertThrows(DSLSyntaxException.class, () -> parser.parse("f1 &"));
        assertThrows(DSLSyntaxException.class, () -> parser.parse("f1 & & f2"));
    }
    
    @Test
    void testParseWhitespaceHandling() throws DSLSyntaxException {
        FilterTree tree1 = parser.parse("f1&f2");
        FilterTree tree2 = parser.parse("f1 & f2");
        FilterTree tree3 = parser.parse(" f1  &  f2 ");
        
        // All should parse successfully
        assertNotNull(tree1);
        assertNotNull(tree2);
        assertNotNull(tree3);
    }
    
    @Test
    void testParseComplexRealWorldExample() throws DSLSyntaxException {
        String expression = "(user_name & status) | (!created_date & priority)";
        FilterTree tree = parser.parse(expression);
        assertNotNull(tree);
    }
}
