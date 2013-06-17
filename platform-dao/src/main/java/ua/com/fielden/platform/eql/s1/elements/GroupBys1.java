package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.GroupBy2;
import ua.com.fielden.platform.eql.s2.elements.GroupBys2;


public class GroupBys1 implements IElement1<GroupBys2> {
    private final List<GroupBy1> groups;

    public GroupBys1(final List<GroupBy1> groups) {
	this.groups = groups;
    }

    @Override
    public GroupBys2 transform(final TransformatorToS2 resolver) {
	final List<GroupBy2> transformed = new ArrayList<>();
	for (final GroupBy1 groupBy : groups) {
	    transformed.add(new GroupBy2(groupBy.getOperand().transform(resolver)));
	}
	return new GroupBys2(transformed);
    }

    public List<GroupBy1> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	if (groups.size() > 0) {
	    sb.append("\nGROUP BY ");
	}
	for (final Iterator<GroupBy1> iterator = groups.iterator(); iterator.hasNext();) {
	    sb.append(iterator.next());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}

	return sb.toString();
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
	if (!(obj instanceof GroupBys1)) {
	    return false;
	}
	final GroupBys1 other = (GroupBys1) obj;
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