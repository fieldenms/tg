package ua.com.fielden.platform.eql.stage2;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;

public class KeyPropertyExtractor {
    
    public static List<EntProp2> extract(final EntProp2 original) {
        final AbstractPropInfo<?> operandPropLastMember = original.lastPart();
        if (needsExtraction(operandPropLastMember)) {
            final int pathSize = original.getPath().size();
            final EntityInfo<?> ei = original.getPath().size() == 1 ? original.source.entityInfo() : ((EntityTypePropInfo<?>) original.getPath().get(pathSize - 2)).propEntityInfo;
            final List<String> keyTreeLeaves = keyPaths(ei.javaType());
            final List<EntProp2> result = new ArrayList<>();
            for (final String keyTreeLeaf : keyTreeLeaves) {
                final PropResolutionProgress resolution = ei.resolve(new PropResolutionProgress(keyTreeLeaf));
                final List<AbstractPropInfo<?>> enhancedPath = new ArrayList<>(original.getPath().subList(0, pathSize - 1));
                enhancedPath.addAll(resolution.getResolved());
                result.add(new EntProp2(original.source, enhancedPath));
            }
            return result;
        }
        
        return Arrays.asList(original);
    }
    
    public static boolean needsExtraction(final AbstractPropInfo<?> operandPropLastMember) {
        return KEY.equals(operandPropLastMember.name) && (operandPropLastMember.hasExpression()/*composite*/ ||  isEntityType(operandPropLastMember.javaType())/*1-2-1*/);
    }
}