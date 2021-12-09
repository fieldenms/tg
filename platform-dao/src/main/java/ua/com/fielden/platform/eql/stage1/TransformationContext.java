package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class TransformationContext {
    private final List<List<ISource2<? extends ISource3>>> sources;
    public final EqlDomainMetadata domainInfo;
    public final String sourceIdPrefix; //used for ensuring query sources ids uniqueness within calc-prop expression queries
    public final boolean shouldIncludeCalcProps;

    public TransformationContext(final EqlDomainMetadata domainInfo, final List<List<ISource2<? extends ISource3>>> sources, final String sourceIdPrefix, final boolean shouldIncludeCalcProps) {
        this.domainInfo = domainInfo;
        this.sources = sources;
        this.sourceIdPrefix = sourceIdPrefix;
        this.shouldIncludeCalcProps = shouldIncludeCalcProps;
    }

    public TransformationContext(final EqlDomainMetadata domainInfo) {
        this(domainInfo, buildSourcesStackForNewQuery(emptyList()), null, false);
    }
    
    private static List<List<ISource2<? extends ISource3>>> buildSourcesStackForNewQuery(final List<List<ISource2<? extends ISource3>>> existingSources) {
        final List<List<ISource2<? extends ISource3>>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(existingSources);
        return srcs;
    }

    public TransformationContext produceForCorrelatedSubquery() {
        return new TransformationContext(domainInfo, buildSourcesStackForNewQuery(sources), sourceIdPrefix, false);
    }

    public TransformationContext produceForCorrelatedSourceQuery() {
        return new TransformationContext(domainInfo, buildSourcesStackForNewQuery(sources.subList(1, sources.size())), sourceIdPrefix, false);
    }
    
    public TransformationContext produceForUncorrelatedSourceQuery() {
        return new TransformationContext(domainInfo, buildSourcesStackForNewQuery(emptyList()), sourceIdPrefix, false);
    }
    
    public TransformationContext cloneWithAdded(final ISource2<? extends ISource3> transformedSource) {
        final List<List<ISource2<? extends ISource3>>> newSources = sources.stream().map(el -> new ArrayList<>(el)).collect(toList()); // making deep copy of old list of sources
        newSources.get(0).add(transformedSource); // adding source to current query list of sources
        return new TransformationContext(domainInfo, newSources, sourceIdPrefix, false);
    }
    
    public TransformationContext cloneForAggregates() {
        final List<List<ISource2<? extends ISource3>>> newSources = sources.stream().map(el -> new ArrayList<>(el)).collect(toList()); // making deep copy of old list of sources
        return new TransformationContext(domainInfo, newSources, sourceIdPrefix, true);
    }

    public List<List<ISource2<? extends ISource3>>> getSources() {
        return unmodifiableList(sources);
    }
}