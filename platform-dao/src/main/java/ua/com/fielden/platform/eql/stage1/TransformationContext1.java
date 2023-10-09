package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public final class TransformationContext1 {
    public final List<List<ISource2<? extends ISource3>>> sourcesForNestedQueries; // in reverse order -- the first list is for the deepest nested query
    public final QuerySourceInfoProvider querySourceInfoProvider;
    public final boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery;
    public final boolean isForCalcProp; // indicates that this context is used to transform calc-prop expression.

    public TransformationContext1(final QuerySourceInfoProvider querySourceInfoProvider, final boolean isForCalcProp) {
        this(querySourceInfoProvider, emptyList(), false, isForCalcProp);
    }

    private TransformationContext1(final QuerySourceInfoProvider querySourceInfoProvider, final List<List<ISource2<? extends ISource3>>> sourcesForNestedQueries, final boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery, final boolean isForCalcProp) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.sourcesForNestedQueries = sourcesForNestedQueries;
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = shouldMaterialiseCalcPropsAsColumnsInSqlQuery;
        this.isForCalcProp = isForCalcProp;
    }

    public TransformationContext1 cloneWithAdded(final ISource2<? extends ISource3> transformedSource) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.add(transformedSource);
        final List<List<ISource2<? extends ISource3>>> newSourcesForNestedQueries = new ArrayList<>();
        newSourcesForNestedQueries.add(unmodifiableList(current));
        newSourcesForNestedQueries.addAll(sourcesForNestedQueries); // all lists within added list are already unmodifiable
        return new TransformationContext1(querySourceInfoProvider, unmodifiableList(newSourcesForNestedQueries), false, isForCalcProp);
    }

    public TransformationContext1 cloneWithAdded(final List<ISource2<? extends ISource3>> leftNodeSources, final List<ISource2<? extends ISource3>> rightNodeSources) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.addAll(leftNodeSources);
        current.addAll(rightNodeSources);
        final List<List<ISource2<? extends ISource3>>> newSourcesForNestedQueries = new ArrayList<>();
        newSourcesForNestedQueries.add(unmodifiableList(current));
        newSourcesForNestedQueries.addAll(sourcesForNestedQueries); // all lists within added list are already unmodifiable
        return new TransformationContext1(querySourceInfoProvider, unmodifiableList(newSourcesForNestedQueries), false, isForCalcProp);
    }

    /**
     * Is used to handle entity types with calculated totals. It is mainly needed to handle SQL Server limitation of aggregation on subqueries.
     * 
     * @return
     */
    public TransformationContext1 cloneForAggregates() {
        return new TransformationContext1(querySourceInfoProvider, sourcesForNestedQueries, true, isForCalcProp);
    }
    
    public List<ISource2<? extends ISource3>> getCurrentLevelSources() {
        return sourcesForNestedQueries.get(0);
    }
}