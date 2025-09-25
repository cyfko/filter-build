/**
 * Builder interface for creating TypeORM condition adapters.
 * Each implementation defines how to build a TypeORM condition from PropertyRef, Operator, and value.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */

import { Operator, PropertyRefEnum } from '@cyfko/dynamic-filter-core';
import { TypeORMConditionAdapter } from './typeorm-condition-adapter';

export interface TypeORMConditionAdapterBuilder<T, P extends PropertyRefEnum> {
    /**
     * Builds a TypeORM condition adapter from the given parameters.
     * 
     * @param ref The property reference key (type-safe enum key)
     * @param op The operator
     * @param value The value as object
     * @return A TypeORM condition adapter
     */
    build(ref: keyof P, op: Operator, value: any): TypeORMConditionAdapter<T>;
}
