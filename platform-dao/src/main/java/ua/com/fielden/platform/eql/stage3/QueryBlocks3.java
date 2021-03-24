package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.core.GroupBys3;
import ua.com.fielden.platform.eql.stage3.core.OrderBys3;
import ua.com.fielden.platform.eql.stage3.core.Yields3;
import ua.com.fielden.platform.eql.stage3.sources.IQrySources3;

public class QueryBlocks3 {
    public final IQrySources3 sources;
    public final Conditions3 conditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    
    public QueryBlocks3(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final GroupBys3 groups, final OrderBys3 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields =  yields;
        this.groups =  groups;
        this.orderings = orderings;
    }
}