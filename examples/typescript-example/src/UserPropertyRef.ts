import { PropertyRef, Operator } from '@cyfko/dynamic-filter-core';

/**
 * Example PropertyRef enum for User entity.
 * This shows how developers should define their own property references.
 * They only need to extend PropertyRef and call super() - all methods are inherited!
 */
export class UserPropertyRef extends PropertyRef {
  // User entity properties
  static readonly USER_NAME = new UserPropertyRef("userName", "string", [Operator.LIKE, Operator.EQUALS, Operator.IN, Operator.NOT_IN]);
  static readonly USER_EMAIL = new UserPropertyRef("email", "string", [Operator.LIKE, Operator.EQUALS, Operator.IN, Operator.NOT_IN]);
  static readonly USER_STATUS = new UserPropertyRef("status", "string", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN]);
  static readonly USER_AGE = new UserPropertyRef("age", "number", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN]);
  static readonly USER_CREATED_DATE = new UserPropertyRef("createdDate", "Date", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN]);
  static readonly USER_IS_ACTIVE = new UserPropertyRef("active", "boolean", [Operator.EQUALS, Operator.NOT_EQUALS]);
  static readonly USER_SALARY = new UserPropertyRef("salary", "number", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN]);

  private constructor(entityField: string, type: string, supportedOperators: Operator[]) {
    super(entityField, type, supportedOperators);
  }

  /**
   * Finds a UserPropertyRef by its entity field name.
   * 
   * @param entityField The entity field name to search for
   * @returns The matching UserPropertyRef or null if not found
   */
  static findByEntityField(entityField: string): UserPropertyRef | null {
    const allProperties = [
      UserPropertyRef.USER_NAME,
      UserPropertyRef.USER_EMAIL,
      UserPropertyRef.USER_STATUS,
      UserPropertyRef.USER_AGE,
      UserPropertyRef.USER_CREATED_DATE,
      UserPropertyRef.USER_IS_ACTIVE,
      UserPropertyRef.USER_SALARY
    ];

    return allProperties.find(prop => prop.entityField === entityField) || null;
  }

  /**
   * Gets all UserPropertyRef values.
   * 
   * @returns An array of all UserPropertyRef values
   */
  static getAllProperties(): UserPropertyRef[] {
    return [
      UserPropertyRef.USER_NAME,
      UserPropertyRef.USER_EMAIL,
      UserPropertyRef.USER_STATUS,
      UserPropertyRef.USER_AGE,
      UserPropertyRef.USER_CREATED_DATE,
      UserPropertyRef.USER_IS_ACTIVE,
      UserPropertyRef.USER_SALARY
    ];
  }
}
