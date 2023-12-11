package ua.com.fielden.platform.eql.stage3.sundries;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class GroupBys3 {
    private final List<GroupBy3> groups;

    public GroupBys3(final List<GroupBy3> groups) {
        this.groups = groups;
    }

    public String sql(final DbVersion dbVersion) {
        return groups.stream().map(y -> y.sql(dbVersion)).collect(joining(", "));    
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

        if (!(obj instanceof GroupBys3)) {
            return false;
        }

        final GroupBys3 other = (GroupBys3) obj;
        
        return Objects.equals(groups, other.groups);
    }
}