import { TypeORMContextAdapter } from '../src/typeorm-context-adapter';
import { TypeORMConditionAdapterBuilder } from '../src/typeorm-condition-adapter-builder';
import { FilterDefinition, PropertyRefEnum } from '../../../../core/typescript/src/interfaces';
import { Operator, PropertyRef } from '../../../../core/typescript/src/validation';

// Test types
enum TestPropertyRef {
  USER_NAME = 'USER_NAME',
  USER_AGE = 'USER_AGE',
  USER_EMAIL = 'USER_EMAIL',
  USER_STATUS = 'USER_STATUS'
}

const TestPropertyRefImpl = {
  [TestPropertyRef.USER_NAME]: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.LIKE, Operator.IN]
  },
  [TestPropertyRef.USER_AGE]: {
    type: 'number',
    supportedOperators: [Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.BETWEEN]
  },
  [TestPropertyRef.USER_EMAIL]: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.LIKE]
  },
  [TestPropertyRef.USER_STATUS]: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN]
  }
};

// Mock TypeORMConditionAdapterBuilder
class MockTypeORMConditionAdapterBuilder implements TypeORMConditionAdapterBuilder<any, typeof TestPropertyRefImpl> {
  build(ref: string, op: Operator, value: any): any {
    return {
      ref,
      operator: op,
      value,
      toString: () => `${ref} ${op} ${value}`
    };
  }
}

describe('TypeORMContextAdapter', () => {
  let adapter: TypeORMContextAdapter<any, typeof TestPropertyRefImpl>;
  let mockBuilder: MockTypeORMConditionAdapterBuilder;

  beforeEach(() => {
    mockBuilder = new MockTypeORMConditionAdapterBuilder();
    adapter = new TypeORMContextAdapter(mockBuilder, TestPropertyRefImpl);
  });

  describe('addCondition', () => {
    it('should add condition successfully for valid property and operator', () => {
      const filterDefinition: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.EQUALS,
        value: 'John'
      };

      expect(() => {
        adapter.addCondition('nameFilter', filterDefinition);
      }).not.toThrow();
    });

    it('should throw error for unsupported operator', () => {
      const filterDefinition: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.GREATER_THAN, // Not supported for USER_NAME
        value: 'John'
      };

      expect(() => {
        adapter.addCondition('nameFilter', filterDefinition);
      }).toThrow('Operator \'>\' is not supported for this property');
    });

    it('should throw error for invalid property ref', () => {
      const filterDefinition: FilterDefinition<any> = {
        ref: 'INVALID_PROPERTY' as any,
        operator: Operator.EQUALS,
        value: 'John'
      };

      expect(() => {
        adapter.addCondition('nameFilter', filterDefinition);
      }).toThrow('Invalid PropertyRef: INVALID_PROPERTY');
    });

    it('should handle different value types', () => {
      const stringFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.LIKE,
        value: 'John%'
      };

      const numberFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_AGE,
        operator: Operator.GREATER_THAN,
        value: 18
      };

      const arrayFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_STATUS,
        operator: Operator.IN,
        value: ['ACTIVE', 'PENDING']
      };

      expect(() => {
        adapter.addCondition('stringFilter', stringFilter);
        adapter.addCondition('numberFilter', numberFilter);
        adapter.addCondition('arrayFilter', arrayFilter);
      }).not.toThrow();
    });
  });

  describe('getCondition', () => {
    it('should return null for non-existent filter key', () => {
      const condition = adapter.getCondition('nonExistent');
      expect(condition).toBeNull();
    });

    it('should return condition for existing filter key', () => {
      const filterDefinition: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.EQUALS,
        value: 'John'
      };

      adapter.addCondition('nameFilter', filterDefinition);
      const condition = adapter.getCondition('nameFilter');

      expect(condition).toBeDefined();
      expect(condition?.toString()).toBe('USER_NAME = John');
    });

    it('should return different conditions for different filter keys', () => {
      const nameFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.EQUALS,
        value: 'John'
      };

      const ageFilter: FilterDefinition<any> = {
        ref: TestPropertyRef.USER_AGE,
        operator: Operator.GREATER_THAN,
        value: 18
      };

      adapter.addCondition('nameFilter', nameFilter);
      adapter.addCondition('ageFilter', ageFilter);

      const nameCondition = adapter.getCondition('nameFilter');
      const ageCondition = adapter.getCondition('ageFilter');

      expect(nameCondition?.toString()).toBe('USER_NAME = John');
      expect(ageCondition?.toString()).toBe('USER_AGE > 18');
    });
  });

  describe('integration', () => {
    it('should handle multiple conditions with different operators', () => {
      const conditions = [
        { key: 'nameFilter', definition: { ref: TestPropertyRef.USER_NAME, operator: Operator.LIKE, value: 'John%' } },
        { key: 'ageFilter', definition: { ref: TestPropertyRef.USER_AGE, operator: Operator.BETWEEN, value: [18, 65] } },
        { key: 'emailFilter', definition: { ref: TestPropertyRef.USER_EMAIL, operator: Operator.EQUALS, value: 'john@example.com' } },
        { key: 'statusFilter', definition: { ref: TestPropertyRef.USER_STATUS, operator: Operator.IN, value: ['ACTIVE', 'PENDING'] } }
      ];

      conditions.forEach(({ key, definition }) => {
        adapter.addCondition(key, definition as FilterDefinition<any>);
      });

      conditions.forEach(({ key }) => {
        const condition = adapter.getCondition(key);
        expect(condition).toBeDefined();
      });
    });
  });
});
