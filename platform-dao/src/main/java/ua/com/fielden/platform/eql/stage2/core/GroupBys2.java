package ua.com.fielden.platform.eql.stage2.core;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.core.GroupBy3;
import ua.com.fielden.platform.eql.stage3.core.GroupBys3;

public class GroupBys2 {
    private final List<GroupBy2> groups;

    public GroupBys2(final List<GroupBy2> groups) {
        this.groups = groups;
    }

    public TransformationResult<GroupBys3> transform(final TransformationContext context) {
            final List<GroupBy3> transformed = new ArrayList<>();
            TransformationContext currentContext = context;
            for (final GroupBy2 groupBy : groups) {
                final TransformationResult<GroupBy3> groupByTr = groupBy.transform(currentContext);
                transformed.add(groupByTr.item);
                currentContext = groupByTr.updatedContext;
            }
            return new TransformationResult<GroupBys3>(new GroupBys3(transformed), currentContext);
    }
    
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        for (final GroupBy2 group : groups) {
            result.addAll(group.operand.collectProps());
        }
        return result;
    }
    
    public List<GroupBy2> getGroups() {
        return unmodifiableList(groups);
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

        if (!(obj instanceof GroupBys2)) {
            return false;
        }

        final GroupBys2 other = (GroupBys2) obj;
        
        return Objects.equals(groups, other.groups);
    }
}