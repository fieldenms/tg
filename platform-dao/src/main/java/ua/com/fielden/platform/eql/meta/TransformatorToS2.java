package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.s1.elements.EntProp;
import ua.com.fielden.platform.eql.s1.elements.ISource;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource;
import ua.com.fielden.platform.eql.s2.elements.ISource2;
import ua.com.fielden.platform.utils.Pair;

public class TransformatorToS2 {
    private List<Map<ISource<? extends ISource2>, Pair<ISource2, EntityInfo>>> sourceMap;
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata;

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata) {
	this.metadata = metadata;
	sourceMap.add(new HashMap<ISource<? extends ISource2>, Pair<ISource2, EntityInfo>>());
    }

    public void addSource(final ISource<? extends ISource2> source) {
	getCurrentQueryMap().put(source, new Pair<ISource2, EntityInfo>(transformSource(source), metadata.get(source.sourceType())));
    }

    public TransformatorToS2 produceBasedOn() {
	final TransformatorToS2 result = new TransformatorToS2(metadata);
	result.sourceMap.addAll(sourceMap);
	return result;
    }

    private ISource2 transformSource(final ISource<? extends ISource2> originalSource) {
	if (originalSource instanceof TypeBasedSource) {
	    return new ua.com.fielden.platform.eql.s2.elements.TypeBasedSource(((TypeBasedSource) originalSource).getEntityMetadata(), originalSource.getAlias(), ((TypeBasedSource) originalSource).getDomainMetadataAnalyser());
	}
	throw new IllegalStateException("Transformation of query based source not yet implemented!");
    }

    private Map<ISource<? extends ISource2>, Pair<ISource2, EntityInfo>> getCurrentQueryMap() {
	return sourceMap.get(0);
    }

    public ISource2 getTransformedSource(final ISource<? extends ISource2> originalSource) {
	return getCurrentQueryMap().get(originalSource).getKey();
    }

    public ua.com.fielden.platform.eql.s2.elements.EntProp getTransformedProp(final EntProp originalProp) {
	final Iterator<Map<ISource<? extends ISource2>, Pair<ISource2, EntityInfo>>> it = sourceMap.iterator();
	if (originalProp.isExternal()) {
	    it.next();
	}

	for (; it.hasNext();) {
	    final Map<ISource<? extends ISource2>, Pair<ISource2, EntityInfo>> item = it.next();
	    final PropResolution resolution = resolveProp(item.values(), originalProp);
	    if (resolution != null) {
		return generateTransformedProp(resolution);
	    }
	}

	throw new IllegalStateException("Can't resolve property [" + originalProp.getName() + "].");
    }

    private ua.com.fielden.platform.eql.s2.elements.EntProp generateTransformedProp(final PropResolution resolution) {
	return new ua.com.fielden.platform.eql.s2.elements.EntProp(resolution.entProp.getName(), resolution.source, resolution.aliased, resolution.resolution);
    }

    public static class PropResolution {
	private final boolean aliased;

	public PropResolution(final boolean aliased, final ISource2 source, final Object resolution, final EntProp entProp) {
	    super();
	    this.aliased = aliased;
	    this.source = source;
	    this.resolution = resolution;
	    this.entProp = entProp;
	}

	private final ISource2 source;
	private final Object resolution;
	private final EntProp entProp;
    }

    private PropResolution resolvePropAgainstSource(final Pair<ISource2, EntityInfo> source, final EntProp entProp) {
	final Object asIsResolution = source.getValue().resolve(entProp.getName());
	if (source.getKey().getAlias() != null && entProp.getName().startsWith(source.getKey().getAlias() + ".")) {
	    final String aliasLessPropName = entProp.getName().substring(source.getKey().getAlias().length() + 1);
	    final Object aliasLessResolution = source.getValue().resolve(aliasLessPropName);
	    if (aliasLessResolution != null) {
		if (asIsResolution == null) {
		    return new PropResolution(true, source.getKey(), aliasLessResolution, entProp);
		} else {
		    throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]. Both [" + entProp.getName() + "] and [" + aliasLessPropName
			    + "] are resolvable against given source.");
		}
	    }
	}
	return asIsResolution != null ? new PropResolution(false, source.getKey(), asIsResolution, entProp) : null;
    }

    private PropResolution resolveProp(final Collection<Pair<ISource2, EntityInfo>> sources, final EntProp entProp) {
	final List<PropResolution> result = new ArrayList<>();
	for (final Pair<ISource2, EntityInfo> pair : sources) {
	    final PropResolution resolution = resolvePropAgainstSource(pair, entProp);
	    if (resolution != null) {
		result.add(resolution);
	    }
	}

	if (result.size() > 1) {
	    throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]");
	}

	return result.size() == 1 ? result.get(0) : null;
    }

    public String generateNextSqlParamName() {
	return null;
    }
}