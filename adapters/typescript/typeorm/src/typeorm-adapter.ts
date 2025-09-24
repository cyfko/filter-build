/**
 * TypeORM adapter for dynamic filtering.
 * Converts core conditions into TypeORM query builder conditions.
 */

import { Condition, Context, FilterDefinition, FilterExecutor } from '@cyfko/dynamic-filter-core';
import { Operator, PropertyRegistry, parseOperator } from '@cyfko/dynamic-filter-core';
import { SelectQueryBuilder, Repository } from 'typeorm';

export interface TypeORMCondition extends Condition {
  applyToQuery(queryBuilder: SelectQueryBuilder<any>): SelectQueryBuilder<any>;
}

export class TypeORMConditionAdapter implements TypeORMCondition {
  constructor(private conditionBuilder: (qb: SelectQueryBuilder<any>) => SelectQueryBuilder<any>) {}

  applyToQuery(queryBuilder: SelectQueryBuilder<any>): SelectQueryBuilder<any> {
    return this.conditionBuilder(queryBuilder);
  }

  and(other: Condition): Condition {
    if (!(other instanceof TypeORMConditionAdapter)) {
      throw new Error('Cannot combine with non-TypeORM condition');
    }

    return new TypeORMConditionAdapter((qb) => {
      return qb.andWhere((subQb) => {
        this.conditionBuilder(subQb);
        other.conditionBuilder(subQb);
      });
    });
  }

  or(other: Condition): Condition {
    if (!(other instanceof TypeORMConditionAdapter)) {
      throw new Error('Cannot combine with non-TypeORM condition');
    }

    return new TypeORMConditionAdapter((qb) => {
      return qb.orWhere((subQb) => {
        this.conditionBuilder(subQb);
        other.conditionBuilder(subQb);
      });
    });
  }

  not(): Condition {
    return new TypeORMConditionAdapter((qb) => {
      return qb.andWhere((subQb) => {
        subQb.where((notQb) => {
          this.conditionBuilder(notQb);
        });
      });
    });
  }
}

export class TypeORMContextAdapter implements Context {
  constructor(
    private filters: Record<string, FilterDefinition>,
    private propertyRegistry: PropertyRegistry
  ) {}

  getCondition(filterKey: string): Condition | null {
    const filter = this.filters[filterKey];
    if (!filter) {
      throw new Error(`No filter found for key: ${filterKey}`);
    }

    return this.createCondition(filter);
  }

  private createCondition(filter: FilterDefinition): Condition {
    // Validate property reference
    const propertyRef = this.propertyRegistry.getProperty(filter.ref);
    if (!propertyRef) {
      throw new Error(`Property not found: ${filter.ref}`);
    }

    // Validate operator
    const operator = parseOperator(filter.operator);
    if (!operator) {
      throw new Error(`Invalid operator: ${filter.operator}`);
    }

    // Create TypeORM condition
    const conditionBuilder = this.createConditionBuilder(filter.ref, operator, filter.value, propertyRef.type);

    return new TypeORMConditionAdapter(conditionBuilder);
  }

  private createConditionBuilder(propertyRef: string, operator: Operator, value: any, expectedType: string) {
    return (qb: SelectQueryBuilder<any>) => {
      const fieldPath = this.getFieldPath(propertyRef);

      switch (operator) {
        case Operator.EQUALS:
          return qb.andWhere(`${fieldPath} = :value`, { value });

        case Operator.NOT_EQUALS:
          return qb.andWhere(`${fieldPath} != :value`, { value });

        case Operator.GREATER_THAN:
          return qb.andWhere(`${fieldPath} > :value`, { value });

        case Operator.GREATER_THAN_OR_EQUAL:
          return qb.andWhere(`${fieldPath} >= :value`, { value });

        case Operator.LESS_THAN:
          return qb.andWhere(`${fieldPath} < :value`, { value });

        case Operator.LESS_THAN_OR_EQUAL:
          return qb.andWhere(`${fieldPath} <= :value`, { value });

        case Operator.LIKE:
          return qb.andWhere(`${fieldPath} LIKE :value`, { value: `%${value}%` });

        case Operator.NOT_LIKE:
          return qb.andWhere(`${fieldPath} NOT LIKE :value`, { value: `%${value}%` });

        case Operator.IN:
          const inValues = Array.isArray(value) ? value : [value];
          return qb.andWhere(`${fieldPath} IN (:...values)`, { values: inValues });

        case Operator.NOT_IN:
          const notInValues = Array.isArray(value) ? value : [value];
          return qb.andWhere(`${fieldPath} NOT IN (:...values)`, { values: notInValues });

        case Operator.IS_NULL:
          return qb.andWhere(`${fieldPath} IS NULL`);

        case Operator.IS_NOT_NULL:
          return qb.andWhere(`${fieldPath} IS NOT NULL`);

        case Operator.BETWEEN:
          if (Array.isArray(value) && value.length === 2) {
            return qb.andWhere(`${fieldPath} BETWEEN :from AND :to`, { from: value[0], to: value[1] });
          } else {
            throw new Error('BETWEEN operator requires exactly 2 values');
          }

        case Operator.NOT_BETWEEN:
          if (Array.isArray(value) && value.length === 2) {
            return qb.andWhere(`${fieldPath} NOT BETWEEN :from AND :to`, { from: value[0], to: value[1] });
          } else {
            throw new Error('NOT BETWEEN operator requires exactly 2 values');
          }

        default:
          throw new Error(`Unsupported operator: ${operator}`);
      }
    };
  }

  private getFieldPath(propertyRef: string): string {
    // Convert property reference to TypeORM field path
    // For nested fields, use dot notation with proper aliasing
    return propertyRef.replace(/\./g, '.');
  }
}

export class TypeORMFilterExecutor<T> implements FilterExecutor<T> {
  constructor(private repository: Repository<T>) {}

  async execute(globalCondition: Condition, entityClass: new () => T): Promise<T[]> {
    if (!(globalCondition instanceof TypeORMConditionAdapter)) {
      throw new Error('Condition must be a TypeORMConditionAdapter');
    }

    const queryBuilder = this.repository.createQueryBuilder();
    const finalQuery = globalCondition.applyToQuery(queryBuilder);

    return await finalQuery.getMany();
  }
}

export class TypeORMFilterService<T> {
  constructor(
    private parser: any, // Parser from core
    private propertyRegistry: PropertyRegistry,
    private repository: Repository<T>
  ) {}

  async executeFilter(filterRequest: any, entityClass: new () => T): Promise<T[]> {
    // Parse DSL expression
    const filterTree = this.parser.parse(filterRequest.combineWith);

    // Create TypeORM context
    const context = new TypeORMContextAdapter(filterRequest.filters, this.propertyRegistry);

    // Generate global condition
    const globalCondition = filterTree.generate(context);

    // Execute filter
    const executor = new TypeORMFilterExecutor<T>(this.repository);
    return await executor.execute(globalCondition, entityClass);
  }

  createFilteredQueryBuilder(filterRequest: any): SelectQueryBuilder<T> {
    // Parse DSL expression
    const filterTree = this.parser.parse(filterRequest.combineWith);

    // Create TypeORM context
    const context = new TypeORMContextAdapter(filterRequest.filters, this.propertyRegistry);

    // Generate global condition
    const globalCondition = filterTree.generate(context);

    if (globalCondition instanceof TypeORMConditionAdapter) {
      const queryBuilder = this.repository.createQueryBuilder();
      return globalCondition.applyToQuery(queryBuilder);
    }

    throw new Error('Invalid condition type');
  }
}
