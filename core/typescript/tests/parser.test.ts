import { DSLParser, TokenType } from '../src/parser';
import { FilterTree, Context, Condition } from '../src/interfaces';

describe('DSL Parser', () => {
  let parser: DSLParser;

  beforeEach(() => {
    parser = new DSLParser();
  });

  describe('Tokenization', () => {
    it('should tokenize simple expressions', () => {
      const tokens = parser['tokenize']('f1 & f2');
      
      expect(tokens).toHaveLength(3);
      expect(tokens[0]).toEqual({ type: TokenType.IDENTIFIER, value: 'f1' });
      expect(tokens[1]).toEqual({ type: TokenType.AND, value: '&' });
      expect(tokens[2]).toEqual({ type: TokenType.IDENTIFIER, value: 'f2' });
    });

    it('should tokenize complex expressions with parentheses', () => {
      const tokens = parser['tokenize']('(f1 & f2) | !f3');
      
      expect(tokens).toHaveLength(8);
      expect(tokens[0]).toEqual({ type: TokenType.LEFT_PAREN, value: '(' });
      expect(tokens[1]).toEqual({ type: TokenType.IDENTIFIER, value: 'f1' });
      expect(tokens[2]).toEqual({ type: TokenType.AND, value: '&' });
      expect(tokens[3]).toEqual({ type: TokenType.IDENTIFIER, value: 'f2' });
      expect(tokens[4]).toEqual({ type: TokenType.RIGHT_PAREN, value: ')' });
      expect(tokens[5]).toEqual({ type: TokenType.OR, value: '|' });
      expect(tokens[6]).toEqual({ type: TokenType.NOT, value: '!' });
      expect(tokens[7]).toEqual({ type: TokenType.IDENTIFIER, value: 'f3' });
    });

    it('should handle symbols correctly', () => {
      const tokens = parser['tokenize']('f1 & f2 | f3');
      
      expect(tokens[1]).toEqual({ type: TokenType.AND, value: '&' });
      expect(tokens[3]).toEqual({ type: TokenType.OR, value: '|' });
    });
  });

  describe('Parsing', () => {
    it('should parse simple AND expression', () => {
      const filterTree = parser.parse('f1 & f2');
      expect(filterTree).toBeDefined();
    });

    it('should parse simple OR expression', () => {
      const filterTree = parser.parse('f1 | f2');
      expect(filterTree).toBeDefined();
    });

    it('should parse NOT expression', () => {
      const filterTree = parser.parse('!f1');
      expect(filterTree).toBeDefined();
    });

    it('should parse complex expression with parentheses', () => {
      const filterTree = parser.parse('(f1 & f2) | !f3');
      expect(filterTree).toBeDefined();
    });

    it('should handle nested parentheses', () => {
      const filterTree = parser.parse('((f1 & f2) | f3) & !f4');
      expect(filterTree).toBeDefined();
    });
  });

  describe('Error handling', () => {
    it('should throw error for invalid syntax', () => {
      expect(() => {
        parser.parse('f1 &');
      }).toThrow();
    });

    it('should throw error for mismatched parentheses', () => {
      expect(() => {
        parser.parse('(f1 & f2');
      }).toThrow();
    });

    it('should throw error for empty expression', () => {
      expect(() => {
        parser.parse('');
      }).toThrow();
    });

    it('should throw error for invalid characters', () => {
      expect(() => {
        parser.parse('f1 @ f2');
      }).toThrow();
    });
  });

  describe('FilterTree generation', () => {
    class MockContext implements Context {
      private conditions: Map<string, Condition> = new Map();

      addCondition(key: string, condition: Condition) {
        this.conditions.set(key, condition);
      }

      getCondition(filterKey: string): Condition | null {
        return this.conditions.get(filterKey) || null;
      }
    }

    class MockCondition implements Condition {
      constructor(private value: string) {}

      and(other: Condition): Condition {
        return new MockCondition(`(${this.value} AND ${(other as MockCondition).value})`);
      }

      or(other: Condition): Condition {
        return new MockCondition(`(${this.value} OR ${(other as MockCondition).value})`);
      }

      not(): Condition {
        return new MockCondition(`NOT ${this.value}`);
      }

      toString(): string {
        return this.value;
      }
    }

    it('should generate condition from simple expression', () => {
      const filterTree = parser.parse('f1 & f2');
      const context = new MockContext();
      
      context.addCondition('f1', new MockCondition('condition1'));
      context.addCondition('f2', new MockCondition('condition2'));

      const condition = filterTree.generate(context);
      expect(condition.toString()).toBe('(condition1 AND condition2)');
    });

    it('should generate condition from complex expression', () => {
      const filterTree = parser.parse('(f1 & f2) | !f3');
      const context = new MockContext();
      
      context.addCondition('f1', new MockCondition('condition1'));
      context.addCondition('f2', new MockCondition('condition2'));
      context.addCondition('f3', new MockCondition('condition3'));

      const condition = filterTree.generate(context);
      expect(condition.toString()).toBe('((condition1 AND condition2) OR NOT condition3)');
    });

    it('should handle missing conditions gracefully', () => {
      const filterTree = parser.parse('f1 & f2');
      const context = new MockContext();
      
      context.addCondition('f1', new MockCondition('condition1'));
      // f2 is missing

      expect(() => {
        filterTree.generate(context);
      }).toThrow();
    });
  });
});
