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

export interface PropertyRef {
  name: string;
  type: string;
  nullable: boolean;
}

export class PropertyRegistry {
  private properties: Map<string, PropertyRef> = new Map();

  registerProperty(name: string, type: string, nullable: boolean = true): void {
    this.properties.set(name, { name, type, nullable });
  }

  getProperty(name: string): PropertyRef | null {
    return this.properties.get(name) || null;
  }

  hasProperty(name: string): boolean {
    return this.properties.has(name);
  }

  getPropertyNames(): string[] {
    return Array.from(this.properties.keys());
  }

  getAllProperties(): Map<string, PropertyRef> {
    return new Map(this.properties);
  }

  clear(): void {
    this.properties.clear();
  }

  size(): number {
    return this.properties.size;
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
