package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.elements.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;
import ua.com.fielden.platform.utils.Pair;

public class PropsResolutionContext {
    private final List<List<IQrySource2>> sources;
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;

    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = new ArrayList<>();
        sources.add(new ArrayList<>());
    }
    
    private PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, List<List<IQrySource2>> sources) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = sources;
    }

    public PropsResolutionContext produceBasedOn() {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs);
    }

    public PropsResolutionContext cloneWithAdded(final IQrySource2 transformedSource) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        srcs.get(0).add(transformedSource); // adding source to current query list of sources
        return new PropsResolutionContext(domainInfo, srcs);
    }

    public PropsResolutionContext cloneWithAdded(final Pair<EntProp2, PropResolution> propResolution) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs);
    }

    public PropsResolutionContext cloneWithAdded(final EntValue2 value) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs);
    }

    public PropsResolutionContext cloneNew() {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs);
    }

    
    public PropsResolutionContext produceNewOne() {
        return new PropsResolutionContext(domainInfo);
    }

    public List<List<IQrySource2>> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> getDomainInfo() {
        return Collections.unmodifiableMap(domainInfo);
    }
}