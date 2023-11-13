package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.HelperNodeForImplicitJoins;

public record TreeResultBySources (
        Map<Integer, List<HelperNodeForImplicitJoins>> helperNodesMap,
        Map<Integer, Map<String, Expression2>> calcPropsResolutions,
        Map<Integer, Map<String, DataForProp3>> plainPropsResolutions) {

    public TreeResultBySources {
        helperNodesMap = copyOf(helperNodesMap);
        calcPropsResolutions = copyOf(calcPropsResolutions);
        plainPropsResolutions = copyOf(plainPropsResolutions);
    }
}