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
 * Base class for property references.
 * Developers should create their own enums extending this class to define
 * the properties available for their entities.
 * 
 * Example usage:
 * ```typescript
 * enum UserPropertyRef extends PropertyRef {
 *   USER_NAME = new PropertyRef("userName", "string", [Operator.LIKE, Operator.EQUALS]),
 *   USER_AGE = new PropertyRef("age", "number", [Operator.EQUALS, Operator.GREATER_THAN]);
 * }
 * ```
 */
export abstract class PropertyRef {
  constructor(
    public readonly entityField: string,
    public readonly type: string,
    public readonly supportedOperators: Operator[]
  ) {}

  /**
   * Checks if this property supports the given operator.
   */
  supportsOperator(operator: Operator): boolean {
    return this.supportedOperators.includes(operator);
  }

  /**
   * Validates that the given operator is supported by this property.
   */
  validateOperator(operator: Operator): void {
    if (!this.supportsOperator(operator)) {
      throw new Error(
        `Operator '${operator}' is not supported for property '${this.entityField}'. ` +
        `Supported operators: ${this.supportedOperators.join(', ')}`
      );
    }
  }

  /**
   * Gets a human-readable description of this property reference.
   */
  getDescription(): string {
    return `${this.constructor.name}.${this.entityField} (${this.type})`;
  }

  toString(): string {
    return `PropertyRef{entityField='${this.entityField}', type=${this.type}, supportedOperators=[${this.supportedOperators.join(', ')}]}`;
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
