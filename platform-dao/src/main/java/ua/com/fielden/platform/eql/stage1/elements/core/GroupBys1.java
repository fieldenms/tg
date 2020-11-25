package ua.com.fielden.platform.eql.stage1.elements.core;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.core.GroupBys2;

public class GroupBys1 {
    private final List<GroupBy1> groups;

    public GroupBys1(final List<GroupBy1> groups) {
        this.groups = groups;
    }

    public GroupBys2 transform(final PropsResolutionContext context) {
        return new GroupBys2(groups.stream().map(el -> el.transform(context)).collect(toList()));
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
        if (!(obj instanceof GroupBys1)) {
            return false;
        }

        final GroupBys1 other = (GroupBys1) obj;
        
        return Objects.equals(groups, other.groups);
    }
}