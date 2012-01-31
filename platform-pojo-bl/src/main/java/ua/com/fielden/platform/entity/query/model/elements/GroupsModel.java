package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Iterator;
import java.util.List;


public class GroupsModel {
    private final List<GroupModel> groups;

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

    public GroupsModel(final List<GroupModel> groups) {
	this.groups = groups;
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
