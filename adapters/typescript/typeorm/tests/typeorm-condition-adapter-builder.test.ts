import { TypeORMConditionAdapterBuilder } from '../src/typeorm-condition-adapter-builder';
import { Operator } from '../../../../core/typescript/src/validation';

// Test types
enum TestPropertyRef {
  USER_NAME = 'USER_NAME',
  USER_AGE = 'USER_AGE',
  USER_EMAIL = 'USER_EMAIL',
  USER_STATUS = 'USER_STATUS'
}

// Mock implementation for testing
class MockTypeORMConditionAdapterBuilder implements TypeORMConditionAdapterBuilder<any, any> {
  build(ref: string, op: Operator, value: any): any {
    return {
      ref,
      operator: op,
      value,
      toString: () => `${ref} ${op} ${value}`,
      getRef: () => ref,
      getOperator: () => op,
      getValue: () => value
    };
  }
}

describe('TypeORMConditionAdapterBuilder', () => {
  let builder: MockTypeORMConditionAdapterBuilder;

  beforeEach(() => {
    builder = new MockTypeORMConditionAdapterBuilder();
  });

  describe('build', () => {
    it('should build condition with string value', () => {
      const condition = builder.build('USER_NAME', Operator.EQUALS, 'John');
      
      expect(condition).toBeDefined();
      expect(condition.getRef()).toBe('USER_NAME');
      expect(condition.getOperator()).toBe(Operator.EQUALS);
      expect(condition.getValue()).toBe('John');
      expect(condition.toString()).toBe('USER_NAME = John');
    });

    it('should build condition with number value', () => {
      const condition = builder.build('USER_AGE', Operator.GREATER_THAN, 18);
      
      expect(condition).toBeDefined();
      expect(condition.getRef()).toBe('USER_AGE');
      expect(condition.getOperator()).toBe(Operator.GREATER_THAN);
      expect(condition.getValue()).toBe(18);
      expect(condition.toString()).toBe('USER_AGE > 18');
    });

    it('should build condition with array value', () => {
      const condition = builder.build('USER_STATUS', Operator.IN, ['ACTIVE', 'PENDING']);
      
      expect(condition).toBeDefined();
      expect(condition.getRef()).toBe('USER_STATUS');
      expect(condition.getOperator()).toBe(Operator.IN);
      expect(condition.getValue()).toEqual(['ACTIVE', 'PENDING']);
      expect(condition.toString()).toBe('USER_STATUS IN ACTIVE,PENDING');
    });

    it('should build condition with null value', () => {
      const condition = builder.build('USER_EMAIL', Operator.IS_NULL, null);
      
      expect(condition).toBeDefined();
      expect(condition.getRef()).toBe('USER_EMAIL');
      expect(condition.getOperator()).toBe(Operator.IS_NULL);
      expect(condition.getValue()).toBeNull();
      expect(condition.toString()).toBe('USER_EMAIL IS NULL null');
    });

    it('should build condition with boolean value', () => {
      const condition = builder.build('USER_ACTIVE', Operator.EQUALS, true);
      
      expect(condition).toBeDefined();
      expect(condition.getRef()).toBe('USER_ACTIVE');
      expect(condition.getOperator()).toBe(Operator.EQUALS);
      expect(condition.getValue()).toBe(true);
      expect(condition.toString()).toBe('USER_ACTIVE = true');
    });

    it('should handle all operator types', () => {
      const operators = [
        Operator.EQUALS,
        Operator.NOT_EQUALS,
        Operator.LIKE,
        Operator.GREATER_THAN,
        Operator.LESS_THAN,
        Operator.BETWEEN,
        Operator.IN,
        Operator.NOT_IN,
        Operator.IS_NULL,
        Operator.IS_NOT_NULL
      ];

      operators.forEach((operator, index) => {
        const condition = builder.build(`PROPERTY_${index}`, operator, 'test');
        
        expect(condition).toBeDefined();
        expect(condition.getOperator()).toBe(operator);
      });
    });

    it('should handle complex value types', () => {
      const complexValue = {
        nested: {
          property: 'value',
          array: [1, 2, 3]
        }
      };

      const condition = builder.build('COMPLEX_PROPERTY', Operator.EQUALS, complexValue);
      
      expect(condition).toBeDefined();
      expect(condition.getValue()).toEqual(complexValue);
    });
  });

  describe('type safety', () => {
    it('should accept string ref parameter', () => {
      expect(() => {
        builder.build('VALID_REF', Operator.EQUALS, 'value');
      }).not.toThrow();
    });

    it('should handle empty string ref', () => {
      const condition = builder.build('', Operator.EQUALS, 'value');
      expect(condition.getRef()).toBe('');
    });

    it('should handle special characters in ref', () => {
      const condition = builder.build('USER_NAME_WITH_UNDERSCORES', Operator.EQUALS, 'value');
      expect(condition.getRef()).toBe('USER_NAME_WITH_UNDERSCORES');
    });
  });
});
