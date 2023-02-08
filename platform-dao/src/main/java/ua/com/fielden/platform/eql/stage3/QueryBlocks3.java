package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class QueryBlocks3 {
    public final IJoinNode3 joinRoot;
    public final Conditions3 conditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;

    public QueryBlocks3(final IJoinNode3 joinRoot, final Conditions3 conditions, final Yields3 yields, final GroupBys3 groups, final OrderBys3 orderings) {
        this.joinRoot = joinRoot;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}