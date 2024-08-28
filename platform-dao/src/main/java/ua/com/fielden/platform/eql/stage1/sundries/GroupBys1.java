package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public record GroupBys1 (List<GroupBy1> groups) implements ToString.IFormattable {

    public static final GroupBys1 EMPTY_GROUP_BYS = new GroupBys1(emptyList());
    
    public static GroupBys1 groupBys(final List<GroupBy1> groups) {
        return groups.isEmpty() ? EMPTY_GROUP_BYS : new GroupBys1(groups);
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public GroupBys2 transform(final TransformationContextFromStage1To2 context) {
        if (groups.isEmpty()) {
            return GroupBys2.EMPTY_GROUP_BYS;
        } else {
            return new GroupBys2(groups.stream().map(el -> el.transform(context)).collect(toList()));    
        }
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return groups.isEmpty() ? emptySet() : groups.stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("groups", groups)
                .$();
    }

}
