package ua.com.fielden.platform.eql.stage3.elements;

import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.sources.Sources3;

public class EntQueryBlocks3 {
    public final Sources3 sources;
    public final Conditions3 conditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    
    public EntQueryBlocks3(final Sources3 sources, final Conditions3 conditions, final Yields3 yields, final GroupBys3 groups, final OrderBys3 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields =  yields;
        this.groups =  groups;
        this.orderings = orderings;
    }
}