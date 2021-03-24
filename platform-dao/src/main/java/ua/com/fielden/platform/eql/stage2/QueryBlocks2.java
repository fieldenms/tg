package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.core.GroupBys2;
import ua.com.fielden.platform.eql.stage2.core.OrderBys2;
import ua.com.fielden.platform.eql.stage2.core.Yields2;
import ua.com.fielden.platform.eql.stage2.sources.QrySources2;

public class QueryBlocks2 {
    public final QrySources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;

    public QueryBlocks2(final QrySources2 sources, final Conditions2 conditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}