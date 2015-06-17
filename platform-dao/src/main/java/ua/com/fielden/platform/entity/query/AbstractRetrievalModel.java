package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public abstract class AbstractRetrievalModel<T extends AbstractEntity<?>> {

    private final fetch<T> originalFetch;
    private DomainMetadataAnalyser domainMetadataAnalyser;

    private final Map<String, fetch<? extends AbstractEntity<?>>> entityProps = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final Set<String> primProps = new HashSet<String>();
    private final Set<String> proxiedProps = new HashSet<String>();
    private final Map<String, Class<? extends AbstractEntity<?>>>  proxiedPropsWithoutId = new HashMap<String, Class<? extends AbstractEntity<?>>>();

    public AbstractRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this.originalFetch = originalFetch;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    public fetch<T> getOriginalFetch() {
        return originalFetch;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getEntityProps() {
        return entityProps;
    }

    public Set<String> getProxiedProps() {
        return proxiedProps;
    }
    
    public Map<String, Class<? extends AbstractEntity<?>>> getProxiedPropsWithoutId() {
        return proxiedPropsWithoutId;
    }

    public DomainMetadataAnalyser getDomainMetadataAnalyser() {
        return domainMetadataAnalyser;
    }

    public boolean containsProp(final String propName) {
        return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    public boolean containsProxy(final String propName) {
        return proxiedProps.contains(propName) || proxiedPropsWithoutId.containsKey(propName);
    }

    public Class<T> getEntityType() {
        return originalFetch.getEntityType();
    }

    public Set<String> getPrimProps() {
        return primProps;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels() {
        return entityProps;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Fetch model:\n------------------------------------------------\n");
        sb.append("\t original:\n" + originalFetch + "\n\n");
        sb.append(primProps);
        if (entityProps.size() > 0) {
            sb.append("\n------------------------------------------------");
            for (final Entry<String, fetch<? extends AbstractEntity<?>>> fetchEntry : entityProps.entrySet()) {
                sb.append("\n" + fetchEntry.getKey() + " <<< " + fetchEntry.getValue());
                sb.append("\n------------------------------------------------");
            }
        }

        return sb.toString();
    }

    protected PropertyMetadata getPropMetadata(final String propName) {
        final PropertyMetadata ppi = getDomainMetadataAnalyser().getPropPersistenceInfoExplicitly(getEntityType(), propName);
        if (ppi != null) {
            if (ppi.getJavaType() != null) {
                return ppi;
            } else {
                throw new IllegalStateException("Couldn't determine type of property " + propName + " of entity type " + getEntityType());
            }
        } else {
            throw new IllegalArgumentException("Trying fetch entity of type [" + getEntityType() + "] with non-existing property [" + propName + "]");
        }
    }

    protected void without(final String propName) {
        final Class propType = getPropMetadata(propName).getJavaType();

        if (AbstractEntity.class.isAssignableFrom(propType)) {
            final Object removalResult = getEntityProps().remove(propName);
            if (removalResult == null) {
                throw new IllegalStateException("Couldn't find property [" + propName + "] to be excluded from fetched entity properties of entity type " + getEntityType());
            }
        } else {
            final boolean removalResult = getPrimProps().remove(propName);
            if (!removalResult) {
                throw new IllegalStateException("Couldn't find property [" + propName + "] to be excluded from fetched primitive properties of entity type " + getEntityType());
            }
        }
    }
}