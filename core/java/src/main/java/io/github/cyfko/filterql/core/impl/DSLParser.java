package io.github.cyfko.filterql.core.impl;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.Parser;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Robust implementation of a parser for a specialized language (DSL) that converts
 * textual expressions into filter tree structures {@link FilterTree}.
 * <p>
 * The parser supports the following logical operators:
 * <ul>
 *   <li>&amp; (AND) - precedence: 2, associativity: left</li>
 *   <li>| (OR) - precedence: 1, associativity: left</li>
 *   <li>! (NOT) - precedence: 3, associativity: right</li>
 * </ul>
 * as well as parentheses for managing priorities.
 *
 * <p>Token identifiers must be alphanumeric or include underscores,
 * and must start with a letter or an underscore.
 *
 * <p>The parser uses the Shunting Yard algorithm to convert infix expressions to postfix
 * before generating an expression tree representing the boolean logic.
 *
 * <h2>Enhanced syntax validation</h2>
 * <ul>
 *   <li>Rejects empty or null expressions via {@link DSLSyntaxException}.</li>
 *   <li>Reports invalid characters with precise position in the original expression.</li>
 *   <li>Detects and rejects unbalanced parentheses with error position.</li>
 *   <li>Validates operand structure according to operators with contextual error messages.</li>
 *   <li>Checks identifier validity according to naming rules.</li>
 *   <li>Detects invalid consecutive operators and malformed expressions.</li>
 * </ul>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * DSLParser parser = new DSLParser();
 * FilterTree tree = parser.parse("!(A & B) | C");
 * Condition condition = tree.generate(context);
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 2.0.0
 */
public class DSLParser implements Parser {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * Default constructor for DSLParser.
     */
    public DSLParser() {
        // Default constructor
    }

    /**
     * Parses a DSL expression string and returns the corresponding {@link FilterTree}.
     *
     * @param dslExpression the DSL expression to parse
     * @return the parsed filter tree
     * @throws DSLSyntaxException if the expression is null, empty, or contains syntax errors
     */
    @Override
    public FilterTree parse(String dslExpression) throws DSLSyntaxException {
        if (dslExpression == null || dslExpression.trim().isEmpty()) {
            throw new DSLSyntaxException("DSL expression cannot be null or empty");
        }

        String trimmed = dslExpression.trim();
        return parseExpression(trimmed);
    }

    /**
     * Parses the given expression string into a {@link FilterTree}.
     *
     * @param expression the expression to parse
     * @return the parsed filter tree
     * @throws DSLSyntaxException if the expression contains syntax errors
     */
    private FilterTree parseExpression(String expression) throws DSLSyntaxException {
        List<Token> tokens = tokenize(expression);
        validateSyntax(tokens);
        return parseTokens(tokens);
    }

