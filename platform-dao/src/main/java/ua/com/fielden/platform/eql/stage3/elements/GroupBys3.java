package ua.com.fielden.platform.eql.stage3.elements;

import java.util.List;
import java.util.Objects;

public class GroupBys3 {
    private final List<GroupBy3> groups;

    public GroupBys3(final List<GroupBy3> groups) {
        this.groups = groups;
    }

    public List<GroupBy3> getGroups() {
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

        if (!(obj instanceof GroupBys3)) {
            return false;
        }

        final GroupBys3 other = (GroupBys3) obj;
        
        return Objects.equals(groups, other.groups);
    }
}