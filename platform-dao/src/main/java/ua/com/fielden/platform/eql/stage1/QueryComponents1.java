package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Optional;

/**
 * Represents all structural components of a query obtained as a result of stage 0 to stage 1 transformation (parsing).
 * This class is used as a convenience to keep all the query components together.
 */
public record QueryComponents1(
                Optional<IJoinNode1<? extends IJoinNode2<?>>> maybeJoinRoot,
                Conditions1 whereConditions,
                Conditions1 udfConditions,
                Yields1 yields,
                GroupBys1 groups,
                OrderBys1 orderings,
                boolean yieldAll,
                boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
    implements ToString.IFormattable
{

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yieldAll", yieldAll)
                .add("shouldMaterialiseCalcPropsAsColumnsInSqlQuery", shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
                .addIfPresent("join", maybeJoinRoot)
                .addIfNot("where", whereConditions, Conditions1::isEmpty)
                .addIfNot("udf", udfConditions, Conditions1::isEmpty)
                .addIfNot("yields", yields, Yields1::isEmpty)
                .addIfNot("groups", groups, GroupBys1::isEmpty)
                .addIfNot("orderings", orderings, OrderBys1::isEmpty)
                .$();
    }

}
