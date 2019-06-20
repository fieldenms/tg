package ua.com.fielden.platform.eql.stage2.elements;

import java.util.List;
import java.util.Objects;

public class GroupBys2 {
    private final List<GroupBy2> groups;

    public GroupBys2(final List<GroupBy2> groups) {
        this.groups = groups;
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

        if (!(obj instanceof GroupBys2)) {
            return false;
        }

        final GroupBys2 other = (GroupBys2) obj;
        
        return Objects.equals(groups, other.groups);
    }
}