package ua.com.fielden.platform.eql.stage2;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

public class KeyPropertyExtractor {

    private KeyPropertyExtractor() {}

    public static List<Prop2> extract(final Prop2 original) {
        if (needsExtraction(original.lastPart(), original.penultPart())) {
            final int pathSize = original.getPath().size();
            final QuerySourceInfo<?> ei = pathSize == 1 ? original.source.querySourceInfo() : ((QuerySourceItemForEntityType<?>) original.getPath().get(pathSize - 2)).querySourceInfo;
            final List<String> keyTreeLeaves = keyPaths(ei.javaType());
            final List<Prop2> result = new ArrayList<>();
            for (final String keyTreeLeaf : keyTreeLeaves) {
                final PropResolutionProgress resolution = ei.resolve(new PropResolutionProgress(keyTreeLeaf));
                final List<AbstractQuerySourceItem<?>> enhancedPath = new ArrayList<>(original.getPath().subList(0, pathSize - 1));
                enhancedPath.addAll(resolution.getResolved());
                result.add(new Prop2(original.source, enhancedPath));
            }
            return result;
        }

        return asList(original);
    }

    public static boolean needsExtraction(final AbstractQuerySourceItem<?> operandPropLastMember, final Optional<AbstractQuerySourceItem<?>> maybeOperandPropPenultMember) {
        // TODO for now we simply skip the extraction for key members of a union entity
        //      but this has the limitation of representing the key members of union properties, in case they are composite and have more than 1 member, as a concatenated string and thus, for example, ordering would happen by the string representation
        //      rather than structural ordering by each key member individually
        //      this needs to be addressed
        final var caseOfUnionEntity = maybeOperandPropPenultMember.map(part -> part instanceof QuerySourceItemForUnionType).orElse(false);
        return !caseOfUnionEntity && KEY.equals(operandPropLastMember.name) && (operandPropLastMember.hasExpression()/*composite*/ || isEntityType(operandPropLastMember.javaType())/*1-2-1*/);
    }
}