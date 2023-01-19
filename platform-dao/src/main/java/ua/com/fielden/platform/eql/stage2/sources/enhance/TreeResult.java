package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.sources.BranchNode;

public record TreeResult(Map<Integer, List<BranchNode>> branchesMap, List<Prop3Links> propsData, List<ExpressionLinks> expressionsData) {

    public TreeResult {
        branchesMap = copyOf(branchesMap);
        propsData = List.copyOf(propsData);
        expressionsData = List.copyOf(expressionsData);
    }
}