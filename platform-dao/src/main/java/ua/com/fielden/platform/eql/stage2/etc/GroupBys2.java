package ua.com.fielden.platform.eql.stage2.etc;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.etc.GroupBy3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;

public class GroupBys2 {
    public static final GroupBys2 EMPTY_GROUP_BYS = new GroupBys2(emptyList());
    
    private final List<GroupBy2> groups;

    public GroupBys2(final List<GroupBy2> groups) {
        this.groups = groups;
    }

    public TransformationResult2<GroupBys3> transform(final TransformationContext2 context) {
        if (groups.isEmpty()) {
            return new TransformationResult2<>(null, context);
        }
        
        final List<GroupBy3> transformed = new ArrayList<>();
        TransformationContext2 currentContext = context;
        for (final GroupBy2 groupBy : groups) {
            final TransformationResult2<GroupBy3> groupByTr = groupBy.transform(currentContext);
            transformed.add(groupByTr.item);
            currentContext = groupByTr.updatedContext;
        }
        return new TransformationResult2<>(new GroupBys3(transformed), currentContext);
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final GroupBy2 group : groups) {
            result.addAll(group.operand.collectProps());
        }
        return result;
    }
    
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return groups.isEmpty() ? emptySet() : groups.stream().map(el -> el.operand.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
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