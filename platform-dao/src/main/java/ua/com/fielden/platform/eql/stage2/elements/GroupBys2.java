package ua.com.fielden.platform.eql.stage2.elements;

import java.util.ArrayList;
import java.util.List;

public class GroupBys2 {
    private final List<GroupBy2> groups = new ArrayList<>();

    public GroupBys2(final List<GroupBy2> groups) {
        this.groups.addAll(groups);
    }

    public List<GroupBy2> getGroups() {
        return groups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groups.hashCode();
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


}