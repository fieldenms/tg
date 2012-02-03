package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GroupsModel implements IPropertyCollector {
    private final List<GroupModel> groups;

    public GroupsModel(final List<GroupModel> groups) {
	this.groups = groups;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final GroupModel group : groups) {
	    result.addAll(group.getOperand().getAllValues());
	}
	return result;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final GroupModel group : groups) {
	    result.addAll(group.getOperand().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final GroupModel group : groups) {
	    result.addAll(group.getOperand().getLocalProps());
	}
	return result;
    }

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	if (groups.size() > 0) {
	    sb.append("\nGROUP BY ");
	}
	for (final Iterator<GroupModel> iterator = groups.iterator(); iterator.hasNext();) {
	    sb.append(iterator.next().sql());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}

	return sb.toString();
    }

    public List<GroupModel> getGroups() {
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
	if (!(obj instanceof GroupsModel)) {
	    return false;
	}
	final GroupsModel other = (GroupsModel) obj;
	if (groups == null) {
	    if (other.groups != null) {
		return false;
	    }
	} else if (!groups.equals(other.groups)) {
	    return false;
	}
	return true;
    }
}