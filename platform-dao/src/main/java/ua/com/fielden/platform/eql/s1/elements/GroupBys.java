package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;


public class GroupBys implements IElement<ua.com.fielden.platform.eql.s2.elements.GroupBys> {
    private final List<GroupBy> groups;

    public GroupBys(final List<GroupBy> groups) {
	this.groups = groups;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.GroupBys transform(final TransformatorToS2 resolver) {
	final List<ua.com.fielden.platform.eql.s2.elements.GroupBy> transformed = new ArrayList<>();
	for (final GroupBy groupBy : groups) {
	    transformed.add(new ua.com.fielden.platform.eql.s2.elements.GroupBy(groupBy.getOperand().transform(resolver)));
	}
	return new ua.com.fielden.platform.eql.s2.elements.GroupBys(transformed);
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final GroupBy group : groups) {
	    result.addAll(group.getOperand().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final GroupBy group : groups) {
	    result.addAll(group.getOperand().getLocalProps());
	}
	return result;
    }

    public List<GroupBy> getGroups() {
        return groups;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((groups == null) ? 0 : groups.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof GroupBys)) {
	    return false;
	}
	final GroupBys other = (GroupBys) obj;
	if (groups == null) {
	    if (other.groups != null) {
		return false;
	    }
	} else if (!groups.equals(other.groups)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}