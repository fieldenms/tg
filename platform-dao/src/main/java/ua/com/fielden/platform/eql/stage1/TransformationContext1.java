package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public final class TransformationContext1 {
    public final List<List<ISource2<? extends ISource3>>> sources;
    public final EqlDomainMetadata domainInfo;
    public final boolean shouldIncludeCalcProps;

    public TransformationContext1(final EqlDomainMetadata domainInfo) {
        this(domainInfo, emptyList(), false);
    }

    private TransformationContext1(final EqlDomainMetadata domainInfo, final List<List<ISource2<? extends ISource3>>> sources, final boolean shouldIncludeCalcProps) {
        this.domainInfo = domainInfo;
        this.sources = sources;
        this.shouldIncludeCalcProps = shouldIncludeCalcProps;
    }

    public TransformationContext1 cloneWithAdded(final ISource2<? extends ISource3> transformedSource) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.add(transformedSource);
        final List<List<ISource2<? extends ISource3>>> newSources = new ArrayList<>();
        newSources.add(unmodifiableList(current));
        newSources.addAll(sources); // all lists within added list are already unmodifiable
        return new TransformationContext1(domainInfo, unmodifiableList(newSources), false);
    }

    public TransformationContext1 cloneWithAdded(final List<ISource2<? extends ISource3>> leftNodeSources, final List<ISource2<? extends ISource3>> rightNodeSources) {
        final List<ISource2<? extends ISource3>> current = new ArrayList<>();
        current.addAll(leftNodeSources);
        current.addAll(rightNodeSources);
        final List<List<ISource2<? extends ISource3>>> newSources = new ArrayList<>();
        newSources.add(unmodifiableList(current));
        newSources.addAll(sources); // all lists within added list are already unmodifiable
        return new TransformationContext1(domainInfo, unmodifiableList(newSources), false);
    }

    public TransformationContext1 cloneForAggregates() {
        return new TransformationContext1(domainInfo, sources, true);
    }
}