    /**
     * Tokenizes the input expression string into a list of tokens.
     *
     * @param expression the expression to tokenize
     * @return the list of tokens
     * @throws DSLSyntaxException if invalid characters or identifiers are found
     */
    private List<Token> tokenize(String expression) throws DSLSyntaxException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int position = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                // End current token if any
                if (currentToken.length() > 0) {
                    String tokenValue = currentToken.toString();
                    validateIdentifier(tokenValue, position - tokenValue.length());
                    tokens.add(new Token(TokenType.IDENTIFIER, tokenValue, position - tokenValue.length()));
                    currentToken.setLength(0);
                }
                position = i + 1;
                continue;
            }

            if (isOperatorChar(c)) {
                // End current token if any
                if (currentToken.length() > 0) {
                    String tokenValue = currentToken.toString();
                    validateIdentifier(tokenValue, position - tokenValue.length());
                    tokens.add(new Token(TokenType.IDENTIFIER, tokenValue, position - tokenValue.length()));
                    currentToken.setLength(0);
                }

                // Add operator/parenthesis token
                TokenType type = getTokenType(c);
                tokens.add(new Token(type, String.valueOf(c), i));
                position = i + 1;
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                if (currentToken.length() == 0) {
                    position = i;
                }
                currentToken.append(c);
            } else {
                throw new DSLSyntaxException("Invalid character '" + c + "' at position " + i);
            }
        }

        // Add final token if any
        if (currentToken.length() > 0) {
            String tokenValue = currentToken.toString();
            validateIdentifier(tokenValue, position);
            tokens.add(new Token(TokenType.IDENTIFIER, tokenValue, position));
        }

        return tokens;
    }

    /**
     * Checks if the given character is a recognized operator or parenthesis.
     *
     * @param c the character to check
     * @return true if the character is an operator or parenthesis, false otherwise
     */
    private boolean isOperatorChar(char c) {
        return c == '&' || c == '|' || c == '!' || c == '(' || c == ')';
    }

    /**
     * Validates that the identifier is non-empty and matches the allowed pattern.
     *
     * @param identifier the identifier to validate
     * @param position the position in the expression
     * @throws DSLSyntaxException if the identifier is invalid
     */
    private void validateIdentifier(String identifier, int position) throws DSLSyntaxException {
        if (identifier.isEmpty()) {
            throw new DSLSyntaxException("Empty identifier at position " + position);
        }

        if (!VALID_IDENTIFIER.matcher(identifier).matches()) {
            throw new DSLSyntaxException(
                    String.format("Invalid identifier '%s' at position %d. Identifiers must start with a letter or underscore and contain only alphanumeric characters and underscores.",
                            identifier, position));
        }
    }

    /**
     * Returns the token type for the given operator character.
     *
     * @param c the operator character
     * @return the corresponding TokenType
     * @throws IllegalArgumentException if the character is not a recognized operator
     */
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

    /**
     * Validates the syntax of the token sequence before parsing.
     *
     * @param tokens the list of tokens to validate
     * @throws DSLSyntaxException if the syntax is invalid
     */
    private void validateSyntax(List<Token> tokens) throws DSLSyntaxException {
        if (tokens.isEmpty()) {
            throw new DSLSyntaxException("Empty expression");
        }

    // Check valid transitions between tokens
        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);
            Token previous = i > 0 ? tokens.get(i - 1) : null;
            Token next = i < tokens.size() - 1 ? tokens.get(i + 1) : null;

            validateTokenTransition(previous, current, next, i);
        }

    // Check that the expression does not start/end with a binary operator
        Token first = tokens.get(0);
        Token last = tokens.get(tokens.size() - 1);

        if (first.getType() == TokenType.AND || first.getType() == TokenType.OR) {
            throw new DSLSyntaxException("Expression cannot start with binary operator '" +
                    first.getValue() + "' at position " + first.getPosition());
        }

        if (last.getType() == TokenType.AND || last.getType() == TokenType.OR || last.getType() == TokenType.NOT) {
            throw new DSLSyntaxException("Expression cannot end with operator '" +
                    last.getValue() + "' at position " + last.getPosition());
        }
    }

    /**
     * Validates the transition between tokens in the expression.
     *
     * @param previous the previous token
     * @param current the current token
     * @param next the next token
     * @param index the index of the current token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateTokenTransition(Token previous, Token current, Token next, int index) throws DSLSyntaxException {
        TokenType currentType = current.getType();

        switch (currentType) {
            case IDENTIFIER:
                validateIdentifierTransition(previous, current, next);
                break;
            case AND:
            case OR:
                validateBinaryOperatorTransition(previous, current, next);
                break;
            case NOT:
                validateNotOperatorTransition(previous, current, next);
                break;
            case LEFT_PAREN:
                validateLeftParenTransition(previous, current, next);
                break;
            case RIGHT_PAREN:
                validateRightParenTransition(previous, current, next);
                break;
        }
    }

    /**
     * Validates transitions for identifier tokens.
     *
     * @param previous the previous token
     * @param current the current identifier token
     * @param next the next token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateIdentifierTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
    // An identifier cannot be followed by another identifier or a NOT
        if (next != null && (next.getType() == TokenType.IDENTIFIER || next.getType() == TokenType.NOT)) {
            throw new DSLSyntaxException("Invalid syntax: identifier '" + current.getValue() +
                    "' cannot be followed by '" + next.getValue() +
                    "' at position " + next.getPosition());
        }

    // An identifier cannot follow a closing parenthesis
        if (previous != null && previous.getType() == TokenType.RIGHT_PAREN) {
            throw new DSLSyntaxException("Invalid syntax: identifier '" + current.getValue() +
                    "' cannot follow ')' at position " + current.getPosition());
        }
    }

    /**
     * Validates transitions for binary operator tokens.
     *
     * @param previous the previous token
     * @param current the current operator token
     * @param next the next token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateBinaryOperatorTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
    // A binary operator must have an operand before
        if (previous == null || (previous.getType() != TokenType.IDENTIFIER && previous.getType() != TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("Binary operator '" + current.getValue() +
                    "' requires a left operand at position " + current.getPosition());
        }

    // A binary operator cannot be followed by another binary operator
        if (next != null && (next.getType() == TokenType.AND || next.getType() == TokenType.OR)) {
            throw new DSLSyntaxException("Invalid syntax: binary operator '" + current.getValue() +
                    "' cannot be followed by '" + next.getValue() +
                    "' at position " + next.getPosition());
        }
    }

    /**
     * Validates transitions for NOT operator tokens.
     *
     * @param previous the previous token
     * @param current the current NOT token
     * @param next the next token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateNotOperatorTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
    // NOT cannot be followed by a binary operator
        if (next != null && (next.getType() == TokenType.AND || next.getType() == TokenType.OR)) {
            throw new DSLSyntaxException("NOT operator cannot be followed by binary operator '" +
                    next.getValue() + "' at position " + next.getPosition());
        }

    // NOT cannot follow an identifier or a closing parenthesis
        if (previous != null && (previous.getType() == TokenType.IDENTIFIER || previous.getType() == TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("NOT operator cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    /**
     * Validates transitions for left parenthesis tokens.
     *
     * @param previous the previous token
     * @param current the current left parenthesis token
     * @param next the next token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateLeftParenTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
    // ( cannot follow an identifier or )
        if (previous != null && (previous.getType() == TokenType.IDENTIFIER || previous.getType() == TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("Left parenthesis cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    /**
     * Validates transitions for right parenthesis tokens.
     *
     * @param previous the previous token
     * @param current the current right parenthesis token
     * @param next the next token
     * @throws DSLSyntaxException if the transition is invalid
     */
    private void validateRightParenTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
    // ) cannot follow an operator or (
        if (previous != null && (previous.getType() == TokenType.AND || previous.getType() == TokenType.OR ||
                previous.getType() == TokenType.NOT || previous.getType() == TokenType.LEFT_PAREN)) {
            throw new DSLSyntaxException("Right parenthesis cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    /**
     * Parses the list of tokens into a {@link FilterTree} by first converting to postfix notation
     * and then building the expression tree.
     *
     * @param tokens the list of tokens
     * @return the parsed filter tree
     * @throws DSLSyntaxException if the expression is malformed
     */
    private FilterTree parseTokens(List<Token> tokens) throws DSLSyntaxException {
        // Convert to postfix notation using Shunting Yard algorithm
        List<Token> postfix = infixToPostfix(tokens);

        // Build expression tree from postfix notation
        return buildExpressionTree(postfix);
    }

    /**
     * Converts infix token list to postfix notation using the Shunting Yard algorithm.
     *
     * @param tokens the infix token list
     * @return the postfix token list
     * @throws DSLSyntaxException if parentheses are mismatched
     */
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
                    // Handle left associativity for AND and OR
                    while (!operators.isEmpty() &&
                            operators.peek().getType() != TokenType.LEFT_PAREN &&
                            (getPrecedence(operators.peek().getType()) > getPrecedence(token.getType()) ||
                                    (getPrecedence(operators.peek().getType()) == getPrecedence(token.getType()) &&
                                            isLeftAssociative(token.getType())))) {
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
                        throw new DSLSyntaxException("Mismatched parentheses: unmatched ')' at position " + token.getPosition());
                    }
                    operators.pop(); // Remove left parenthesis
                    break;
            }
        }

        while (!operators.isEmpty()) {
            Token op = operators.pop();
            if (op.getType() == TokenType.LEFT_PAREN) {
                throw new DSLSyntaxException("Mismatched parentheses: unmatched '(' at position " + op.getPosition());
            }
            output.add(op);
        }

        return output;
    }

    /**
     * Returns the precedence of the given token type.
     *
     * @param type the token type
     * @return the precedence value (higher means higher precedence)
     */
    private int getPrecedence(TokenType type) {
        switch (type) {
            case NOT: return 3;
            case AND: return 2;
            case OR: return 1;
            default: return 0;
        }
    }

    /**
     * Checks if the given token type is left-associative.
     *
     * @param type the token type
     * @return true if left-associative, false if right-associative
     */
    private boolean isLeftAssociative(TokenType type) {
        // NOT is right-associative, AND and OR are left-associative
        return type != TokenType.NOT;
    }

    /**
     * Builds an expression tree from the postfix token list.
     *
     * @param postfix the postfix token list
     * @return the root of the expression tree
     * @throws DSLSyntaxException if the expression is malformed
     */
    private FilterTree buildExpressionTree(List<Token> postfix) throws DSLSyntaxException {
        Stack<FilterTree> stack = new Stack<>();

        for (Token token : postfix) {
            switch (token.getType()) {
                case IDENTIFIER:
                    stack.push(new IdentifierNode(token.getValue()));
                    break;
                case NOT:
                    if (stack.isEmpty()) {
                        throw new DSLSyntaxException("Invalid expression: NOT operator without operand at position " + token.getPosition());
                    }
                    stack.push(new NotNode(stack.pop()));
                    break;
                case AND:
                    if (stack.size() < 2) {
                        throw new DSLSyntaxException("Invalid expression: AND operator requires two operands at position " + token.getPosition());
                    }
                    FilterTree right = stack.pop();
                    FilterTree left = stack.pop();
                    stack.push(new AndNode(left, right));
                    break;
                case OR:
                    if (stack.size() < 2) {
                        throw new DSLSyntaxException("Invalid expression: OR operator requires two operands at position " + token.getPosition());
                    }
                    FilterTree rightOr = stack.pop();
                    FilterTree leftOr = stack.pop();
                    stack.push(new OrNode(leftOr, rightOr));
                    break;
            }
        }

        if (stack.size() != 1) {
            throw new DSLSyntaxException("Invalid expression: malformed syntax - expected single result but got " + stack.size());
        }

        return stack.pop();
    }

    /**
     * Represents a token in the DSL expression, with type, value, and position.
     */
    private static class Token {
        private final TokenType type;
        private final String value;
        private final int position;

        /**
         * Constructs a new Token.
         *
         * @param type the token type
         * @param value the token value
         * @param position the position in the expression
         */
        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        /**
         * Gets the token type.
         * @return the token type
         */
        public TokenType getType() {
            return type;
        }

        /**
         * Gets the token value.
         * @return the token value
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the position of the token in the expression.
         * @return the position
         */
        public int getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return String.format("Token{type=%s, value='%s', position=%d}", type, value, position);
        }
    }

    /**
     * Enumerates the types of tokens in the DSL expression.
     */
    private enum TokenType {
        IDENTIFIER, AND, OR, NOT, LEFT_PAREN, RIGHT_PAREN
    }

    /**
     * Node representing an identifier in the expression tree.
     */
    private static class IdentifierNode implements FilterTree {
        private final String identifier;

        /**
         * Constructs an IdentifierNode.
         * @param identifier the identifier name
         */
        public IdentifierNode(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Generates the condition for this identifier using the provided context.
         * @param context the context
         * @return the condition
         * @throws DSLSyntaxException if the identifier is not found in the context
         */
        @Override
        public Condition generate(Context context) throws DSLSyntaxException {
            try {
                return context.getCondition(identifier);
            } catch (IllegalArgumentException e) {
                throw new DSLSyntaxException(String.format("Filter <%s> referenced in the combination expression does not exist.", identifier));
            }
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    /**
     * Node representing a NOT operation in the expression tree.
     */
    private static class NotNode implements FilterTree {
        private final FilterTree operand;

        /**
         * Constructs a NotNode.
         * @param operand the operand to negate
         */
        public NotNode(FilterTree operand) {
            this.operand = operand;
        }

        /**
         * Generates the negated condition.
         * @param context the context
         * @return the negated condition
         * @throws FilterValidationException if validation fails
         * @throws DSLSyntaxException if syntax is invalid
         */
        @Override
        public Condition generate(Context context) throws FilterValidationException, DSLSyntaxException {
            return operand.generate(context).not();
        }

        @Override
        public String toString() {
            return "NOT(" + operand + ")";
        }
    }

    /**
     * Node representing an AND operation in the expression tree.
     */
    private static class AndNode implements FilterTree {
        private final FilterTree left;
        private final FilterTree right;

        /**
         * Constructs an AndNode.
         * @param left the left operand
         * @param right the right operand
         */
        public AndNode(FilterTree left, FilterTree right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Generates the AND condition.
         * @param context the context
         * @return the ANDed condition
         * @throws FilterValidationException if validation fails
         * @throws DSLSyntaxException if syntax is invalid
         */
        @Override
        public Condition generate(Context context) throws FilterValidationException, DSLSyntaxException {
            return left.generate(context).and(right.generate(context));
        }

        @Override
        public String toString() {
            return "(" + left + " AND " + right + ")";
        }
    }

    /**
     * Node representing an OR operation in the expression tree.
     */
    private static class OrNode implements FilterTree {
        private final FilterTree left;
        private final FilterTree right;

        /**
         * Constructs an OrNode.
         * @param left the left operand
         * @param right the right operand
         */
        public OrNode(FilterTree left, FilterTree right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Generates the OR condition.
         * @param context the context
         * @return the ORed condition
         * @throws FilterValidationException if validation fails
         * @throws DSLSyntaxException if syntax is invalid
         */
        @Override
        public Condition generate(Context context) throws FilterValidationException, DSLSyntaxException {
            return left.generate(context).or(right.generate(context));
        }

        @Override
        public String toString() {
            return "(" + left + " OR " + right + ")";
        }
    }
}
