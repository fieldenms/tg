package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class TransformationContext1 {
    private final List<List<ISource2<? extends ISource3>>> sources;
    public final EqlDomainMetadata domainInfo;
    public final boolean shouldIncludeCalcProps;

    public TransformationContext1(final EqlDomainMetadata domainInfo, final List<List<ISource2<? extends ISource3>>> sources, final boolean shouldIncludeCalcProps) {
        this.domainInfo = domainInfo;
        this.sources = sources;
        this.shouldIncludeCalcProps = shouldIncludeCalcProps;
    }

    public TransformationContext1(final EqlDomainMetadata domainInfo) {
        this(domainInfo, buildSourcesStackForNewQuery(emptyList()), false);
    }
    
    private static List<List<ISource2<? extends ISource3>>> buildSourcesStackForNewQuery(final List<List<ISource2<? extends ISource3>>> existingSources) {
        final List<List<ISource2<? extends ISource3>>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(existingSources);
        return srcs;
    }

    public TransformationContext1 produceForCorrelatedSubquery() {
        return new TransformationContext1(domainInfo, buildSourcesStackForNewQuery(sources), false);
    }

    public TransformationContext1 produceForCorrelatedSourceQuery() {
        return new TransformationContext1(domainInfo, buildSourcesStackForNewQuery(sources.subList(1, sources.size())), false);
    }
    
    public TransformationContext1 produceForUncorrelatedSourceQuery() {
        return new TransformationContext1(domainInfo, buildSourcesStackForNewQuery(emptyList()), false);
    }
    
    public TransformationContext1 cloneWithAdded(final ISource2<? extends ISource3> transformedSource) {
        final List<List<ISource2<? extends ISource3>>> newSources = sources.stream().map(el -> new ArrayList<>(el)).collect(toList()); // making deep copy of old list of sources
        newSources.get(0).add(transformedSource); // adding source to current query list of sources
        return new TransformationContext1(domainInfo, newSources, false);
    }
    
    public TransformationContext1 cloneForAggregates() {
        final List<List<ISource2<? extends ISource3>>> newSources = sources.stream().map(el -> new ArrayList<>(el)).collect(toList()); // making deep copy of old list of sources
        return new TransformationContext1(domainInfo, newSources, true);
    }

    public List<List<ISource2<? extends ISource3>>> getSources() {
        return unmodifiableList(sources);
    }
}