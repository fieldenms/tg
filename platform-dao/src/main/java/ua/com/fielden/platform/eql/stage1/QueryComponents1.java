package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.utils.ToString;

/**
 * Represents all structural query parts (components) obtained as the result of stage 0 (fluent API tokens) to stage 1 transformation.
 * This class is used as a convenience to keep all the query components together.
 *
 */
public record QueryComponents1(
                IJoinNode1<? extends IJoinNode2<?>> joinRoot,
                Conditions1 whereConditions,
                Conditions1 udfConditions,
                Yields1 yields,
                GroupBys1 groups,
                OrderBys1 orderings,
                boolean yieldAll,
                boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery) {

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
                .add("yieldAll", yieldAll)
                .add("shouldMaterialiseCalcPropsAsColumnsInSqlQuery", shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
                .addIfNotNull("joinRoot", joinRoot)
                .addIfNot("where", whereConditions, Conditions1::isEmpty)
                .addIfNot("udf", udfConditions, Conditions1::isEmpty)
                .addIfNot("yields", yields, Yields1::isEmpty)
                .addIfNot("groups", groups, GroupBys1::isEmpty)
                .addIfNot("orderings", orderings, OrderBys1::isEmpty)
                .$();
    }

}
