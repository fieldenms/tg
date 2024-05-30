package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.UNION_MEMBER;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

final class EntityMetadataUtilsImpl implements EntityMetadataUtils {

    @Override
    public List<PropertyMetadata> compositeKeyMembers(final EntityMetadata entityMetadata) {
        final var compKeyMembers = entityMetadata.properties().stream()
                .filter(prop -> prop.has(KEY_MEMBER))
                .collect(toImmutableList());
        return validateCompositeKeyMembers(compKeyMembers, entityMetadata);
    }

    @Override
    public List<PropertyMetadata> keyMembers(final EntityMetadata entityMetadata) {
        final var compKeyMembers = compositeKeyMembers(entityMetadata);
        return compKeyMembers.isEmpty()
                ? ImmutableList.of(entityMetadata.property("key").orElseThrow()) // TODO informative error message
                : compKeyMembers;
    }

    @Override
    public List<PropertyMetadata> unionMembers(final EntityMetadata.Union entityMetadata) {
        return entityMetadata.properties().stream()
                .filter(pm -> pm.is(UNION_MEMBER))
                .toList();
    }

    private static List<PropertyMetadata> validateCompositeKeyMembers(final List<PropertyMetadata> members,
                                                                      final EntityMetadata entityMetadata) {
        if (members.size() <= 1) {
            return members;
        }

        // [(property, key member order)]
        final var seenMembers = new ArrayList<PropertyMetadata>(5);
        final var seenOrders = new ArrayList<Integer>(5);

        for (final var memb : members) {
            final var atCompositeKeyMember = memb.get(KEY_MEMBER).orElseThrow();
            final int order = atCompositeKeyMember.value();

            // side-effectful stream
            zip(seenMembers.stream(), seenOrders.stream(), (seenMemb, seenOrder) -> {
                // protect against mistakes with boxed Integer
                if (Integer.compare(order, seenOrder) == 0) {
                    throw new EntityDefinitionException(
                            format(ERR_DUPLICATE_COMPOSITE_KEY_ORDER,
                                   entityMetadata.javaType().getTypeName(), memb.name(), seenMemb.name()));
                }
                return "ok"; // return anything
            }).close();

            seenMembers.add(memb);
            seenOrders.add(order);
        }

        return members;
    }
    // where
    private static final String ERR_DUPLICATE_COMPOSITE_KEY_ORDER = """
Duplicate CompositeKeyMember order detected in entity [%s], conflicting properties: [%s] and [%s].""";

}
