/**
 * Core interfaces for the dynamic filtering system.
 * These interfaces define the contract that all implementations must follow.
 */

import { PropertyRef, Operator } from './validation';

/**
 * Type constraint for PropertyRef enums
 */
export type PropertyRefEnum<T = any> = Record<string, PropertyRef> & T;

export interface FilterDefinition<P extends PropertyRefEnum> {
  ref: string;
  operator: Operator;
  value: any;
}

export interface FilterRequest<P extends PropertyRefEnum> {
  filters: Record<string, FilterDefinition<P>>;
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

/**
 * Context adapter interface for type-safe filter building.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */
export interface ContextAdapter<T, P extends PropertyRefEnum> extends Context {
  addCondition(filterKey: string, definition: FilterDefinition<P>): void;
}

/**
 * Builder interface for creating condition adapters.
 * Each implementation defines how to build a condition from PropertyRef, Operator, and value.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */
export interface ConditionAdapterBuilder<T, P extends PropertyRefEnum> {
  build(ref: string, operator: Operator, value: any): Condition;
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
  public readonly cause?: Error;
  
  constructor(message: string, cause?: Error) {
    super(message);
    this.name = 'DSLSyntaxException';
    if (cause) {
      this.cause = cause;
    }
  }
}

export class FilterValidationException extends Error {
  public readonly cause?: Error;
  
  constructor(message: string, cause?: Error) {
    super(message);
    this.name = 'FilterValidationException';
    if (cause) {
      this.cause = cause;
    }
  }
}

