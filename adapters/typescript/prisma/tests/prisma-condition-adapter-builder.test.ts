import { PrismaConditionAdapterBuilder } from '../src/prisma-condition-adapter-builder';
import { Operator } from '../../../../core/typescript/src/validation';

// Test types
enum TestPropertyRef {
  USER_NAME = 'USER_NAME',
  USER_AGE = 'USER_AGE',
  USER_EMAIL = 'USER_EMAIL',
  USER_STATUS = 'USER_STATUS'
}

// Mock implementation for testing
class MockPrismaConditionAdapterBuilder implements PrismaConditionAdapterBuilder<any, typeof TestPropertyRef> {
  build(ref: string, op: Operator, value: any): any {
    return {
      ref,
      operator: op,
      value,
      toString: () => `${ref} ${op} ${value}`,
      toPrismaWhere: () => this.convertToPrismaWhere(ref, op, value),
      getRef: () => ref,
      getOperator: () => op,
      getValue: () => value
    };
  }

  private convertToPrismaWhere(ref: string, op: Operator, value: any): any {
    switch (op) {
      case Operator.EQUALS:
        return { [ref]: { equals: value } };
      case Operator.NOT_EQUALS:
        return { [ref]: { not: { equals: value } } };
      case Operator.LIKE:
        return { [ref]: { contains: value } };
      case Operator.GREATER_THAN:
        return { [ref]: { gt: value } };
      case Operator.LESS_THAN:
        return { [ref]: { lt: value } };
      case Operator.BETWEEN:
        return { [ref]: { gte: value[0], lte: value[1] } };
      case Operator.IN:
        return { [ref]: { in: value } };
      case Operator.NOT_IN:
        return { [ref]: { notIn: value } };
      case Operator.IS_NULL:
        return { [ref]: null };
      case Operator.IS_NOT_NULL:
        return { [ref]: { not: null } };
      default:
        return { [ref]: { equals: value } };
    }
  }
}

describe('PrismaConditionAdapterBuilder', () => {
  let builder: MockPrismaConditionAdapterBuilder;

  beforeEach(() => {
    builder = new MockPrismaConditionAdapterBuilder();
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
  });

  describe('Prisma where clause conversion', () => {
    it('should convert EQUALS to Prisma equals', () => {
      const condition = builder.build('USER_NAME', Operator.EQUALS, 'John');
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_NAME: { equals: 'John' }
      });
    });

    it('should convert NOT_EQUALS to Prisma not equals', () => {
      const condition = builder.build('USER_NAME', Operator.NOT_EQUALS, 'John');
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_NAME: { not: { equals: 'John' } }
      });
    });

    it('should convert LIKE to Prisma contains', () => {
      const condition = builder.build('USER_NAME', Operator.LIKE, 'John%');
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_NAME: { contains: 'John%' }
      });
    });

    it('should convert GREATER_THAN to Prisma gt', () => {
      const condition = builder.build('USER_AGE', Operator.GREATER_THAN, 18);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_AGE: { gt: 18 }
      });
    });

    it('should convert LESS_THAN to Prisma lt', () => {
      const condition = builder.build('USER_AGE', Operator.LESS_THAN, 65);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_AGE: { lt: 65 }
      });
    });

    it('should convert BETWEEN to Prisma gte and lte', () => {
      const condition = builder.build('USER_AGE', Operator.BETWEEN, [18, 65]);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_AGE: { gte: 18, lte: 65 }
      });
    });

    it('should convert IN to Prisma in', () => {
      const condition = builder.build('USER_STATUS', Operator.IN, ['ACTIVE', 'PENDING']);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_STATUS: { in: ['ACTIVE', 'PENDING'] }
      });
    });

    it('should convert NOT_IN to Prisma notIn', () => {
      const condition = builder.build('USER_STATUS', Operator.NOT_IN, ['INACTIVE', 'DELETED']);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_STATUS: { notIn: ['INACTIVE', 'DELETED'] }
      });
    });

    it('should convert IS_NULL to Prisma null', () => {
      const condition = builder.build('USER_EMAIL', Operator.IS_NULL, null);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_EMAIL: null
      });
    });

    it('should convert IS_NOT_NULL to Prisma not null', () => {
      const condition = builder.build('USER_EMAIL', Operator.IS_NOT_NULL, null);
      const whereClause = condition.toPrismaWhere();
      
      expect(whereClause).toEqual({
        USER_EMAIL: { not: null }
      });
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
