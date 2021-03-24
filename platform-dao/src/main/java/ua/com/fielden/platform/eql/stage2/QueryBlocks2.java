package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.sources.Sources2;

public class QueryBlocks2 {
    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;

    public QueryBlocks2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}