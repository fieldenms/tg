package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditEntityType;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditEntityType;

final class AuditTypeFinder implements IAuditTypeFinder {

    /**
     * A map in which each entry associates an audited entity type with a non-empty sequence of its audit-entity types sorted by version in ascending order.
     * For example, {@code Vehicle} may be mapped to {@code [Vehicle_a3t_1, Vehicle_a3t_2, ..., Vehicle_a3t_n]}.
     */
    private final Map<Class<? extends AbstractEntity<?>>, List<Class<? extends AbstractAuditEntity<?>>>> auditEntityTypesMap;

    /**
     * A map in which each entry associates an audited entity type with a synthetic audit-entity type.
     */
    private final Map<Class<? extends AbstractEntity<?>>, Class<? extends AbstractSynAuditEntity<?>>> synAuditEntityTypesMap;

    AuditTypeFinder(final Iterable<Class<?>> types) {
        this.auditEntityTypesMap = Streams.stream(types)
                .filter(ty -> ty != AbstractAuditEntity.class && isAuditEntityType(ty))
                .map(ty -> (Class<AbstractAuditEntity<AbstractEntity<?>>>) ty)
                .collect(groupingBy(AuditUtils::getAuditedType,
                                    collectingAndThen(toImmutableList(),
                                                      list -> ImmutableList.sortedCopyOf(comparingLong(AuditUtils::getAuditEntityTypeVersion), list))));
        this.synAuditEntityTypesMap = Streams.stream(types)
                .filter(ty -> ty != AbstractSynAuditEntity.class && isSynAuditEntityType(ty))
                .map(ty -> (Class<AbstractSynAuditEntity<AbstractEntity<?>>>) ty)
                .collect(toImmutableMap(AuditUtils::getAuditedTypeForSyn, Function.identity()));
    }

    @Override
    public <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditEntityType(final Class<E> entityType) {
        return (Class<AbstractAuditEntity<E>>) requireAllAuditEntityTypesFor(entityType).getLast();
    }

    @Override
    public <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(final Class<E> entityType) {
        final var auditEntityTypes = auditEntityTypesMap.get(entityType);
        return auditEntityTypes == null
                ? Optional.empty()
                : Optional.of((Class<AbstractAuditEntity<E>>) auditEntityTypes.getLast());
    }

    @Override
    public <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditEntityType(
            final Class<E> entityType,
            final int version)
    {
        if (version <= 0) {
            throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
        }

        final var auditEntityTypes = requireAllAuditEntityTypesFor(entityType);
        if (version > auditEntityTypes.size()) {
            throw new IllegalArgumentException("Audit-entity type with version %s for [%s] doesn't exist.".formatted(version, entityType.getSimpleName()));
        }
        return (Class<AbstractAuditEntity<E>>) auditEntityTypes.get(version - 1);
    }

    @Override
    public <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(
            final Class<E> entityType,
            final int version)
    {
        if (version <= 0) {
            throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
        }

        final var auditEntityTypes = requireAllAuditEntityTypesFor(entityType);
        return version > auditEntityTypes.size()
                ? Optional.empty()
                : Optional.of((Class<AbstractAuditEntity<E>>) auditEntityTypes.get(version - 1));
    }

    @Override
    public <AE extends AbstractAuditEntity<?>> Class<AbstractAuditProp<AE>> getAuditPropTypeForAuditEntity(final Class<AE> auditEntityType) {
        return AuditUtils.getAuditPropTypeForAuditType(auditEntityType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> findAllAuditEntityTypesFor(final Class<? extends AbstractEntity<?>> entityType) {
        return (Collection) auditEntityTypesMap.getOrDefault(entityType, ImmutableList.of());
    }

    @Override
    public <E extends AbstractEntity<?>> Class<AbstractSynAuditEntity<E>> getSynAuditEntityType(final Class<E> entityType) {
        final var synAuditType = synAuditEntityTypesMap.get(entityType);
        if (synAuditType == null) {
            throw new InvalidArgumentException("Synthetic audit-entity type for [%s] doesn't exist.".formatted(entityType.getSimpleName()));
        }
        return (Class<AbstractSynAuditEntity<E>>) synAuditType;
    }

    @Override
    public <E extends AbstractEntity<?>> Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType(final Class<E> entityType) {
        final var synAuditType = synAuditEntityTypesMap.get(entityType);
        return Optional.ofNullable((Class<AbstractSynAuditEntity<E>>) synAuditType);
    }

    @Override
    public <E extends AbstractSynAuditEntity<?>> Class<AbstractSynAuditProp<E>> getSynAuditPropTypeForSynAuditEntity(final Class<E> synAuditType) {
        return AuditUtils.getSynAuditPropTypeForSynAuditType(synAuditType);
    }

    @Override
    public <E extends AbstractSynAuditEntity<?>> Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropTypeForSynAuditEntity(final Class<E> synAuditType)
    {
        return AuditUtils.findSynAuditPropTypeForSynAuditType(synAuditType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends AbstractEntity<?>> Collection<Class<AbstractAuditEntity<E>>> getAllAuditEntityTypesFor(final Class<? extends AbstractEntity<?>> entityType) {
        return (Collection) requireAllAuditEntityTypesFor(entityType);
    }

    private <E extends AbstractEntity<?>> List<Class<? extends AbstractAuditEntity<?>>> requireAllAuditEntityTypesFor(final Class<E> entityType) {
        final var auditEntityTypes = auditEntityTypesMap.get(entityType);
        if (auditEntityTypes == null) {
            throw new InvalidArgumentException("Audit-entity type for [%s] doesn't exist.".formatted(entityType.getSimpleName()));
        }
        return auditEntityTypes;
    }

}
