package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.etc.GroupBys1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.sources.ISources1;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;

public class QueryBlocks1 {
    public final ISources1<? extends ISources2<?>> sources;
    public final Conditions1 conditions;
    public final Conditions1 udfConditions; //User data filtering conditions
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;
    public final boolean yieldAll;

    public QueryBlocks1(final ISources1<? extends ISources2<?>> sources, final Conditions1 conditions, final Conditions1 udfConditions, final Yields1 yields, final GroupBys1 groups, final OrderBys1 orderings, final boolean yieldAll) {
        this.sources = sources;
        this.conditions = conditions;
        this.udfConditions = udfConditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
        this.yieldAll = yieldAll;
    }
}