package ua.com.fielden.platform.eql.stage2;

import static java.util.Map.copyOf;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.types.tuples.T2;

public record TablesAndSourceTreeResult (
        Map<String, Table> tables,  
        Map<Integer, List<ImplicitNode>> implicitNodesMap, 
        Map<Integer, Map<String, Expression2>> calcPropsResolutions,
        Map<Integer, Map<String, T2<String, Integer>>> plainPropsResolutions) {
    
    public TablesAndSourceTreeResult {
        tables = copyOf(tables);
        implicitNodesMap = copyOf(implicitNodesMap);
        calcPropsResolutions = copyOf(calcPropsResolutions);
        plainPropsResolutions = copyOf(plainPropsResolutions);
    }
}