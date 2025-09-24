/**
 * Core interfaces for the dynamic filtering system.
 * These interfaces define the contract that all implementations must follow.
 */

export interface FilterDefinition {
  ref: string;
  operator: string;
  value: any;
}

export interface FilterRequest {
  filters: Record<string, FilterDefinition>;
  combineWith: string;
}

export interface Condition {
  and(other: Condition): Condition;
  or(other: Condition): Condition;
  not(): Condition;
}

export interface Context {
  getCondition(filterKey: string): Condition | null;
}

export interface FilterTree {
  generate(context: Context): Condition;
}

export interface Parser {
  parse(dslExpression: string): FilterTree;
}

export interface FilterExecutor<T> {
  execute(globalCondition: Condition, entityClass: new () => T): Promise<T[]>;
}

export class DSLSyntaxException extends Error {
  constructor(message: string, cause?: Error) {
    super(message);
    this.name = 'DSLSyntaxException';
    if (cause) {
      this.cause = cause;
    }
  }
}

export class FilterValidationException extends Error {
  constructor(message: string, cause?: Error) {
    super(message);
    this.name = 'FilterValidationException';
    if (cause) {
      this.cause = cause;
    }
  }
}
