"""
DSL Parser implementation for Python.
Converts string expressions into FilterTree structures.
"""

from typing import List, Optional
from enum import Enum
from dataclasses import dataclass

from .interfaces import FilterTree, Context, Condition, DSLSyntaxException

class TokenType(Enum):
    IDENTIFIER = "IDENTIFIER"
    AND = "AND"
    OR = "OR"
    NOT = "NOT"
    LEFT_PAREN = "LEFT_PAREN"
    RIGHT_PAREN = "RIGHT_PAREN"

@dataclass
class Token:
    type: TokenType
    value: str

class DSLParser:
    """Implementation of the DSL parser that converts string expressions into FilterTree structures."""
    
    def parse(self, dsl_expression: str) -> FilterTree:
        """Parses a DSL expression string into a FilterTree."""
        if not dsl_expression or not dsl_expression.strip():
            raise DSLSyntaxException("DSL expression cannot be null or empty")
        
        trimmed = dsl_expression.strip()
        return self._parse_expression(trimmed)
    
    def _parse_expression(self, expression: str) -> FilterTree:
        """Parses an expression into tokens and builds a FilterTree."""
        tokens = self._tokenize(expression)
        return self._parse_tokens(tokens)
    
    def _tokenize(self, expression: str) -> List[Token]:
        """Tokenizes the expression string."""
        tokens = []
        current_token = ""
        
        for i, c in enumerate(expression):
            if c.isspace():
                # End current token if any
                if current_token:
                    tokens.append(Token(TokenType.IDENTIFIER, current_token))
                    current_token = ""
                continue
            
            if c in "&|!()":
                # End current token if any
                if current_token:
                    tokens.append(Token(TokenType.IDENTIFIER, current_token))
                    current_token = ""
                
                # Add operator/parenthesis token
                token_type = self._get_token_type(c)
                tokens.append(Token(token_type, c))
            elif c.isalnum() or c == '_':
                current_token += c
            else:
                raise DSLSyntaxException(f"Invalid character '{c}' at position {i}")
        
        # Add final token if any
        if current_token:
            tokens.append(Token(TokenType.IDENTIFIER, current_token))
        
        return tokens
    
    def _get_token_type(self, c: str) -> TokenType:
        """Gets the token type for a character."""
        token_map = {
            '&': TokenType.AND,
            '|': TokenType.OR,
            '!': TokenType.NOT,
            '(': TokenType.LEFT_PAREN,
            ')': TokenType.RIGHT_PAREN
        }
        if c not in token_map:
            raise ValueError(f"Unknown operator: {c}")
        return token_map[c]
    
    def _parse_tokens(self, tokens: List[Token]) -> FilterTree:
        """Parses tokens into a FilterTree using the Shunting Yard algorithm."""
        if not tokens:
            raise DSLSyntaxException("Empty expression")
        
        # Convert to postfix notation using Shunting Yard algorithm
        postfix = self._infix_to_postfix(tokens)
        
        # Build expression tree from postfix notation
        return self._build_expression_tree(postfix)
    
    def _infix_to_postfix(self, tokens: List[Token]) -> List[Token]:
        """Converts infix notation to postfix notation using Shunting Yard algorithm."""
        output = []
        operators = []
        
        for token in tokens:
            if token.type == TokenType.IDENTIFIER:
                output.append(token)
            elif token.type == TokenType.NOT:
                operators.append(token)
            elif token.type in (TokenType.AND, TokenType.OR):
                while (operators and 
                       operators[-1].type != TokenType.LEFT_PAREN and
                       self._get_precedence(operators[-1].type) >= self._get_precedence(token.type)):
                    output.append(operators.pop())
                operators.append(token)
            elif token.type == TokenType.LEFT_PAREN:
                operators.append(token)
            elif token.type == TokenType.RIGHT_PAREN:
                while operators and operators[-1].type != TokenType.LEFT_PAREN:
                    output.append(operators.pop())
                if not operators:
                    raise DSLSyntaxException("Mismatched parentheses")
                operators.pop()  # Remove left parenthesis
        
        while operators:
            op = operators.pop()
            if op.type == TokenType.LEFT_PAREN:
                raise DSLSyntaxException("Mismatched parentheses")
            output.append(op)
        
        return output
    
    def _get_precedence(self, token_type: TokenType) -> int:
        """Gets the precedence of a token type."""
        precedence_map = {
            TokenType.NOT: 3,
            TokenType.AND: 2,
            TokenType.OR: 1
        }
        return precedence_map.get(token_type, 0)
    
    def _build_expression_tree(self, postfix: List[Token]) -> FilterTree:
        """Builds an expression tree from postfix notation."""
        stack = []
        
        for token in postfix:
            if token.type == TokenType.IDENTIFIER:
                stack.append(IdentifierNode(token.value))
            elif token.type == TokenType.NOT:
                if not stack:
                    raise DSLSyntaxException("Invalid expression: NOT operator without operand")
                stack.append(NotNode(stack.pop()))
            elif token.type == TokenType.AND:
                if len(stack) < 2:
                    raise DSLSyntaxException("Invalid expression: AND operator requires two operands")
                right = stack.pop()
                left = stack.pop()
                stack.append(AndNode(left, right))
            elif token.type == TokenType.OR:
                if len(stack) < 2:
                    raise DSLSyntaxException("Invalid expression: OR operator requires two operands")
                right = stack.pop()
                left = stack.pop()
                stack.append(OrNode(left, right))
        
        if len(stack) != 1:
            raise DSLSyntaxException("Invalid expression: malformed syntax")
        
        return stack[0]

# Node classes for the expression tree
class IdentifierNode(FilterTree):
    def __init__(self, identifier: str):
        self.identifier = identifier
    
    def generate(self, context: Context) -> Condition:
        condition = context.get_condition(self.identifier)
        if not condition:
            raise ValueError(f"No condition found for identifier: {self.identifier}")
        return condition

class NotNode(FilterTree):
    def __init__(self, operand: FilterTree):
        self.operand = operand
    
    def generate(self, context: Context) -> Condition:
        return self.operand.generate(context).not_()

class AndNode(FilterTree):
    def __init__(self, left: FilterTree, right: FilterTree):
        self.left = left
        self.right = right
    
    def generate(self, context: Context) -> Condition:
        return self.left.generate(context).and_(self.right.generate(context))

class OrNode(FilterTree):
    def __init__(self, left: FilterTree, right: FilterTree):
        self.left = left
        self.right = right
    
    def generate(self, context: Context) -> Condition:
        return self.left.generate(context).or_(self.right.generate(context))
