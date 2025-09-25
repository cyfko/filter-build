/**
 * DSL Parser implementation for TypeScript/JavaScript.
 * Converts string expressions into FilterTree structures.
 */

import { FilterTree, Context, Condition, DSLSyntaxException } from './interfaces';

export enum TokenType {
  IDENTIFIER = 'IDENTIFIER',
  AND = 'AND',
  OR = 'OR',
  NOT = 'NOT',
  LEFT_PAREN = 'LEFT_PAREN',
  RIGHT_PAREN = 'RIGHT_PAREN'
}

export interface Token {
  type: TokenType;
  value: string;
}

export class DSLParser {
  parse(dslExpression: string): FilterTree {
    if (!dslExpression || dslExpression.trim().length === 0) {
      throw new DSLSyntaxException('DSL expression cannot be null or empty');
    }

    const trimmed = dslExpression.trim();
    return this.parseExpression(trimmed);
  }

  private parseExpression(expression: string): FilterTree {
    const tokens = this.tokenize(expression);
    return this.parseTokens(tokens);
  }

  private tokenize(expression: string): Token[] {
    const tokens: Token[] = [];
    let currentToken = '';

    for (let i = 0; i < expression.length; i++) {
      const c = expression[i];

      if (/\s/.test(c)) {
        // End current token if any
        if (currentToken.length > 0) {
          tokens.push({ type: TokenType.IDENTIFIER, value: currentToken });
          currentToken = '';
        }
        continue;
      }

      if (c === '&' || c === '|' || c === '!' || c === '(' || c === ')') {
        // End current token if any
        if (currentToken.length > 0) {
          tokens.push({ type: TokenType.IDENTIFIER, value: currentToken });
          currentToken = '';
        }

        // Add operator/parenthesis token
        const type = this.getTokenType(c);
        tokens.push({ type, value: c });
      } else if (/[a-zA-Z0-9_]/.test(c)) {
        currentToken += c;
      } else {
        throw new DSLSyntaxException(`Invalid character '${c}' at position ${i}`);
      }
    }

    // Add final token if any
    if (currentToken.length > 0) {
      tokens.push({ type: TokenType.IDENTIFIER, value: currentToken });
    }

    return tokens;
  }

  private getTokenType(c: string): TokenType {
    switch (c) {
      case '&': return TokenType.AND;
      case '|': return TokenType.OR;
      case '!': return TokenType.NOT;
      case '(': return TokenType.LEFT_PAREN;
      case ')': return TokenType.RIGHT_PAREN;
      default: throw new Error(`Unknown operator: ${c}`);
    }
  }

  private parseTokens(tokens: Token[]): FilterTree {
    if (tokens.length === 0) {
      throw new DSLSyntaxException('Empty expression');
    }

    // Convert to postfix notation using Shunting Yard algorithm
    const postfix = this.infixToPostfix(tokens);

    // Build expression tree from postfix notation
    return this.buildExpressionTree(postfix);
  }

  private infixToPostfix(tokens: Token[]): Token[] {
    const output: Token[] = [];
    const operators: Token[] = [];

    for (const token of tokens) {
      switch (token.type) {
        case TokenType.IDENTIFIER:
          output.push(token);
          break;
        case TokenType.NOT:
          operators.push(token);
          break;
        case TokenType.AND:
        case TokenType.OR:
          while (operators.length > 0 &&
                 operators[operators.length - 1].type !== TokenType.LEFT_PAREN &&
                 this.getPrecedence(operators[operators.length - 1].type) >= this.getPrecedence(token.type)) {
            output.push(operators.pop()!);
          }
          operators.push(token);
          break;
        case TokenType.LEFT_PAREN:
          operators.push(token);
          break;
        case TokenType.RIGHT_PAREN:
          while (operators.length > 0 && operators[operators.length - 1].type !== TokenType.LEFT_PAREN) {
            output.push(operators.pop()!);
          }
          if (operators.length === 0) {
            throw new DSLSyntaxException('Mismatched parentheses');
          }
          operators.pop(); // Remove left parenthesis
          break;
      }
    }

    while (operators.length > 0) {
      const op = operators.pop()!;
      if (op.type === TokenType.LEFT_PAREN) {
        throw new DSLSyntaxException('Mismatched parentheses');
      }
      output.push(op);
    }

    return output;
  }

  private getPrecedence(type: TokenType): number {
    switch (type) {
      case TokenType.NOT: return 3;
      case TokenType.AND: return 2;
      case TokenType.OR: return 1;
      default: return 0;
    }
  }

  private buildExpressionTree(postfix: Token[]): FilterTree {
    const stack: FilterTree[] = [];

    for (const token of postfix) {
      switch (token.type) {
        case TokenType.IDENTIFIER:
          stack.push(new IdentifierNode(token.value));
          break;
        case TokenType.NOT:
          if (stack.length === 0) {
            throw new DSLSyntaxException('Invalid expression: NOT operator without operand');
          }
          stack.push(new NotNode(stack.pop()!));
          break;
        case TokenType.AND:
          if (stack.length < 2) {
            throw new DSLSyntaxException('Invalid expression: AND operator requires two operands');
          }
          const rightAnd = stack.pop()!;
          const leftAnd = stack.pop()!;
          stack.push(new AndNode(leftAnd, rightAnd));
          break;
        case TokenType.OR:
          if (stack.length < 2) {
            throw new DSLSyntaxException('Invalid expression: OR operator requires two operands');
          }
          const rightOr = stack.pop()!;
          const leftOr = stack.pop()!;
          stack.push(new OrNode(leftOr, rightOr));
          break;
      }
    }

    if (stack.length !== 1) {
      throw new DSLSyntaxException('Invalid expression: malformed syntax');
    }

    return stack[0];
  }
}

// Node classes for the expression tree
class IdentifierNode implements FilterTree {
  constructor(private identifier: string) {}

  generate(context: Context): Condition {
    const condition = context.getCondition(this.identifier);
    if (!condition) {
      throw new Error(`No condition found for identifier: ${this.identifier}`);
    }
    return condition;
  }
}

class NotNode implements FilterTree {
  constructor(private operand: FilterTree) {}

  generate(context: Context): Condition {
    return this.operand.generate(context).not();
  }
}

class AndNode implements FilterTree {
  constructor(private left: FilterTree, private right: FilterTree) {}

  generate(context: Context): Condition {
    return this.left.generate(context).and(this.right.generate(context));
  }
}

class OrNode implements FilterTree {
  constructor(private left: FilterTree, private right: FilterTree) {}

  generate(context: Context): Condition {
    return this.left.generate(context).or(this.right.generate(context));
  }
}
