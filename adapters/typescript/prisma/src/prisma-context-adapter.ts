/**
 * Prisma Context Adapter implementing the correct pattern.
 * This adapter uses PrismaConditionAdapterBuilder to create conditions.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */

import { Condition, ContextAdapter, FilterDefinition, PropertyRefEnum } from '../../../../core/typescript/src/interfaces';
import { PropertyRefUtils } from '../../../../core/typescript/src/validation';
import { PrismaConditionAdapter } from './prisma-condition-adapter';
import { PrismaConditionAdapterBuilder } from './prisma-condition-adapter-builder';

export class PrismaContextAdapter<T, P extends PropertyRefEnum> implements ContextAdapter<T, P> {
    private readonly filters: Map<string, PrismaConditionAdapter<T>>;
    private readonly conditionAdapterBuilder: PrismaConditionAdapterBuilder<T, P>;
    private readonly propertyRefImpl: P;

    constructor(conditionAdapterBuilder: PrismaConditionAdapterBuilder<T, P>, propertyRefImpl: P) {
        this.filters = new Map();
        this.conditionAdapterBuilder = conditionAdapterBuilder;
        this.propertyRefImpl = propertyRefImpl;
    }

    addCondition(filterKey: string, definition: FilterDefinition<P>): void {
        // Get PropertyRef and Operator directly (type-safe, no resolution needed)
        const propertyRefKey = definition.ref;
        const operator = definition.operator;
        
        // Get the PropertyRef implementation
        const propertyRef = (this.propertyRefImpl as any)[propertyRefKey];
        if (!propertyRef || typeof propertyRef !== 'object' || !('supportedOperators' in propertyRef)) {
            throw new Error(`Invalid PropertyRef: ${String(propertyRefKey)}`);
        }

        // Validate that the property supports this operator
        PropertyRefUtils.validateOperator(propertyRef, operator);

        // Build condition using the builder and store it
        const condition = this.conditionAdapterBuilder.build(propertyRefKey as any, operator, definition.value);
        this.filters.set(filterKey, condition);
    }

    getCondition(filterKey: string): Condition | null {
        return this.filters.get(filterKey) || null;
    }
}
