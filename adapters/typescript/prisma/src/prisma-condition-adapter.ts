/**
 * Prisma Condition Adapter implementing the Condition interface.
 * This adapter wraps Prisma query conditions.
 */

import { Condition } from '../../../../core/typescript/src/interfaces';

export class PrismaConditionAdapter<T> implements Condition {
    constructor(private whereCondition: any) {}

    getWhereCondition(): any {
        return this.whereCondition;
    }

    and(other: Condition): Condition {
        if (!(other instanceof PrismaConditionAdapter)) {
            throw new Error('Cannot combine with non-Prisma condition');
        }

        return new PrismaConditionAdapter<T>({
            AND: [this.whereCondition, other.whereCondition]
        });
    }

    or(other: Condition): Condition {
        if (!(other instanceof PrismaConditionAdapter)) {
            throw new Error('Cannot combine with non-Prisma condition');
        }

        return new PrismaConditionAdapter<T>({
            OR: [this.whereCondition, other.whereCondition]
        });
    }

    not(): Condition {
        return new PrismaConditionAdapter<T>({
            NOT: this.whereCondition
        });
    }
}
