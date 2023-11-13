package ua.com.fielden.platform.eql.stage1.sundries;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;

public class GroupBys1 {
    public static final GroupBys1 EMPTY_GROUP_BYS = new GroupBys1(emptyList());
    
    private final List<GroupBy1> groups;

    public GroupBys1(final List<GroupBy1> groups) {
        this.groups = groups;
    }

    public GroupBys2 transform(final TransformationContextFromStage1To2 context) {
        if (groups.isEmpty()) {
            return GroupBys2.EMPTY_GROUP_BYS;
        } else {
            return new GroupBys2(groups.stream().map(el -> el.transform(context)).collect(toList()));    
        }
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return groups.isEmpty() ? emptySet() : groups.stream().map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
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