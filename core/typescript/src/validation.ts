/**
 * Validation utilities for the dynamic filtering system.
 */

export enum Operator {
  EQUALS = '=',
  NOT_EQUALS = '!=',
  GREATER_THAN = '>',
  GREATER_THAN_OR_EQUAL = '>=',
  LESS_THAN = '<',
  LESS_THAN_OR_EQUAL = '<=',
  LIKE = 'LIKE',
  NOT_LIKE = 'NOT LIKE',
  IN = 'IN',
  NOT_IN = 'NOT IN',
  IS_NULL = 'IS NULL',
  IS_NOT_NULL = 'IS NOT NULL',
  BETWEEN = 'BETWEEN',
  NOT_BETWEEN = 'NOT BETWEEN'
}

/**
 * Interface for property references in dynamic filtering.
 * 
 * Developers should create their own enums implementing this interface to define
 * the properties available for their entities.
 * 
 * Example usage:
 * ```typescript
 * enum UserPropertyRef implements PropertyRef {
 *   USER_NAME = "USER_NAME",
 *   USER_AGE = "USER_AGE",
 *   USER_EMAIL = "USER_EMAIL"
 * }
 * 
 * // Enum values implement PropertyRef interface
 * Object.assign(UserPropertyRef, {
 *   [UserPropertyRef.USER_NAME]: {
 *     type: "string",
 *     supportedOperators: [Operator.LIKE, Operator.EQUALS]
 *   },
 *   [UserPropertyRef.USER_AGE]: {
 *     type: "number", 
 *     supportedOperators: [Operator.EQUALS, Operator.GREATER_THAN]
 *   }
 * });
 * ```
 */
export interface PropertyRef {
  readonly type: string;
  readonly supportedOperators: readonly Operator[];
  validateOperator(operator: Operator): void;
}

/**
 * Utility functions for PropertyRef
 */
export namespace PropertyRefUtils {
  /**
   * Checks if this property supports the given operator.
   */
  export function supportsOperator(propertyRef: PropertyRef, operator: Operator): boolean {
    return propertyRef.supportedOperators.includes(operator);
  }

  /**
   * Validates that the given operator is supported by this property.
   */
  export function validateOperator(propertyRef: PropertyRef, operator: Operator): void {
    if (!supportsOperator(propertyRef, operator)) {
      throw new Error(
        `Operator '${operator}' is not supported for this property. ` +
        `Supported operators: ${propertyRef.supportedOperators.join(', ')}`
      );
    }
  }

  /**
   * Gets a human-readable description of this property reference.
   */
  export function getDescription(propertyRef: PropertyRef, name: string): string {
    return `${name} (${propertyRef.type})`;
  }
}


export function parseOperator(value: string): Operator | null {
  if (!value) return null;

  const trimmed = value.trim().toUpperCase();
  
  for (const op of Object.values(Operator)) {
    if (op === value || op === trimmed) {
      return op;
    }
  }

  return null;
}

export function operatorRequiresValue(operator: Operator): boolean {
  return operator !== Operator.IS_NULL && operator !== Operator.IS_NOT_NULL;
}

export function operatorSupportsMultipleValues(operator: Operator): boolean {
  return [Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN].includes(operator);
}
