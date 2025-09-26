package io.github.cyfko.filterql.core.impl;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.Parser;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implémentation robuste d'un parser pour un langage spécialisé (DSL) permettant de convertir
 * des expressions textuelles en structures d'arbre de filtres {@link FilterTree}.
 * <p>
 * Le parser supporte les opérateurs logiques suivants :
 * <ul>
 *   <li>&amp; (AND) - précédence: 2, associativité: gauche</li>
 *   <li>| (OR) - précédence: 1, associativité: gauche</li>
 *   <li>! (NOT) - précédence: 3, associativité: droite</li>
 * </ul>
 * ainsi que les parenthèses pour la gestion des priorités.
 *
 * <p>Les identifiants des tokens doivent être alphanumériques ou inclure des underscores,
 * et doivent commencer par une lettre ou un underscore.
 *
 * <p>Le parser utilise l'algorithme de Shunting Yard pour la conversion des expressions infixées en postfixées
 * avant de générer un arbre d'expression représentant la logique booléenne.
 *
 * <h2>Validation syntaxique renforcée</h2>
 * <ul>
 *   <li>Rejette les expressions vides ou nulles via {@link DSLSyntaxException}.</li>
 *   <li>Signale les caractères invalides avec position précise dans l'expression originale.</li>
 *   <li>Détecte et rejette les parenthèses déséquilibrées avec position d'erreur.</li>
 *   <li>Valide la structure des opérandes selon les opérateurs avec messages d'erreur contextuels.</li>
 *   <li>Vérifie la validité des identifiants selon les règles de nommage.</li>
 *   <li>Détecte les opérateurs consécutifs invalides et les expressions malformées.</li>
 * </ul>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * DSLParser parser = new DSLParser();
 * FilterTree tree = parser.parse("!(A & B) | C");
 * Condition condition = tree.generate(context);
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class DSLParser implements Parser {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

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
        validateSyntax(tokens);
        return parseTokens(tokens);
    }

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

    private boolean isOperatorChar(char c) {
        return c == '&' || c == '|' || c == '!' || c == '(' || c == ')';
    }

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
     * Valide la syntaxe de la séquence de tokens avant le parsing.
     */
    private void validateSyntax(List<Token> tokens) throws DSLSyntaxException {
        if (tokens.isEmpty()) {
            throw new DSLSyntaxException("Empty expression");
        }

        // Vérifier les transitions valides entre tokens
        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);
            Token previous = i > 0 ? tokens.get(i - 1) : null;
            Token next = i < tokens.size() - 1 ? tokens.get(i + 1) : null;

            validateTokenTransition(previous, current, next, i);
        }

        // Vérifier que l'expression ne commence/finit pas par un opérateur binaire
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

    private void validateIdentifierTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
        // Un identifiant ne peut pas être suivi d'un autre identifiant ou d'un NOT
        if (next != null && (next.getType() == TokenType.IDENTIFIER || next.getType() == TokenType.NOT)) {
            throw new DSLSyntaxException("Invalid syntax: identifier '" + current.getValue() +
                    "' cannot be followed by '" + next.getValue() +
                    "' at position " + next.getPosition());
        }

        // Un identifiant ne peut pas suivre une parenthèse fermante
        if (previous != null && previous.getType() == TokenType.RIGHT_PAREN) {
            throw new DSLSyntaxException("Invalid syntax: identifier '" + current.getValue() +
                    "' cannot follow ')' at position " + current.getPosition());
        }
    }

    private void validateBinaryOperatorTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
        // Un opérateur binaire doit avoir un opérande avant
        if (previous == null || (previous.getType() != TokenType.IDENTIFIER && previous.getType() != TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("Binary operator '" + current.getValue() +
                    "' requires a left operand at position " + current.getPosition());
        }

        // Un opérateur binaire ne peut pas être suivi d'un autre opérateur binaire
        if (next != null && (next.getType() == TokenType.AND || next.getType() == TokenType.OR)) {
            throw new DSLSyntaxException("Invalid syntax: binary operator '" + current.getValue() +
                    "' cannot be followed by '" + next.getValue() +
                    "' at position " + next.getPosition());
        }
    }

    private void validateNotOperatorTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
        // NOT ne peut pas être suivi d'un opérateur binaire
        if (next != null && (next.getType() == TokenType.AND || next.getType() == TokenType.OR)) {
            throw new DSLSyntaxException("NOT operator cannot be followed by binary operator '" +
                    next.getValue() + "' at position " + next.getPosition());
        }

        // NOT ne peut pas suivre un identifiant ou une parenthèse fermante
        if (previous != null && (previous.getType() == TokenType.IDENTIFIER || previous.getType() == TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("NOT operator cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    private void validateLeftParenTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
        // ( ne peut pas suivre un identifiant ou )
        if (previous != null && (previous.getType() == TokenType.IDENTIFIER || previous.getType() == TokenType.RIGHT_PAREN)) {
            throw new DSLSyntaxException("Left parenthesis cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    private void validateRightParenTransition(Token previous, Token current, Token next) throws DSLSyntaxException {
        // ) ne peut pas suivre un opérateur ou (
        if (previous != null && (previous.getType() == TokenType.AND || previous.getType() == TokenType.OR ||
                previous.getType() == TokenType.NOT || previous.getType() == TokenType.LEFT_PAREN)) {
            throw new DSLSyntaxException("Right parenthesis cannot follow '" + previous.getValue() +
                    "' at position " + current.getPosition());
        }
    }

    private FilterTree parseTokens(List<Token> tokens) throws DSLSyntaxException {
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
                    // Gestion de l'associativité gauche pour AND et OR
                    while (!operators.isEmpty() &&
                            operators.peek().getType() != TokenType.LEFT_PAREN &&
                            getPrecedence(operators.peek().getType()) > getPrecedence(token.getType()) ||
                            (getPrecedence(operators.peek().getType()) == getPrecedence(token.getType()) &&
                                    isLeftAssociative(token.getType()))) {
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

    private int getPrecedence(TokenType type) {
        switch (type) {
            case NOT: return 3;
            case AND: return 2;
            case OR: return 1;
            default: return 0;
        }
    }

    private boolean isLeftAssociative(TokenType type) {
        // NOT est associatif à droite, AND et OR à gauche
        return type != TokenType.NOT;
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

    // Enhanced Token class with position tracking
    private static class Token {
        private final TokenType type;
        private final String value;
        private final int position;

        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        public TokenType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return String.format("Token{type=%s, value='%s', position=%d}", type, value, position);
        }
    }

    private enum TokenType {
        IDENTIFIER, AND, OR, NOT, LEFT_PAREN, RIGHT_PAREN
    }

    // Node classes for the expression tree - unchanged
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

        @Override
        public String toString() {
            return identifier;
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

        @Override
        public String toString() {
            return "NOT(" + operand + ")";
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

        @Override
        public String toString() {
            return "(" + left + " AND " + right + ")";
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

        @Override
        public String toString() {
            return "(" + left + " OR " + right + ")";
        }
    }
}
