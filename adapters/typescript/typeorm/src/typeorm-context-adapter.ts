/**
 * TypeORM Context Adapter implementing the correct pattern.
 * This adapter uses TypeORMConditionAdapterBuilder to create conditions.
 * 
 * @template T The entity type (e.g., User, Product)
 * @template P The PropertyRef enum for this entity
 */

import { Condition, Context, FilterDefinition, PropertyRefEnum } from '../../../../core/typescript/src/interfaces';
import { TypeORMConditionAdapter } from './typeorm-condition-adapter';
import { TypeORMConditionAdapterBuilder } from './typeorm-condition-adapter-builder';

export class TypeORMContextAdapter<T, P extends PropertyRefEnum> implements Context {
    private readonly filters: Map<string, TypeORMConditionAdapter<T>>;
    private readonly conditionAdapterBuilder: TypeORMConditionAdapterBuilder<T, P>;

    constructor(conditionAdapterBuilder: TypeORMConditionAdapterBuilder<T, P>) {
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
