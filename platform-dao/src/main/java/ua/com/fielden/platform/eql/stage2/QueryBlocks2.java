package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class QueryBlocks2 {
    public final IJoinNode2<? extends IJoinNode3> joinRoot;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;

    public QueryBlocks2(final IJoinNode2<? extends IJoinNode3> joinRoot, final Conditions2 conditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        this.joinRoot = joinRoot;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}