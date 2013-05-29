package ua.com.fielden.platform.eql.s2.elements;



public class EntQueryBlocks {
    final Sources sources;
    final Conditions conditions;
    final Yields yields;
    final GroupBys groups;
    final OrderBys orderings;

    public EntQueryBlocks(final Sources sources, final Conditions conditions, final Yields yields, final GroupBys groups, final OrderBys orderings) {
	super();
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.orderings = orderings;
    }

    public Sources getSources() {
	return sources;
    }

    public Conditions getConditions() {
	return conditions;
    }

    public Yields getYields() {
	return yields;
    }

    public GroupBys getGroups() {
	return groups;
    }

    public OrderBys getOrderings() {
	return orderings;
    }
}
