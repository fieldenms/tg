package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.etc.GroupBys1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;

/**
 * Represents all structural query parts (components) obtained as the result of stage 0 (fluent API tokens) to stage 1 transformation.
 * This class is used as a convenience to keep all the query components together.
 *
 */
public class QueryComponents1 {
    public final IJoinNode1<? extends IJoinNode2<?>> joinRoot;
    public final Conditions1 whereConditions;
    public final Conditions1 udfConditions; //User data filtering conditions
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;
    public final boolean yieldAll;
    public final boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery;

    public QueryComponents1(final IJoinNode1<? extends IJoinNode2<?>> joinRoot, final Conditions1 whereConditions, final Conditions1 udfConditions, final Yields1 yields, final GroupBys1 groups, final OrderBys1 orderings, final boolean yieldAll, final boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery) {
        this.joinRoot = joinRoot;
        this.whereConditions = whereConditions;
        this.udfConditions = udfConditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
        this.yieldAll = yieldAll;
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = shouldMaterialiseCalcPropsAsColumnsInSqlQuery;
    }
}