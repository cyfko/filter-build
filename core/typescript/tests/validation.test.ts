import { Operator, PropertyRef, PropertyRefUtils } from '../src/validation';

// Test PropertyRef implementation
const TestPropertyRefImpl = {
  USER_NAME: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.LIKE, Operator.IN]
  },
  USER_AGE: {
    type: 'number',
    supportedOperators: [Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.BETWEEN]
  },
  USER_EMAIL: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.LIKE]
  },
  USER_STATUS: {
    type: 'string',
    supportedOperators: [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN]
  }
};

enum TestPropertyRef {
  USER_NAME = 'USER_NAME',
  USER_AGE = 'USER_AGE',
  USER_EMAIL = 'USER_EMAIL',
  USER_STATUS = 'USER_STATUS'
}

describe('PropertyRef Interface and Utils', () => {
  describe('PropertyRef interface', () => {
    it('should have required properties', () => {
      const propertyRef: PropertyRef = {
        type: 'string',
        supportedOperators: [Operator.EQUALS, Operator.LIKE]
      };

      expect(propertyRef.type).toBe('string');
      expect(propertyRef.supportedOperators).toEqual([Operator.EQUALS, Operator.LIKE]);
    });
  });

  describe('PropertyRefUtils', () => {
    const propertyRef: PropertyRef = {
      type: 'string',
      supportedOperators: [Operator.EQUALS, Operator.LIKE, Operator.IN]
    };

    describe('supportsOperator', () => {
      it('should return true for supported operators', () => {
        expect(PropertyRefUtils.supportsOperator(propertyRef, Operator.EQUALS)).toBe(true);
        expect(PropertyRefUtils.supportsOperator(propertyRef, Operator.LIKE)).toBe(true);
        expect(PropertyRefUtils.supportsOperator(propertyRef, Operator.IN)).toBe(true);
      });

      it('should return false for unsupported operators', () => {
        expect(PropertyRefUtils.supportsOperator(propertyRef, Operator.GREATER_THAN)).toBe(false);
        expect(PropertyRefUtils.supportsOperator(propertyRef, Operator.BETWEEN)).toBe(false);
      });
    });

    describe('validateOperator', () => {
      it('should not throw for supported operators', () => {
        expect(() => {
          PropertyRefUtils.validateOperator(propertyRef, Operator.EQUALS);
        }).not.toThrow();

        expect(() => {
          PropertyRefUtils.validateOperator(propertyRef, Operator.LIKE);
        }).not.toThrow();
      });

      it('should throw for unsupported operators', () => {
        expect(() => {
          PropertyRefUtils.validateOperator(propertyRef, Operator.GREATER_THAN);
        }).toThrow('Operator \'>\' is not supported for this property');
      });
    });

    describe('getDescription', () => {
      it('should return formatted description', () => {
        const description = PropertyRefUtils.getDescription(propertyRef, 'USER_NAME');
        expect(description).toBe('USER_NAME (string)');
      });
    });
  });

  describe('TestPropertyRef enum', () => {
    it('should have all expected values', () => {
      expect(TestPropertyRef.USER_NAME).toBe('USER_NAME');
      expect(TestPropertyRef.USER_AGE).toBe('USER_AGE');
      expect(TestPropertyRef.USER_EMAIL).toBe('USER_EMAIL');
      expect(TestPropertyRef.USER_STATUS).toBe('USER_STATUS');
    });
  });

  describe('TestPropertyRefImpl', () => {
    it('should have PropertyRef implementations for all enum values', () => {
      expect(TestPropertyRefImpl[TestPropertyRef.USER_NAME]).toBeDefined();
      expect(TestPropertyRefImpl[TestPropertyRef.USER_AGE]).toBeDefined();
      expect(TestPropertyRefImpl[TestPropertyRef.USER_EMAIL]).toBeDefined();
      expect(TestPropertyRefImpl[TestPropertyRef.USER_STATUS]).toBeDefined();
    });

    it('should have correct types for each property', () => {
      expect(TestPropertyRefImpl[TestPropertyRef.USER_NAME].type).toBe('string');
      expect(TestPropertyRefImpl[TestPropertyRef.USER_AGE].type).toBe('number');
      expect(TestPropertyRefImpl[TestPropertyRef.USER_EMAIL].type).toBe('string');
      expect(TestPropertyRefImpl[TestPropertyRef.USER_STATUS].type).toBe('string');
    });

    it('should have correct supported operators', () => {
      const userNameRef = TestPropertyRefImpl[TestPropertyRef.USER_NAME];
      expect(userNameRef.supportedOperators).toContain(Operator.EQUALS);
      expect(userNameRef.supportedOperators).toContain(Operator.LIKE);
      expect(userNameRef.supportedOperators).toContain(Operator.IN);

      const userAgeRef = TestPropertyRefImpl[TestPropertyRef.USER_AGE];
      expect(userAgeRef.supportedOperators).toContain(Operator.EQUALS);
      expect(userAgeRef.supportedOperators).toContain(Operator.GREATER_THAN);
      expect(userAgeRef.supportedOperators).toContain(Operator.LESS_THAN);
      expect(userAgeRef.supportedOperators).toContain(Operator.BETWEEN);
    });
  });
});
