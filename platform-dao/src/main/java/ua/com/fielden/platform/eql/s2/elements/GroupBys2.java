package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;


public class GroupBys2 implements IElement2 {
    private final List<GroupBy2> groups;

    public GroupBys2(final List<GroupBy2> groups) {
	this.groups = groups;
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	for (final GroupBy2 group : groups) {
	    result.addAll(group.getOperand().getAllValues());
	}
	return result;
    }

    public List<GroupBy2> getGroups() {
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
	if (!(obj instanceof GroupBys2)) {
	    return false;
	}
	final GroupBys2 other = (GroupBys2) obj;
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