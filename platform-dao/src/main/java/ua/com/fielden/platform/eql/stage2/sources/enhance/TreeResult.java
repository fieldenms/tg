package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;

public record TreeResult(
        Map<Integer, List<ImplicitNode>> implicitNodesMap, 
        List<Prop3Links> propsData, 
        List<ExpressionLinks> expressionsData) {

    public TreeResult {
        implicitNodesMap = copyOf(implicitNodesMap);
        propsData = List.copyOf(propsData);
        expressionsData = List.copyOf(expressionsData);
    }
}