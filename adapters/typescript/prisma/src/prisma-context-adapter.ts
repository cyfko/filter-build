/**
 * Prisma Context Adapter implementing the correct pattern.
 * This adapter uses PrismaConditionAdapterBuilder to create conditions.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */

import { Condition, Context, FilterDefinition, PropertyRefEnum } from '../../../../core/typescript/src/interfaces';
import { PrismaConditionAdapter } from './prisma-condition-adapter';
import { PrismaConditionAdapterBuilder } from './prisma-condition-adapter-builder';

export class PrismaContextAdapter<T, P extends PropertyRefEnum> implements Context {
    private readonly filters: Map<string, PrismaConditionAdapter<T>>;
    private readonly conditionAdapterBuilder: PrismaConditionAdapterBuilder<T, P>;

    constructor(conditionAdapterBuilder: PrismaConditionAdapterBuilder<T, P>) {
        this.filters = new Map();
        this.conditionAdapterBuilder = conditionAdapterBuilder;
    }

    addCondition(filterKey: string, definition: FilterDefinition<P>): void {
        // Get PropertyRef and Operator directly (type-safe, no resolution needed)
        const propertyRef = definition.ref;
        const operator = definition.operator;
        
        // Validate that the property supports this operator
        propertyRef.validateOperator(operator);

        // Build condition using the builder and store it
        const condition = this.conditionAdapterBuilder.build(propertyRef, operator, definition.value);
        this.filters.set(filterKey, condition);
    }

    getCondition(filterKey: string): Condition | null {
        return this.filters.get(filterKey) || null;
    }
}
