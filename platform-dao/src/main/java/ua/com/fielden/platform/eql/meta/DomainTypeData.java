package ua.com.fielden.platform.eql.meta;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.domain.metadata.DomainType;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.utils.StreamUtils.collectToImmutableMap;
import static ua.com.fielden.platform.utils.StreamUtils.integers;

/// An intermediate representation of [DomainType] used during generation.
///
/// @see DomainMetadataModelGenerator
///
record DomainTypeData(
        Class<?> type,
        Class<?> superType,
        long id,
        String key,
        String desc,
        boolean isEntity,
        @Nullable String dbTable,
        String entityTypeDesc,
        int propsCount,
        Map<String, Integer> keyMembersIndices,
        Map<String, PropertyMetadata> props)
{

    public static DomainTypeData domainTypeData(final Class<?> type, final Class<?> superType, final long id,
                                                final String key, final String desc, final boolean isEntity,
                                                final String dbTable, final String entityTypeDesc, final int propsCount,
                                                final List<? extends PropertyMetadata> keyMembers,
                                                final List<? extends PropertyMetadata> props) {
        final var propsMap = new LinkedHashMap<String, PropertyMetadata>();
        for (final PropertyMetadata prop : props) {
            propsMap.put(prop.name(), prop);
        }

        final Map<String, Integer> keyMembersIndicesMap = keyMembers.isEmpty()
                ? ImmutableMap.of("key", 0)
                : collectToImmutableMap(keyMembers.stream().map(PropertyMetadata::name), integers(1));

        return new DomainTypeData(type, superType, id, key, desc, isEntity, dbTable, entityTypeDesc, propsCount,
                                  keyMembersIndicesMap, unmodifiableMap(propsMap));
    }

    public Integer getKeyMemberIndex(final String keyMember) {
        return keyMembersIndices.get(keyMember);
    }

    public Optional<PropertyMetadata> getProperty(final String name) {
        return Optional.ofNullable(props.get(name));
    }

}
