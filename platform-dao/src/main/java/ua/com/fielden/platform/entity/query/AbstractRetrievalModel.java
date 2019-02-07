package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;

public abstract class AbstractRetrievalModel<T extends AbstractEntity<?>> implements IRetrievalModel<T> {

    protected final fetch<T> originalFetch;
    private DomainMetadataAnalyser domainMetadataAnalyser;

    private final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps = new HashMap<>();
    private final Set<String> primProps = new HashSet<String>();
    private final Set<String> proxiedProps = new HashSet<String>();

    public AbstractRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this.originalFetch = originalFetch;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    public fetch<T> getOriginalFetch() {
        return originalFetch;
    }

    @Override
    public Set<String> getProxiedProps() {
        return proxiedProps;
    }
    
    public DomainMetadataAnalyser getDomainMetadataAnalyser() {
        return domainMetadataAnalyser;
    }

    @Override
    public boolean containsProp(final String propName) {
        return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    @Override
    public boolean containsProxy(final String propName) {
        return proxiedProps.contains(propName);
    }

    @Override
    public Class<T> getEntityType() {
        return originalFetch.getEntityType();
    }

    @Override
    public boolean isInstrumented() {
        return originalFetch.isInstrumented();
    }

    @Override
    public Set<String> getPrimProps() {
        return unmodifiableSet(primProps);
    }

    @Override
    public Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels() {
        return unmodifiableMap(entityProps);
    }
    
    protected void addPrimProp(final String name) {
        primProps.add(name);
    }

    protected void addEntityPropFetchModel(final String propName, final EntityRetrievalModel<? extends AbstractEntity<?>> fetchModel) {
        entityProps.put(propName, fetchModel);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Fetch model:\n------------------------------------------------\n");
        sb.append("\t original:\n" + originalFetch + "\n\n");
        sb.append(primProps);
        if (entityProps.size() > 0) {
            sb.append("\n------------------------------------------------");
            for (final Entry<String, EntityRetrievalModel<? extends AbstractEntity<?>>> fetchEntry : entityProps.entrySet()) {
                sb.append("\n" + fetchEntry.getKey() + " <<< " + fetchEntry.getValue());
                sb.append("\n------------------------------------------------");
            }
        }

        return sb.toString();
    }

    protected PropertyMetadata getPropMetadata(final String propName) {
        final PropertyMetadata ppi = domainMetadataAnalyser.getPropPersistenceInfoExplicitly(getEntityType(), propName);
        if (ppi != null) {
            if (ppi.getJavaType() != null) {
                return ppi;
            } else {
                throw new EqlException(format("Couldn't determine type of property [%s] of entity type [%s]", propName, getEntityType()));
            }
        } else {
            throw new EqlException(format("Trying to fetch entity of type [%s] with non-existing property [%s]", getEntityType(), propName));
        }
    }

    protected void without(final String propName) {
        final Class<?> propType = getPropMetadata(propName).getJavaType();

        if (isEntityType(propType)) {
            final Object removalResult = entityProps.remove(propName);
            if (removalResult == null) {
                throw new EqlException(format("Couldn't find property [%s] to be excluded from fetched entity properties of entity type [%s]", propName, getEntityType()));
            }
        } else {
            final boolean removalResult = primProps.remove(propName);
            if (!removalResult) {
                throw new EqlException(format("Couldn't find property [%s] to be excluded from fetched primitive properties of entity type [%s]", propName, getEntityType()));
            }
        }
    }
}