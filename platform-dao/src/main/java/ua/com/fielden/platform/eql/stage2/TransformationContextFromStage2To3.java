package ua.com.fielden.platform.eql.stage2;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EqlEntityMetadataHolder;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.sources.HelperNodeForImplicitJoins;
import ua.com.fielden.platform.eql.stage2.sources.enhance.DataForProp3;
import ua.com.fielden.platform.eql.stage2.sources.enhance.TreeResultBySources;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContextFromStage2To3 {

    private final TreeResultBySources treeResultBySources;
    private final EqlEntityMetadataHolder eqlEntityMetadataHolder;
    private final Map<Integer, ISource3> sourcesByIds = new HashMap<>();
    private final Map<String, Object> sqlParamValuesByNames = new HashMap<>();
    private final Map<Object, String> sqlParamNamesByValues = new HashMap<>();
    public final int sqlId;
    private final int paramId; //incremented after each new param name generation

    public TransformationContextFromStage2To3(final TreeResultBySources treeResultBySources, final EqlEntityMetadataHolder eqlEntityMetadataHolder) {
        this(treeResultBySources, eqlEntityMetadataHolder, emptyMap(), emptyMap(), emptyMap(), 0, 1);
    }

    private TransformationContextFromStage2To3(final TreeResultBySources treeResultBySources,
            final EqlEntityMetadataHolder eqlEntityMetadataHolder,
            final Map<Integer, ISource3> sourcesByIds,
            final Map<String, Object> sqlParamValuesByNames,
            final Map<Object, String> sqlParamNamesByValues,
            final int sqlId, final int paramId) {
        this.treeResultBySources = treeResultBySources;
        this.eqlEntityMetadataHolder = eqlEntityMetadataHolder;
        this.sourcesByIds.putAll(sourcesByIds);
        this.sqlParamValuesByNames.putAll(sqlParamValuesByNames);
        this.sqlParamNamesByValues.putAll(sqlParamNamesByValues);
        this.sqlId = sqlId;
        this.paramId = paramId;
    }

    public EqlTable getTable(final Class<? extends AbstractEntity<?>> sourceType) {
        return eqlEntityMetadataHolder.getTableForEntityType(sourceType);
    }

    public Map<String, Object> getSqlParamValues() {
        return unmodifiableMap(sqlParamValuesByNames);
    }
    
    public T2<String, TransformationContextFromStage2To3> obtainParamNameAndUpdateContext(final Object paramValue) {
        final String existingParamName = sqlParamNamesByValues.get(paramValue);
        if (existingParamName != null) {
            return t2(existingParamName, this);
        } else {
            final String paramName = "P_" + paramId;
            final TransformationContextFromStage2To3 result = new TransformationContextFromStage2To3(treeResultBySources, eqlEntityMetadataHolder, sourcesByIds, sqlParamValuesByNames, sqlParamNamesByValues, sqlId, paramId + 1);
            result.sqlParamValuesByNames.put(paramName, paramValue);
            result.sqlParamNamesByValues.put(paramValue, paramName);

            return t2(paramName, result);
        }
    }

    public List<HelperNodeForImplicitJoins> getHelperNodesForSource(final Integer sourceId) {
        final List<HelperNodeForImplicitJoins> result = treeResultBySources.helperNodesMap().get(sourceId);
        // result may be null due to count(*) or yield const only queries
        return result != null ? result : emptyList();
    }

    public TransformationContextFromStage2To3 cloneWithNextSqlId() {
        return new TransformationContextFromStage2To3(treeResultBySources, eqlEntityMetadataHolder, sourcesByIds, sqlParamValuesByNames, sqlParamNamesByValues, sqlId + 1, paramId);
    }

    public TransformationContextFromStage2To3 cloneWithSource(final ISource3 source) {
        final TransformationContextFromStage2To3 result = new TransformationContextFromStage2To3(treeResultBySources, eqlEntityMetadataHolder, sourcesByIds, sqlParamValuesByNames, sqlParamNamesByValues, sqlId, paramId);
        result.sourcesByIds.put(source.id(), source);
        return result;
    }

    public T2<String, ISource3> resolve(final Integer sourceId, final String path) {
        final DataForProp3 leafProp = treeResultBySources.plainPropsResolutions().get(sourceId).get(path);
        return t2(leafProp.name(), sourcesByIds.get(leafProp.sourceId()));
    }
    
    public Expression2 resolveExpression(final Integer sourceId, final String path) {
        return treeResultBySources.calcPropsResolutions().get(sourceId).get(path);
    }
}