package ua.com.fielden.platform.eql.stage2;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

public class KeyPropertyExtractor {

    private KeyPropertyExtractor() {}

    public static List<Prop2> extract(final Prop2 original) {
        final AbstractQuerySourceItem<?> operandPropLastMember = original.lastPart();
        if (needsExtraction(operandPropLastMember)) {
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

    public static boolean needsExtraction(final AbstractQuerySourceItem<?> operandPropLastMember) {
        return KEY.equals(operandPropLastMember.name) && (operandPropLastMember.hasExpression()/*composite*/ || isEntityType(operandPropLastMember.javaType())/*1-2-1*/);
    }
}