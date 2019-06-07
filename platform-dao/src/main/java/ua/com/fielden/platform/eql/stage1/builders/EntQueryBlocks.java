package ua.com.fielden.platform.eql.stage1.builders;

import static java.util.Collections.emptyList;

import ua.com.fielden.platform.eql.stage1.elements.GroupBys1;
import ua.com.fielden.platform.eql.stage1.elements.OrderBys1;
import ua.com.fielden.platform.eql.stage1.elements.Yields1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;

public class EntQueryBlocks {
    final Sources1 sources;
    final Conditions1 conditions;
    final Yields1 yields;
    final GroupBys1 groups;
    final OrderBys1 orderings;

    public EntQueryBlocks(final Sources1 sources, final Conditions1 conditions, final Yields1 yields, final GroupBys1 groups, final OrderBys1 orderings) {
        this.sources = sources;
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
    }

    public EntQueryBlocks(final Sources1 sources, final Conditions1 conditions) {
        this(sources, conditions, new Yields1(emptyList()), new GroupBys1(emptyList()), new OrderBys1(emptyList()));
    }

    public Sources1 getSources() {
        return sources;
    }

    public Conditions1 getConditions() {
        return conditions;
    }

    public Yields1 getYields() {
        return yields;
    }

    public GroupBys1 getGroups() {
        return groups;
    }

    public OrderBys1 getOrderings() {
        return orderings;
    }
}
