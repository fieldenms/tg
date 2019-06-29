package ua.com.fielden.platform.eql.stage2.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.GroupBy3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBys3;

public class GroupBys2 {
    private final List<GroupBy2> groups;

    public GroupBys2(final List<GroupBy2> groups) {
        this.groups = groups;
    }

    public TransformationResult<GroupBys3> transform(final TransformationContext context) {
            final List<GroupBy3> transformed = new ArrayList<>();
            TransformationContext currentResolutionContext = context;
            for (final GroupBy2 groupBy : groups) {
                final TransformationResult<GroupBy3> groupByTransformationResult = groupBy.transform(currentResolutionContext);
                transformed.add(groupByTransformationResult.item);
                currentResolutionContext = groupByTransformationResult.updatedContext;
            }
            return new TransformationResult<GroupBys3>(new GroupBys3(transformed), currentResolutionContext);
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