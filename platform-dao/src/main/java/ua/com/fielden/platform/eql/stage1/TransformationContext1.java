package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

/**
 * A helper construct to assist with Prop1 to Prop2 transformation (aka property resolution).
 * Its core idea is to maintain a stack of query sources that get accumulated during the query transformation process.
 * The stack of query source is searched for property attribution -- source where a property belongs to. In case of more than one matching source or no source, an exception is raised.
 *
 * @author TG Team
 */
public final class TransformationContext1 {
    public static boolean SHOW_INTERNALS = false;

    public final List<List<ISource2<? extends ISource3>>> sourcesForNestedQueries; // in reverse order -- the first list is for the deepest nested query
    public final QuerySourceInfoProvider querySourceInfoProvider;
    public final boolean isForCalcProp; // indicates that this context is used to transform calc-prop expression.

    public TransformationContext1(final QuerySourceInfoProvider querySourceInfoProvider, final boolean isForCalcProp) {
        this(querySourceInfoProvider, emptyList(), isForCalcProp);
    }

    private TransformationContext1(final QuerySourceInfoProvider querySourceInfoProvider, final List<List<ISource2<? extends ISource3>>> sourcesForNestedQueries, final boolean isForCalcProp) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.sourcesForNestedQueries = sourcesForNestedQueries;
        this.isForCalcProp = isForCalcProp;
        if (SHOW_INTERNALS) {
            System.out.println(toString());
        }
    }

    public TransformationContext1 cloneWithAdded(final ISource2<? extends ISource3> transformedSource) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.add(transformedSource);
        final List<List<ISource2<? extends ISource3>>> newSourcesForNestedQueries = new ArrayList<>();
        newSourcesForNestedQueries.add(unmodifiableList(current));
        newSourcesForNestedQueries.addAll(sourcesForNestedQueries); // all lists within added list are already unmodifiable
        return new TransformationContext1(querySourceInfoProvider, unmodifiableList(newSourcesForNestedQueries), isForCalcProp);
    }

    public TransformationContext1 cloneWithAdded(final List<ISource2<? extends ISource3>> leftNodeSources, final List<ISource2<? extends ISource3>> rightNodeSources) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.addAll(leftNodeSources);
        current.addAll(rightNodeSources);
        final List<List<ISource2<? extends ISource3>>> newSourcesForNestedQueries = new ArrayList<>();
        newSourcesForNestedQueries.add(unmodifiableList(current));
        newSourcesForNestedQueries.addAll(sourcesForNestedQueries); // all lists within added list are already unmodifiable
        return new TransformationContext1(querySourceInfoProvider, unmodifiableList(newSourcesForNestedQueries), isForCalcProp);
    }

    public List<ISource2<? extends ISource3>> getCurrentLevelSources() {
        return sourcesForNestedQueries.get(0);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();

        int level = sourcesForNestedQueries.size();

        sb.append("TransformationContext1 " + hashCode() + ":\n");
        for (final List<ISource2<? extends ISource3>> levelSources : sourcesForNestedQueries) {
            sb.append("  Level: " + level + (level == sourcesForNestedQueries.size() ? " (innermost)" : (level == 1 ? " (outermost)" : "")) + "\n");
            level = level - 1;
            for (final ISource2<? extends ISource3> src : levelSources) {
                sb.append("    " + src.sourceType().getSimpleName() + (src.alias() != null ? " (" + src.alias() + ")" : "") + " -- " + src.getClass().getSimpleName() +  "\n");
            }
        }

        return sb.toString();
    }
}