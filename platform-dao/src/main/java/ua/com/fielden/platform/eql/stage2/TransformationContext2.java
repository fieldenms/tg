package ua.com.fielden.platform.eql.stage2;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.getOriginalEntityTypeFullName;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.BranchNode;
import ua.com.fielden.platform.eql.stage2.sources.LeafNode;
import ua.com.fielden.platform.eql.stage2.sources.enhance.Prop2Link;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext2 {

    private final TablesAndSourceTreeResult tablesAndSourceChildren;
    private final Map<Integer, Map<String, T2<String, ISource3>>> resolutions = new HashMap<>();  
    private final Map<String, Object> paramValuesByNames = new HashMap<>();
    private final Map<Object, String> paramNamesByValues = new HashMap<>();
    public final int sqlId;
    private final int paramId; //incremented after each new param name generation

    public TransformationContext2(final TablesAndSourceTreeResult tablesAndSourceChildren) {
        this(tablesAndSourceChildren, emptyMap(), emptyMap(), emptyMap(), 0, 1);
    }

    private TransformationContext2(final TablesAndSourceTreeResult tablesAndSourceChildren, 
            final Map<Integer, Map<String, T2<String, ISource3>>> resolutions,
            final Map<String, Object> paramValuesByNames,
            final Map<Object, String> paramNamesByValues,
            final int sqlId, final int paramId) {
        this.tablesAndSourceChildren = tablesAndSourceChildren;
        this.resolutions.putAll(resolutions);
        this.paramValuesByNames.putAll(paramValuesByNames);
        this.paramNamesByValues.putAll(paramNamesByValues);
        this.sqlId = sqlId;
        this.paramId = paramId;
    }

    public Table getTable(final String sourceFullClassName) {
        return tablesAndSourceChildren.tables().get(getOriginalEntityTypeFullName(sourceFullClassName));
    }

    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValuesByNames);
    }
    
    public T2<String, TransformationContext2> obtainParamNameAndUpdateContext(final Object paramValue) {
        final String existingParamName = paramNamesByValues.get(paramValue);
        if (existingParamName != null) {
            return t2(existingParamName, this);
        } else {
            final String paramName = "P_" + paramId;
            final TransformationContext2 result = new TransformationContext2(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId, paramId + 1);
            result.paramValuesByNames.put(paramName, paramValue);
            result.paramNamesByValues.put(paramValue, paramName);

            return t2(paramName, result);
        }
    }

    public List<LeafNode> getSourceLeaves(final Integer sourceId) {
        final List<LeafNode> result = tablesAndSourceChildren.treeResult().leavesMap().get(sourceId);
        // result may be null due to count(*) or yield const only queries
        return result != null ? result : emptyList();
    }

    public List<BranchNode> getSourceBranches(final Integer sourceId) {
        final List<BranchNode> result = tablesAndSourceChildren.treeResult().branchesMap().get(sourceId);
        // result may be null due to count(*) or yield const only queries
        return result != null ? result : emptyList();
    }

    public TransformationContext2 cloneWithNextSqlId() {
        return new TransformationContext2(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId + 1, paramId);
    }

    public TransformationContext2 cloneWithResolutions(final ISource3 source, final List<LeafNode> children) {
        final TransformationContext2 result = new TransformationContext2(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId, paramId);
        
        for (final LeafNode child : children) {
            for (final Prop2Link propLink : child.paths()) {
                final Map<String, T2<String, ISource3>> existing = result.resolutions.get(propLink.sourceId());
                if (existing != null) {
                    existing.put(propLink.name(), t2(child.name(), source));
                } else {
                    final Map<String, T2<String, ISource3>> created = new HashMap<>();
                    created.put(propLink.name(), t2(child.name(), source));
                    result.resolutions.put(propLink.sourceId(), created);
                }
            }
        }
        return result;
    }

    public T2<String, ISource3> resolve(final Integer sourceId, final String path) {
        return resolutions.get(sourceId).get(path);
    }
    
    public Expression2 resolveExpression(final Integer sourceId, final String path) {
        return tablesAndSourceChildren.treeResult().expressionsData().get(sourceId).get(path);
    }
}