package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class EntQueryBlocks2 {
    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;

    public EntQueryBlocks2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}