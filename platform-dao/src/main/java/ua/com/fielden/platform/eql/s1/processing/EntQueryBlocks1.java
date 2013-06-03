package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.GroupBys1;
import ua.com.fielden.platform.eql.s1.elements.OrderBys1;
import ua.com.fielden.platform.eql.s1.elements.Sources1;
import ua.com.fielden.platform.eql.s1.elements.Yields1;


public class EntQueryBlocks1 {
    final Sources1 sources;
    final Conditions1 conditions;
    final Yields1 yields;
    final GroupBys1 groups;
    final OrderBys1 orderings;

    public EntQueryBlocks1(final Sources1 sources, final Conditions1 conditions, final Yields1 yields, final GroupBys1 groups, final OrderBys1 orderings) {
	super();
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.orderings = orderings;
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
