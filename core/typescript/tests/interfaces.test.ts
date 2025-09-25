import { FilterDefinition, FilterRequest, PropertyRefEnum } from '../src/interfaces';
import { Operator, PropertyRef } from '../src/validation';

// Test types
enum TestPropertyRef {
  USER_NAME = 'USER_NAME',
  USER_AGE = 'USER_AGE',
  USER_EMAIL = 'USER_EMAIL',
  USER_STATUS = 'USER_STATUS'
}

// Create a simple PropertyRef implementation for testing
const TestPropertyRefImpl: PropertyRef = {
  type: 'string',
  supportedOperators: [Operator.EQUALS, Operator.LIKE, Operator.IN]
};

describe('Core Interfaces', () => {
  describe('FilterDefinition', () => {
    it('should create FilterDefinition with valid parameters', () => {
      const filter: FilterDefinition<PropertyRef> = {
        ref: 'USER_NAME',
        operator: Operator.EQUALS,
        value: 'John'
      };

      expect(filter.ref).toBe('USER_NAME');
      expect(filter.operator).toBe(Operator.EQUALS);
      expect(filter.value).toBe('John');
    });

    it('should create FilterDefinition with different value types', () => {
      const stringFilter: FilterDefinition<PropertyRef> = {
        ref: 'USER_NAME',
        operator: Operator.LIKE,
        value: 'John%'
      };

      const numberFilter: FilterDefinition<PropertyRef> = {
        ref: 'USER_AGE',
        operator: Operator.GREATER_THAN,
        value: 18
      };

      const arrayFilter: FilterDefinition<PropertyRef> = {
        ref: 'USER_STATUS',
        operator: Operator.IN,
        value: ['ACTIVE', 'PENDING']
      };

      expect(stringFilter.value).toBe('John%');
      expect(numberFilter.value).toBe(18);
      expect(arrayFilter.value).toEqual(['ACTIVE', 'PENDING']);
    });

    it('should create FilterDefinition with null value', () => {
      const filter: FilterDefinition<PropertyRef> = {
        ref: 'USER_EMAIL',
        operator: Operator.IS_NULL,
        value: null
      };

      expect(filter.value).toBeNull();
    });
  });

  describe('FilterRequest', () => {
    it('should create FilterRequest with multiple filters', () => {
      const nameFilter: FilterDefinition<PropertyRef> = {
        ref: 'USER_NAME',
        operator: Operator.LIKE,
        value: 'John%'
      };

      const ageFilter: FilterDefinition<PropertyRef> = {
        ref: 'USER_AGE',
        operator: Operator.GREATER_THAN,
        value: 18
      };

      const request: FilterRequest<PropertyRef> = {
        filters: {
          'nameFilter': nameFilter,
          'ageFilter': ageFilter
        },
        combineWith: 'AND'
      };

      expect(request.filters).toHaveProperty('nameFilter');
      expect(request.filters).toHaveProperty('ageFilter');
      expect(request.combineWith).toBe('AND');
      expect(Object.keys(request.filters)).toHaveLength(2);
    });

    it('should create FilterRequest with empty filters', () => {
      const request: FilterRequest<PropertyRef> = {
        filters: {},
        combineWith: 'OR'
      };

      expect(request.filters).toEqual({});
      expect(request.combineWith).toBe('OR');
    });
  });

  describe('PropertyRefEnum type constraint', () => {
    it('should accept valid PropertyRef enum', () => {
      const validEnum: PropertyRefEnum<any> = {
        [TestPropertyRef.USER_NAME]: TestPropertyRefImpl
      };

      expect(validEnum[TestPropertyRef.USER_NAME].type).toBe('string');
      expect(validEnum[TestPropertyRef.USER_NAME].supportedOperators).toContain(Operator.EQUALS);
    });
  });

  describe('Type safety', () => {
    it('should enforce PropertyRef types in FilterDefinition', () => {
      // This test ensures TypeScript compilation works correctly
      const filter: FilterDefinition<PropertyRef> = {
        ref: 'USER_NAME', // Must be a string key
        operator: Operator.EQUALS,
        value: 'test'
      };

      // The following would cause TypeScript compilation error:
      // ref: 123 as any, // This should not compile

      expect(filter.ref).toBe('USER_NAME');
    });
  });
});
