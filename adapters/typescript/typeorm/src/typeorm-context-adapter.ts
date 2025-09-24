/**
 * TypeORM Context Adapter implementing the correct pattern.
 * This adapter uses TypeORMConditionAdapterBuilder to create conditions.
 */

import { Condition, Context, FilterDefinition } from '@cyfko/dynamic-filter-core';
import { Operator, PropertyRef } from '@cyfko/dynamic-filter-core';
import { TypeORMConditionAdapter } from './typeorm-condition-adapter';
import { TypeORMConditionAdapterBuilder } from './typeorm-condition-adapter-builder';

export class TypeORMContextAdapter<T, P extends PropertyRef> implements Context {
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

    getCondition(filterKey: string): Condition {
        const condition = this.filters.get(filterKey);
        if (!condition) {
            throw new Error(`No condition found for key: ${filterKey}`);
        }
        return condition;
    }

    /**
     * Resolves a String ref to the appropriate PropertyRef enum.
     * Each adapter can implement its own resolution logic.
     * This method should be overridden by concrete implementations to provide
     * the actual PropertyRef resolution strategy.
     */
    private resolvePropertyRef(ref: string): PropertyRef | null {
        // This is a placeholder implementation.
        // Concrete adapters should override this method to implement
        // their own PropertyRef resolution strategy.
        throw new Error('PropertyRef resolution must be implemented by concrete adapter implementations');
    }

    /**
     * Parses operator string to Operator enum.
     */
    private parseOperator(operatorStr: string): Operator | null {
        // This should use the core parseOperator function
        // For now, return null to indicate it needs implementation
        return null;
    }
}
