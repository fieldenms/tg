package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.BranchNode;
import ua.com.fielden.platform.eql.stage2.sources.LeafNode;

public record TreeResult(Map<Integer, List<LeafNode>> leavesMap, Map<Integer, List<BranchNode>> branchesMap, Map<Integer, Map<String, Expression2>> expressionsData) {

    public TreeResult {
        leavesMap = copyOf(leavesMap);
        branchesMap = copyOf(branchesMap);
        expressionsData = copyOf(expressionsData);
    }
}