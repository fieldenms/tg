package ua.com.fielden.platform.eql.meta;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

record DomainTypeData(
        Class<?> type,
        Class<?> superType,
        long id,
        String key,
        String desc,
        boolean isEntity,
        String dbTable,
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
                : zip(keyMembers.stream(), IntStream.iterate(1, i -> i + 1).boxed(), T2::t2)
                        .collect(toImmutableMap(t2 -> t2._1.name(), t2 -> t2._2));

        return new DomainTypeData(type, superType, id, key, desc, isEntity, dbTable, entityTypeDesc, propsCount,
                                  keyMembersIndicesMap, unmodifiableMap(propsMap));
    }

    public Integer getKeyMemberIndex(final String keyMember) {
        return keyMembersIndices.get(keyMember);
    }

}
