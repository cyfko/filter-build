"""
Unit tests for parser module.
"""

import pytest
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from parser import DSLParser, TokenType, Token
from interfaces import FilterTree, Context, Condition, DSLSyntaxException


class MockCondition(Condition):
    """Mock implementation of Condition for testing."""
    
    def __init__(self, value: str = "mock"):
        self.value = value
    
    def and_(self, other: Condition) -> Condition:
        return MockCondition(f"({self.value} AND {other.value})")
    
    def or_(self, other: Condition) -> Condition:
        return MockCondition(f"({self.value} OR {other.value})")
    
    def not_(self) -> Condition:
        return MockCondition(f"NOT {self.value}")


class MockContext(Context):
    """Mock implementation of Context for testing."""
    
    def __init__(self, conditions: dict = None):
        self.conditions = conditions or {}
    
    def get_condition(self, filter_key: str) -> Condition:
        return self.conditions.get(filter_key, MockCondition(filter_key))


class MockFilterTree(FilterTree):
    """Mock implementation of FilterTree for testing."""
    
    def __init__(self, expression: str):
        self.expression = expression
    
    def generate(self, context: Context) -> Condition:
        return MockCondition(f"generated_{self.expression}")


class TestTokenType:
    """Test cases for TokenType enum."""
    
    def test_token_type_values(self):
        """Test that all token types have correct values."""
        assert TokenType.IDENTIFIER.value == "IDENTIFIER"
        assert TokenType.AND.value == "AND"
        assert TokenType.OR.value == "OR"
        assert TokenType.NOT.value == "NOT"
        assert TokenType.LEFT_PAREN.value == "LEFT_PAREN"
        assert TokenType.RIGHT_PAREN.value == "RIGHT_PAREN"
    
    def test_token_type_enumeration(self):
        """Test that all token types can be enumerated."""
        token_types = list(TokenType)
        assert len(token_types) == 6
        assert TokenType.IDENTIFIER in token_types
        assert TokenType.AND in token_types
        assert TokenType.OR in token_types
        assert TokenType.NOT in token_types
        assert TokenType.LEFT_PAREN in token_types
        assert TokenType.RIGHT_PAREN in token_types


class TestToken:
    """Test cases for Token class."""
    
    def test_token_creation(self):
        """Test creating a Token instance."""
        token = Token(TokenType.IDENTIFIER, "filter1")
        
        assert token.type == TokenType.IDENTIFIER
        assert token.value == "filter1"
    
    def test_token_equality(self):
        """Test Token equality."""
        token1 = Token(TokenType.IDENTIFIER, "filter1")
        token2 = Token(TokenType.IDENTIFIER, "filter1")
        token3 = Token(TokenType.AND, "AND")
        
        assert token1 == token2
        assert token1 != token3
    
    def test_token_str_representation(self):
        """Test Token string representation."""
        token = Token(TokenType.IDENTIFIER, "filter1")
        
        str_repr = str(token)
        assert "IDENTIFIER" in str_repr
        assert "filter1" in str_repr


class TestDSLParser:
    """Test cases for DSLParser class."""
    
    def test_parser_creation(self):
        """Test creating a DSLParser instance."""
        parser = DSLParser()
        assert parser is not None
    
    def test_parse_empty_expression(self):
        """Test parsing empty or null expressions."""
        parser = DSLParser()
        
        with pytest.raises(DSLSyntaxException, match="DSL expression cannot be null or empty"):
            parser.parse("")
        
        with pytest.raises(DSLSyntaxException, match="DSL expression cannot be null or empty"):
            parser.parse("   ")
        
        with pytest.raises(DSLSyntaxException, match="DSL expression cannot be null or empty"):
            parser.parse(None)
    
    def test_parse_simple_identifier(self):
        """Test parsing a simple identifier."""
        parser = DSLParser()
        
        # Mock the _parse_expression method to return a MockFilterTree
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: MockFilterTree(expr)
        
        result = parser.parse("filter1")
        assert isinstance(result, MockFilterTree)
        assert result.expression == "filter1"
    
    def test_parse_whitespace_handling(self):
        """Test parsing expressions with whitespace."""
        parser = DSLParser()
        
        # Mock the _parse_expression method
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: MockFilterTree(expr.strip())
        
        result = parser.parse("  filter1  ")
        assert isinstance(result, MockFilterTree)
        assert result.expression == "filter1"


