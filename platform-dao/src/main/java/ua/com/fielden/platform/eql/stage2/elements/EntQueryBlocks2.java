package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class EntQueryBlocks2 {
    final Sources2 sources;
    final Conditions2 conditions;
    final Yields2 yields;
    final GroupBys2 groups;
    final OrderBys2 orderings;

    public EntQueryBlocks2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields, final GroupBys2 groups, final OrderBys2 orderings) {
        super();
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }

    public Sources2 getSources() {
        return sources;
    }

    public Conditions2 getConditions() {
        return conditions;
    }

    public Yields2 getYields() {
        return yields;
    }

    public GroupBys2 getGroups() {
        return groups;
    }

    public OrderBys2 getOrderings() {
        return orderings;
    }
}
