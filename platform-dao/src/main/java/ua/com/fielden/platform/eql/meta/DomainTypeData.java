package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class DomainTypeData {
    public final Class<?> type;
    public final Class<?> superType;
    public final long id;
    public final String key;
    public final String desc;
    public final boolean isEntity;
    public final String dbTable;
    public final String entityTypeDesc;
    public final int propsCount;
    public final Map<String, Integer> keyMembersIndices = new HashMap<>();
    public final Map<String, PropertyMetadata> props = new LinkedHashMap<>();

    public DomainTypeData(final Class<?> type, final Class<?> superType, final long id, final String key, final String desc, final boolean isEntity, final String dbTable, final String entityTypeDesc, final int propsCount, final List<? extends PropertyMetadata> keyMembers, final List<? extends PropertyMetadata> props) {
        this.type = type;
        this.superType = superType;
        this.id = id;
        this.key = key;
        this.desc = desc;
        this.isEntity = isEntity;
        this.dbTable = dbTable;
        this.entityTypeDesc = entityTypeDesc;
        this.propsCount = propsCount;
        for (final PropertyMetadata prop : props) {
            this.props.put(prop.name(), prop);
        }

        int i = 0;
        for (final PropertyMetadata km : keyMembers) {
            i = i + 1;
            keyMembersIndices.put(km.name(), i);
        }

        if (keyMembersIndices.isEmpty()) {
            keyMembersIndices.put("key", 0);
        }
    }

    public Integer getKeyMemberIndex(final String keyMember) {
        return keyMembersIndices.get(keyMember);
    }

    public Map<String, PropertyMetadata> getProps() {
        return unmodifiableMap(props);
    }

}
