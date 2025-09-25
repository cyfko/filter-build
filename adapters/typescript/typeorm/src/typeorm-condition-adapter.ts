/**
 * TypeORM Condition Adapter implementing the Condition interface.
 * This adapter wraps TypeORM query builder conditions.
 */

import { Condition } from '../../../../core/typescript/src/interfaces';
import { SelectQueryBuilder } from 'typeorm';

export class TypeORMConditionAdapter<T> implements Condition {
    constructor(private conditionBuilder: (qb: SelectQueryBuilder<T>) => SelectQueryBuilder<T>) {}

    applyToQuery(queryBuilder: SelectQueryBuilder<T>): SelectQueryBuilder<T> {
        return this.conditionBuilder(queryBuilder);
    }

    and(other: Condition): Condition {
        if (!(other instanceof TypeORMConditionAdapter)) {
            throw new Error('Cannot combine with non-TypeORM condition');
        }

        return new TypeORMConditionAdapter<T>((qb) => {
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

        return new TypeORMConditionAdapter<T>((qb) => {
            return qb.orWhere((subQb) => {
                this.conditionBuilder(subQb);
                other.conditionBuilder(subQb);
            });
        });
    }

    not(): Condition {
        return new TypeORMConditionAdapter<T>((qb) => {
            return qb.andWhere((subQb) => {
                subQb.where((notQb) => {
                    this.conditionBuilder(notQb);
                });
            });
        });
    }
}
