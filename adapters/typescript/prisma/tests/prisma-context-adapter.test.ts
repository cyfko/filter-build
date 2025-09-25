import { PrismaContextAdapter } from '../src/prisma-context-adapter';
import { PrismaConditionAdapterBuilder } from '../src/prisma-condition-adapter-builder';
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

// Mock PrismaConditionAdapterBuilder
class MockPrismaConditionAdapterBuilder implements PrismaConditionAdapterBuilder<any, any> {
  build(ref: string, op: Operator, value: any): any {
    return {
      ref,
      operator: op,
      value,
      toString: () => `${ref} ${op} ${value}`,
      toPrismaWhere: () => ({
        [ref]: this.convertToPrismaCondition(op, value)
      })
    };
  }

  private convertToPrismaCondition(op: Operator, value: any): any {
    switch (op) {
      case Operator.EQUALS:
        return { equals: value };
      case Operator.NOT_EQUALS:
        return { not: { equals: value } };
      case Operator.LIKE:
        return { contains: value };
      case Operator.GREATER_THAN:
        return { gt: value };
      case Operator.LESS_THAN:
        return { lt: value };
      case Operator.BETWEEN:
        return { gte: value[0], lte: value[1] };
      case Operator.IN:
        return { in: value };
      case Operator.NOT_IN:
        return { notIn: value };
      case Operator.IS_NULL:
        return null;
      case Operator.IS_NOT_NULL:
        return { not: null };
      default:
        return { equals: value };
    }
  }
}

describe('PrismaContextAdapter', () => {
  let adapter: PrismaContextAdapter<any, any>;
  let mockBuilder: MockPrismaConditionAdapterBuilder;

  beforeEach(() => {
    mockBuilder = new MockPrismaConditionAdapterBuilder();
    adapter = new PrismaContextAdapter(mockBuilder, TestPropertyRefImpl);
  });

  describe('addCondition', () => {
    it('should add condition successfully for valid property and operator', () => {
      const filterDefinition: FilterDefinition<any> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.EQUALS,
        value: 'John'
      };

      expect(() => {
        adapter.addCondition('nameFilter', filterDefinition);
      }).not.toThrow();
    });

    it('should throw error for unsupported operator', () => {
      const filterDefinition: FilterDefinition<any> = {
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
      const stringFilter: FilterDefinition<any> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.LIKE,
        value: 'John%'
      };

      const numberFilter: FilterDefinition<any> = {
        ref: TestPropertyRef.USER_AGE,
        operator: Operator.GREATER_THAN,
        value: 18
      };

      const arrayFilter: FilterDefinition<any> = {
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
      const filterDefinition: FilterDefinition<any> = {
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

      const ageFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
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

  describe('Prisma-specific functionality', () => {
    it('should store and retrieve conditions correctly', () => {
      const filterDefinition: FilterDefinition<any> = {
        ref: TestPropertyRef.USER_NAME,
        operator: Operator.EQUALS,
        value: 'John'
      };

      adapter.addCondition('nameFilter', filterDefinition);
      const condition = adapter.getCondition('nameFilter');

      expect(condition).toBeDefined();
      expect(condition).not.toBeNull();
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
        adapter.addCondition(key, definition as FilterDefinition<typeof TestPropertyRefImpl>);
      });

      conditions.forEach(({ key }) => {
        const condition = adapter.getCondition(key);
        expect(condition).toBeDefined();
      });
    });
  });
});
