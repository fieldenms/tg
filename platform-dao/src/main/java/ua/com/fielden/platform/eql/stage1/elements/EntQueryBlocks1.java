package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;

public class EntQueryBlocks1 {
    public final Sources1 sources;
    public final Conditions1 conditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;

    public EntQueryBlocks1(final Sources1 sources, final Conditions1 conditions, final Yields1 yields, final GroupBys1 groups, final OrderBys1 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }
}