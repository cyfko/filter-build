/**
 * Prisma adapter for dynamic filtering.
 * Converts core conditions into Prisma where clauses.
 */

import { Condition, Context, FilterDefinition, FilterExecutor } from '@cyfko/dynamic-filter-core';
import { PrismaClient } from '@prisma/client';

export interface PrismaCondition extends Condition {
  getWhereClause(): any;
}

export class PrismaConditionAdapter implements PrismaCondition {
  constructor(private whereClause: any) {}

  getWhereClause(): any {
    return this.whereClause;
  }

  and(other: Condition): Condition {
    if (!(other instanceof PrismaConditionAdapter)) {
      throw new Error('Cannot combine with non-Prisma condition');
    }

    return new PrismaConditionAdapter({
      AND: [this.whereClause, other.whereClause]
    });
  }

  or(other: Condition): Condition {
    if (!(other instanceof PrismaConditionAdapter)) {
      throw new Error('Cannot combine with non-Prisma condition');
    }

    return new PrismaConditionAdapter({
      OR: [this.whereClause, other.whereClause]
    });
  }

  not(): Condition {
    return new PrismaConditionAdapter({
      NOT: this.whereClause
    });
  }
}

export class PrismaContextAdapter implements Context {
  constructor(
    private filters: Record<string, FilterDefinition>,
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
    if (!propertyRef) {
      throw new Error(`Property not found: ${filter.ref}`);
    }

    // Validate operator
    const operator = parseOperator(filter.operator);
    if (!operator) {
      throw new Error(`Invalid operator: ${filter.operator}`);
    }

    // Create Prisma where clause
    const whereClause = this.createWhereClause(filter.ref, operator, filter.value, propertyRef.type);

    return new PrismaConditionAdapter(whereClause);
  }

  private createWhereClause(propertyRef: string, operator: Operator, value: any, expectedType: string): any {
    const fieldPath = this.getFieldPath(propertyRef);

    switch (operator) {
      case Operator.EQUALS:
        return { [fieldPath]: value };

      case Operator.NOT_EQUALS:
        return { [fieldPath]: { not: value } };

      case Operator.GREATER_THAN:
        return { [fieldPath]: { gt: value } };

      case Operator.GREATER_THAN_OR_EQUAL:
        return { [fieldPath]: { gte: value } };

      case Operator.LESS_THAN:
        return { [fieldPath]: { lt: value } };

      case Operator.LESS_THAN_OR_EQUAL:
        return { [fieldPath]: { lte: value } };

      case Operator.LIKE:
        return { [fieldPath]: { contains: value } };

      case Operator.NOT_LIKE:
        return { [fieldPath]: { not: { contains: value } } };

      case Operator.IN:
        return { [fieldPath]: { in: Array.isArray(value) ? value : [value] } };

      case Operator.NOT_IN:
        return { [fieldPath]: { notIn: Array.isArray(value) ? value : [value] } };

      case Operator.IS_NULL:
        return { [fieldPath]: null };

      case Operator.IS_NOT_NULL:
        return { [fieldPath]: { not: null } };

      case Operator.BETWEEN:
        if (Array.isArray(value) && value.length === 2) {
          return {
            [fieldPath]: {
              gte: value[0],
              lte: value[1]
            }
          };
        } else {
          throw new Error('BETWEEN operator requires exactly 2 values');
        }

      case Operator.NOT_BETWEEN:
        if (Array.isArray(value) && value.length === 2) {
          return {
            [fieldPath]: {
              NOT: {
                gte: value[0],
                lte: value[1]
              }
            }
          };
        } else {
          throw new Error('NOT BETWEEN operator requires exactly 2 values');
        }

      default:
        throw new Error(`Unsupported operator: ${operator}`);
    }
  }

  private getFieldPath(propertyRef: string): string {
    // Convert property reference to Prisma field path
    // For nested fields, use dot notation
    return propertyRef.replace(/\./g, '.');
  }
}

export class PrismaFilterExecutor<T> implements FilterExecutor<T> {
  constructor(private prisma: PrismaClient, private modelName: string) {}

  async execute(globalCondition: Condition, entityClass: new () => T): Promise<T[]> {
    if (!(globalCondition instanceof PrismaConditionAdapter)) {
      throw new Error('Condition must be a PrismaConditionAdapter');
    }

    const whereClause = globalCondition.getWhereClause();

    // Use dynamic model access
    const model = (this.prisma as any)[this.modelName];
    if (!model) {
      throw new Error(`Model ${this.modelName} not found in Prisma client`);
    }

    return await model.findMany({
      where: whereClause
    });
  }
}

export class PrismaFilterService<T> {
  constructor(
    private parser: any, // Parser from core
    private prisma: PrismaClient,
    private modelName: string
  ) {}

  async executeFilter(filterRequest: any, entityClass: new () => T): Promise<T[]> {
    // Parse DSL expression
    const filterTree = this.parser.parse(filterRequest.combineWith);

    // Create Prisma context

    // Generate global condition
    const globalCondition = filterTree.generate(context);

    // Execute filter
    const executor = new PrismaFilterExecutor<T>(this.prisma, this.modelName);
    return await executor.execute(globalCondition, entityClass);
  }

  createFilteredQuery(filterRequest: any): any {
    // Parse DSL expression
    const filterTree = this.parser.parse(filterRequest.combineWith);

    // Create Prisma context

    // Generate global condition
    const globalCondition = filterTree.generate(context);

    if (globalCondition instanceof PrismaConditionAdapter) {
      return {
        where: globalCondition.getWhereClause()
      };
    }

    throw new Error('Invalid condition type');
  }
}
