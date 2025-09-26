/**
 * Builder interface for creating Prisma condition adapters.
 * Each implementation defines how to build a Prisma condition from PropertyRef, Operator, and value.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */

import { Operator, PropertyRef } from '../../../../core/typescript/src/validation';
import { PropertyRefEnum } from '../../../../core/typescript/src/interfaces';
import { PrismaConditionAdapter } from './prisma-condition-adapter';

export interface PrismaConditionAdapterBuilder<T, P extends PropertyRefEnum> {
    /**
     * Builds a Prisma condition adapter from the given parameters.
     * 
     * @param ref The property reference object
     * @param op The operator
     * @param value The value as object
     * @return A Prisma condition adapter
     */
    build(ref: PropertyRef, op: Operator, value: any): PrismaConditionAdapter<T>;
}