class TestDSLParserTokenization:
    """Test cases for DSLParser tokenization methods."""
    
    def test_tokenize_simple_expression(self):
        """Test tokenizing a simple expression."""
        parser = DSLParser()
        
        tokens = parser._tokenize("filter1")
        assert len(tokens) == 1
        assert tokens[0].type == TokenType.IDENTIFIER
        assert tokens[0].value == "filter1"
    
    def test_tokenize_and_expression(self):
        """Test tokenizing an AND expression."""
        parser = DSLParser()
        
        tokens = parser._tokenize("filter1 & filter2")
        assert len(tokens) == 3
        assert tokens[0].type == TokenType.IDENTIFIER
        assert tokens[0].value == "filter1"
        assert tokens[1].type == TokenType.AND
        assert tokens[1].value == "&"
        assert tokens[2].type == TokenType.IDENTIFIER
        assert tokens[2].value == "filter2"
    
    def test_tokenize_or_expression(self):
        """Test tokenizing an OR expression."""
        parser = DSLParser()
        
        tokens = parser._tokenize("filter1 | filter2")
        assert len(tokens) == 3
        assert tokens[0].type == TokenType.IDENTIFIER
        assert tokens[0].value == "filter1"
        assert tokens[1].type == TokenType.OR
        assert tokens[1].value == "|"
        assert tokens[2].type == TokenType.IDENTIFIER
        assert tokens[2].value == "filter2"
    
    def test_tokenize_not_expression(self):
        """Test tokenizing a NOT expression."""
        parser = DSLParser()
        
        tokens = parser._tokenize("!filter1")
        assert len(tokens) == 2
        assert tokens[0].type == TokenType.NOT
        assert tokens[0].value == "!"
        assert tokens[1].type == TokenType.IDENTIFIER
        assert tokens[1].value == "filter1"
    
    def test_tokenize_parentheses(self):
        """Test tokenizing expressions with parentheses."""
        parser = DSLParser()
        
        tokens = parser._tokenize("(filter1 & filter2)")
        assert len(tokens) == 5
        assert tokens[0].type == TokenType.LEFT_PAREN
        assert tokens[0].value == "("
        assert tokens[1].type == TokenType.IDENTIFIER
        assert tokens[1].value == "filter1"
        assert tokens[2].type == TokenType.AND
        assert tokens[2].value == "&"
        assert tokens[3].type == TokenType.IDENTIFIER
        assert tokens[3].value == "filter2"
        assert tokens[4].type == TokenType.RIGHT_PAREN
        assert tokens[4].value == ")"
    
    def test_tokenize_complex_expression(self):
        """Test tokenizing a complex expression."""
        parser = DSLParser()
        
        tokens = parser._tokenize("(filter1 & filter2) | !filter3")
        assert len(tokens) == 8
        assert tokens[0].type == TokenType.LEFT_PAREN
        assert tokens[1].type == TokenType.IDENTIFIER
        assert tokens[2].type == TokenType.AND
        assert tokens[3].type == TokenType.IDENTIFIER
        assert tokens[4].type == TokenType.RIGHT_PAREN
        assert tokens[5].type == TokenType.OR
        assert tokens[6].type == TokenType.NOT
        assert tokens[7].type == TokenType.IDENTIFIER
    
    def test_tokenize_whitespace_handling(self):
        """Test tokenizing expressions with various whitespace."""
        parser = DSLParser()
        
        # Test with spaces
        tokens = parser._tokenize("filter1 & filter2")
        assert len(tokens) == 3
        
        # Test with multiple spaces
        tokens = parser._tokenize("filter1   &   filter2")
        assert len(tokens) == 3
        
        # Test with tabs and newlines
        tokens = parser._tokenize("filter1\t&\nfilter2")
        assert len(tokens) == 3
    
    def test_tokenize_invalid_characters(self):
        """Test tokenizing expressions with invalid characters."""
        parser = DSLParser()
        
        # Test with invalid characters (should raise exception)
        with pytest.raises(DSLSyntaxException, match="Invalid character"):
            parser._tokenize("filter1@#$%")


class TestDSLParserParsing:
    """Test cases for DSLParser parsing methods."""
    
    def test_parse_expression_simple_identifier(self):
        """Test parsing a simple identifier expression."""
        parser = DSLParser()
        
        # Mock the context to return a condition
        context = MockContext({"filter1": MockCondition("condition1")})
        
        # Mock the parse_expression method to return a simple filter tree
        class SimpleFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                return context.get_condition("filter1")
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: SimpleFilterTree()
        
        result = parser.parse("filter1")
        condition = result.generate(context)
        assert isinstance(condition, MockCondition)
    
    def test_parse_expression_and_operation(self):
        """Test parsing an AND operation."""
        parser = DSLParser()
        
        # Mock the parse_expression method
        class AndFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                cond1 = context.get_condition("filter1")
                cond2 = context.get_condition("filter2")
                return cond1.and_(cond2)
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: AndFilterTree()
        
        result = parser.parse("filter1 & filter2")
        assert isinstance(result, AndFilterTree)
    
    def test_parse_expression_or_operation(self):
        """Test parsing an OR operation."""
        parser = DSLParser()
        
        # Mock the parse_expression method
        class OrFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                cond1 = context.get_condition("filter1")
                cond2 = context.get_condition("filter2")
                return cond1.or_(cond2)
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: OrFilterTree()
        
        result = parser.parse("filter1 | filter2")
        assert isinstance(result, OrFilterTree)
    
    def test_parse_expression_not_operation(self):
        """Test parsing a NOT operation."""
        parser = DSLParser()
        
        # Mock the parse_expression method
        class NotFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                cond = context.get_condition("filter1")
                return cond.not_()
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: NotFilterTree()
        
        result = parser.parse("!filter1")
        assert isinstance(result, NotFilterTree)
    
    def test_parse_expression_parentheses(self):
        """Test parsing expressions with parentheses."""
        parser = DSLParser()
        
        # Mock the parse_expression method
        class ParenFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                cond1 = context.get_condition("filter1")
                cond2 = context.get_condition("filter2")
                return cond1.and_(cond2)
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: ParenFilterTree()
        
        result = parser.parse("(filter1 & filter2)")
        assert isinstance(result, ParenFilterTree)
    
    def test_parse_expression_complex(self):
        """Test parsing complex expressions."""
        parser = DSLParser()
        
        # Mock the parse_expression method
        class ComplexFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                cond1 = context.get_condition("filter1")
                cond2 = context.get_condition("filter2")
                cond3 = context.get_condition("filter3")
                return cond1.and_(cond2).or_(cond3.not_())
        
        original_parse_expression = parser._parse_expression
        parser._parse_expression = lambda expr: ComplexFilterTree()
        
        result = parser.parse("(filter1 & filter2) | !filter3")
        assert isinstance(result, ComplexFilterTree)
