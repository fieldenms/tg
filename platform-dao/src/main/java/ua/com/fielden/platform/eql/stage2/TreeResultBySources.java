package ua.com.fielden.platform.eql.stage2;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;
import ua.com.fielden.platform.eql.stage2.sources.enhance.Prop3Lite;

public record TreeResultBySources (
        Map<Integer, List<ImplicitNode>> implicitNodesMap, 
        Map<Integer, Map<String, Expression2>> calcPropsResolutions,
        Map<Integer, Map<String, Prop3Lite>> plainPropsResolutions) {
    
    public TreeResultBySources {
        implicitNodesMap = copyOf(implicitNodesMap);
        calcPropsResolutions = copyOf(calcPropsResolutions);
        plainPropsResolutions = copyOf(plainPropsResolutions);
    }
}