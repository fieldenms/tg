package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.*;
import java.util.Map.Entry;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;

public abstract class AbstractRetrievalModel<T extends AbstractEntity<?>> implements IRetrievalModel<T> {

    protected final fetch<T> originalFetch;
    protected final IDomainMetadata domainMetadata;
    public final boolean topLevel;

    private final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps = new HashMap<>();
    private final Set<String> primProps = new HashSet<String>();
    private final Set<String> proxiedProps = new HashSet<String>();

    protected AbstractRetrievalModel(final fetch<T> originalFetch, final IDomainMetadata domainMetadata, final boolean topLevel) {
        this.originalFetch = originalFetch;
        this.topLevel = topLevel;
        this.domainMetadata = domainMetadata;
    }

    public fetch<T> getOriginalFetch() {
        return originalFetch;
    }

    @Override
    public Set<String> getProxiedProps() {
        return proxiedProps;
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
    public boolean topLevel() {
        return topLevel;
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

    protected Optional<PropertyMetadata> getPropMetadata(final String propName) {
        final var optPm = domainMetadata.forPropertyOpt(getEntityType(), propName);
        if (optPm.isEmpty()) {
            // allow only IDs and VERSIONs to have missing PropertyMetadata; this is sometimes useful for pure synthetic entities that yield these props
            if (ID.equals(propName) || VERSION.equals(propName)) {
                return Optional.empty();
            }
            throw new EqlException(format("Trying to fetch entity of type [%s] with non-existing property [%s]", getEntityType(), propName));
        }
        return optPm;
    }

    protected void without(final String propName) {
        final Optional<PropertyMetadata> optPm = getPropMetadata(propName);

        if (optPm.map(pm -> pm.type().isEntity()).orElse(FALSE)) {
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
