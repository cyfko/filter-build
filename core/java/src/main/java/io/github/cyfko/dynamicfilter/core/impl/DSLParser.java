package io.github.cyfko.dynamicfilter.core.impl;

import io.github.cyfko.dynamicfilter.core.FilterTree;
import io.github.cyfko.dynamicfilter.core.Parser;
import io.github.cyfko.dynamicfilter.core.exception.DSLSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Implementation of the DSL parser that converts string expressions into FilterTree structures.
 * 
 * Supports operators: & (AND), | (OR), ! (NOT)
 * Supports parentheses for grouping
 * Token identifiers must be alphanumeric
 */
public class DSLParser implements Parser {
    
    @Override
    public FilterTree parse(String dslExpression) throws DSLSyntaxException {
        if (dslExpression == null || dslExpression.trim().isEmpty()) {
            throw new DSLSyntaxException("DSL expression cannot be null or empty");
        }
        
        String trimmed = dslExpression.trim();
        return parseExpression(trimmed);
    }
    
    private FilterTree parseExpression(String expression) throws DSLSyntaxException {
        List<Token> tokens = tokenize(expression);
        return parseTokens(tokens);
    }
    
    private List<Token> tokenize(String expression) throws DSLSyntaxException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isWhitespace(c)) {
                // End current token if any
                if (currentToken.length() > 0) {
                    tokens.add(new Token(TokenType.IDENTIFIER, currentToken.toString()));
                    currentToken.setLength(0);
                }
                continue;
            }
            
            if (c == '&' || c == '|' || c == '!' || c == '(' || c == ')') {
                // End current token if any
                if (currentToken.length() > 0) {
                    tokens.add(new Token(TokenType.IDENTIFIER, currentToken.toString()));
                    currentToken.setLength(0);
                }
                
                // Add operator/parenthesis token
                TokenType type = getTokenType(c);
                tokens.add(new Token(type, String.valueOf(c)));
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                currentToken.append(c);
            } else {
                throw new DSLSyntaxException("Invalid character '" + c + "' at position " + i);
            }
        }
        
        // Add final token if any
        if (currentToken.length() > 0) {
            tokens.add(new Token(TokenType.IDENTIFIER, currentToken.toString()));
        }
        
        return tokens;
    }
    
    private TokenType getTokenType(char c) {
        switch (c) {
            case '&': return TokenType.AND;
            case '|': return TokenType.OR;
            case '!': return TokenType.NOT;
            case '(': return TokenType.LEFT_PAREN;
            case ')': return TokenType.RIGHT_PAREN;
            default: throw new IllegalArgumentException("Unknown operator: " + c);
        }
    }
    
    private FilterTree parseTokens(List<Token> tokens) throws DSLSyntaxException {
        if (tokens.isEmpty()) {
            throw new DSLSyntaxException("Empty expression");
        }
        
        // Convert to postfix notation using Shunting Yard algorithm
        List<Token> postfix = infixToPostfix(tokens);
        
        // Build expression tree from postfix notation
        return buildExpressionTree(postfix);
    }
    
    private List<Token> infixToPostfix(List<Token> tokens) throws DSLSyntaxException {
        List<Token> output = new ArrayList<>();
        Stack<Token> operators = new Stack<>();
        
        for (Token token : tokens) {
            switch (token.getType()) {
                case IDENTIFIER:
                    output.add(token);
                    break;
                case NOT:
                    operators.push(token);
                    break;
                case AND:
                case OR:
                    while (!operators.isEmpty() && 
                           operators.peek().getType() != TokenType.LEFT_PAREN &&
                           getPrecedence(operators.peek().getType()) >= getPrecedence(token.getType())) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                    break;
                case LEFT_PAREN:
                    operators.push(token);
                    break;
                case RIGHT_PAREN:
                    while (!operators.isEmpty() && operators.peek().getType() != TokenType.LEFT_PAREN) {
                        output.add(operators.pop());
                    }
                    if (operators.isEmpty()) {
                        throw new DSLSyntaxException("Mismatched parentheses");
                    }
                    operators.pop(); // Remove left parenthesis
                    break;
            }
        }
        
        while (!operators.isEmpty()) {
            Token op = operators.pop();
            if (op.getType() == TokenType.LEFT_PAREN) {
                throw new DSLSyntaxException("Mismatched parentheses");
            }
            output.add(op);
        }
        
        return output;
    }
    
    private int getPrecedence(TokenType type) {
        switch (type) {
            case NOT: return 3;
            case AND: return 2;
            case OR: return 1;
            default: return 0;
        }
    }
    
    private FilterTree buildExpressionTree(List<Token> postfix) throws DSLSyntaxException {
        Stack<FilterTree> stack = new Stack<>();
        
        for (Token token : postfix) {
            switch (token.getType()) {
                case IDENTIFIER:
                    stack.push(new IdentifierNode(token.getValue()));
                    break;
                case NOT:
                    if (stack.isEmpty()) {
                        throw new DSLSyntaxException("Invalid expression: NOT operator without operand");
                    }
                    stack.push(new NotNode(stack.pop()));
                    break;
                case AND:
                    if (stack.size() < 2) {
                        throw new DSLSyntaxException("Invalid expression: AND operator requires two operands");
                    }
                    FilterTree right = stack.pop();
                    FilterTree left = stack.pop();
                    stack.push(new AndNode(left, right));
                    break;
                case OR:
                    if (stack.size() < 2) {
                        throw new DSLSyntaxException("Invalid expression: OR operator requires two operands");
                    }
                    FilterTree rightOr = stack.pop();
                    FilterTree leftOr = stack.pop();
                    stack.push(new OrNode(leftOr, rightOr));
                    break;
            }
        }
        
        if (stack.size() != 1) {
            throw new DSLSyntaxException("Invalid expression: malformed syntax");
        }
        
        return stack.pop();
    }
    
    // Token classes
    private static class Token {
        private final TokenType type;
        private final String value;
        
        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
        
        public TokenType getType() {
            return type;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private enum TokenType {
        IDENTIFIER, AND, OR, NOT, LEFT_PAREN, RIGHT_PAREN
    }
    
    // Node classes for the expression tree
    private static class IdentifierNode implements FilterTree {
        private final String identifier;
        
        public IdentifierNode(String identifier) {
            this.identifier = identifier;
        }
        
        @Override
        public Condition generate(Context context) {
            Condition condition = context.getCondition(identifier);
            if (condition == null) {
                throw new IllegalArgumentException("No condition found for identifier: " + identifier);
            }
            return condition;
        }
    }
    
    private static class NotNode implements FilterTree {
        private final FilterTree operand;
        
        public NotNode(FilterTree operand) {
            this.operand = operand;
        }
        
        @Override
        public Condition generate(Context context) {
            return operand.generate(context).not();
        }
    }
    
    private static class AndNode implements FilterTree {
        private final FilterTree left;
        private final FilterTree right;
        
        public AndNode(FilterTree left, FilterTree right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public Condition generate(Context context) {
            return left.generate(context).and(right.generate(context));
        }
    }
    
    private static class OrNode implements FilterTree {
        private final FilterTree left;
        private final FilterTree right;
        
        public OrNode(FilterTree left, FilterTree right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public Condition generate(Context context) {
            return left.generate(context).or(right.generate(context));
        }
    }
}
