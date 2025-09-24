/**
 * Builder interface for creating TypeORM condition adapters.
 * Each implementation defines how to build a TypeORM condition from PropertyRef, Operator, and value.
 */

import { Operator, PropertyRef } from '@cyfko/dynamic-filter-core';
import { TypeORMConditionAdapter } from './typeorm-condition-adapter';

export interface TypeORMConditionAdapterBuilder<T, P extends PropertyRef> {
    /**
     * Builds a TypeORM condition adapter from the given parameters.
     * 
     * @param ref The property reference (type-safe)
     * @param op The operator
     * @param value The value as object
     * @return A TypeORM condition adapter
     */
    build(ref: P, op: Operator, value: any): TypeORMConditionAdapter<T>;
}
