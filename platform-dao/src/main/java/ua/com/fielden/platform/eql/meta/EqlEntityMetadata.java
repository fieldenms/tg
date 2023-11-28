package ua.com.fielden.platform.eql.meta;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;

public class EqlEntityMetadata implements Comparable<EqlEntityMetadata> {
    public final EntityTypeInfo<? extends AbstractEntity<?>> typeInfo;
    private final Map<String, EqlPropertyMetadata> props = new LinkedHashMap<>();

    public EqlEntityMetadata(EntityTypeInfo<? extends AbstractEntity<?>> typeInfo, final List<EqlPropertyMetadata> props) {
        this.typeInfo = typeInfo;
        for (final EqlPropertyMetadata eqlPropertyMetadata : props) {
            this.props.put(eqlPropertyMetadata.name, eqlPropertyMetadata);
        }
    }

    public Set<EqlPropertyMetadata> props() {
        return Collections.unmodifiableSet(new TreeSet<>(props.values()));
    }

    public EqlPropertyMetadata findProp(final String propName) {
        return props.get(propName);
    }

    @Override
    public int compareTo(final EqlEntityMetadata that) {
        return this.typeInfo.entityType.getSimpleName().compareTo(that.typeInfo.entityType.getSimpleName());
    }
}