package ua.com.fielden.platform.eql.stage1.elements;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class PropsResolutionContext {
    private final List<List<IQrySource2<? extends IQrySource3>>> sources;
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    public final String sourceId;

    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final List<List<IQrySource2<? extends IQrySource3>>> sources, final String sourceId) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = sources;
        this.sourceId = sourceId;
    }

    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this(domainInfo, buildSourcesStackForNewQuery(emptyList()), null);
    }
    
    private static List<List<IQrySource2<? extends IQrySource3>>> buildSourcesStackForNewQuery(final List<List<IQrySource2<? extends IQrySource3>>> existingSources) {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(existingSources);
        return srcs;
    }

    public PropsResolutionContext produceForCorrelatedSubquery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(sources), sourceId);
    }

    public PropsResolutionContext produceForCorrelatedSourceQuery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(sources.subList(1, sources.size())), sourceId);
    }
    
    public PropsResolutionContext produceForUncorrelatedSourceQuery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(emptyList()), sourceId);
    }
    
    public PropsResolutionContext cloneWithAdded(final IQrySource2<? extends IQrySource3> transformedSource) {
        final List<List<IQrySource2<? extends IQrySource3>>> newSources = sources.stream().map(el -> new ArrayList<>(el)).collect(toList()); // making deep copy of old list of sources
        newSources.get(0).add(transformedSource); // adding source to current query list of sources
        return new PropsResolutionContext(domainInfo, newSources, sourceId);
    }

    public List<List<IQrySource2<? extends IQrySource3>>> getSources() {
        return unmodifiableList(sources);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> getDomainInfo() {
        return unmodifiableMap(domainInfo);
    }
}