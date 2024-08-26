package ua.com.fielden.platform.eql.stage2.sundries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBy3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3.skipTransformation;

public record GroupBys2 (List<GroupBy2> groups) {

    public static final GroupBys2 EMPTY_GROUP_BYS = new GroupBys2(emptyList());

    public TransformationResultFromStage2To3<GroupBys3> transform(final TransformationContextFromStage2To3 context) {
        if (groups.isEmpty()) {
            return skipTransformation(context);
        }

        final List<GroupBy3> transformed = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;
        for (final GroupBy2 groupBy : groups) {
            final TransformationResultFromStage2To3<GroupBy3> groupByTr = groupBy.transform(currentContext);
            transformed.add(groupByTr.item);
            currentContext = groupByTr.updatedContext;
        }
        return new TransformationResultFromStage2To3<>(new GroupBys3(transformed), currentContext);
    }

    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final GroupBy2 group : groups) {
            result.addAll(group.operand().collectProps());
        }
        return result;
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return groups.isEmpty()
                ? emptySet()
                : groups.stream().map(el -> el.operand().collectEntityTypes()).flatMap(Set::stream).collect(toSet());
    }

    @Override
    public List<GroupBy2> groups() {
        return unmodifiableList(groups);
    }

}
